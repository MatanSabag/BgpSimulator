package com.matansabag.bgpsim;

import static com.matansabag.bgpsim.BGPGraph.Link_Type.LINK_NONE;
import static com.matansabag.bgpsim.BGPGraph.Link_Type.LINK_TO_PEER;
import static com.matansabag.bgpsim.BGPGraph.Link_Type.LINK_TO_PROVIDER;

import com.matansabag.bgpsim.BGPGraph.Link_Type;
import com.matansabag.bgpsim.Route.route_type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingTable {

  enum ADVERTISEMENT_DEST {
    ADVERTISE_TO_CUSTOMER,
    ADVERTISE_TO_PEER,
    ADVERTISE_TO_PROVIDER
  }

  private final int as_number_;
  private final BGPGraph graph_;
  private final int my_percentile_;
  private final RouteFilter filter_;
  private final Map<Integer, List<Route>> routing_table_;
  private final Map<Integer, Boolean> heard_legitimate_path_;
  // dst as_number -> provider AS -> route

  Map<Integer, Map<Integer, Route>> alt_routes_ = new HashMap<>();
  private static SortedASVector kSortedAses = null;

  public RoutingTable(int as_number, BGPGraph graph, boolean filter_two_neighbours) {
    this(as_number, graph, filter_two_neighbours, false);
  }

  public RoutingTable(
      int as_number, BGPGraph graph, boolean filter_two_neighbours, boolean is_real_dst) {
    this.as_number_ = as_number;
    this.graph_ = graph;
    this.my_percentile_ = kSortedAses.get_as_rank_group(as_number_);
    this.filter_ = new RouteFilter(graph, kSortedAses, filter_two_neighbours);
    this.routing_table_ = new HashMap<>();
    this.heard_legitimate_path_ = new HashMap<>();
    if (is_real_dst) {
      List<Integer> self_route = new ArrayList<>();
      self_route.add(as_number_);
      Route my_self_route = new Route(LINK_NONE, self_route, route_type.LEGITIMATE);
      my_self_route.optattr_protected = true;
      routing_table_.put(as_number, new ArrayList<>());
      routing_table_.get(as_number).add(my_self_route);
      routing_table_.get(as_number).add(my_self_route);
      routing_table_.get(as_number).add(my_self_route);
    }
  }

  public void announce_spoofed_route(Route spoofed_route) {
    // TODO IGNORE FOR NOW
  }

  public boolean consider_new_route(Route new_route, Link_Type link_type) {
    int dst_as = new_route.getDestAS();

    update_legitimate_route_table(dst_as, new_route);

    // Discard routes with the BGP opt attribute
    if (((graph_.get(as_number_)).optattr_discard_prefix()) && new_route.optattr_protected) {
      // cout << "Discarding prefix due to optattr.\n";
      return false;
    }

    // TODO RouteFilter logic here...
    if (filter_.should_filter(as_number_, new_route, my_percentile_)) {
      return true;
    }

    Route appended_route = new Route(new_route);
    //	cout << "consider_new_route appended_route->optattr_protected " <<
    // appended_route->optattr_protected << "\n";
    appended_route.append(as_number_, link_type, graph_);
    // store this route as an alternative from the neighbor,
    // we might use is later if we receive a better route that is later withdrawn

    Map<Integer, Route> neigh2route = alt_routes_.computeIfAbsent(dst_as, k -> new HashMap<>());
    neigh2route.put(appended_route.getNeighbor(), appended_route);
    boolean ret = false;
    for (int i = 0; i < 3; i++) {

      if ((link_type == LINK_TO_PROVIDER) && (i > 0)) {
        break;
      }

      if ((link_type == LINK_TO_PEER) && (i > 0)) {
        break;
      }

      Route existing_route = get_route_or_null(dst_as, ADVERTISEMENT_DEST.values()[i]); // FIXME

      List<Route> it2 = routing_table_.get(dst_as);
      // map<int, vector<shared_ptr<Route> > >::iterator it = routing_table_.find(dst_as);

      if (existing_route == null) {
        if (it2 == null || it2.isEmpty()) {
          routing_table_.put(dst_as, new ArrayList<>());
          routing_table_.get(dst_as).add(null);
          routing_table_.get(dst_as).add(null);
          routing_table_.get(dst_as).add(null);
        }

        it2 = routing_table_.get(dst_as);
        it2.set(i, appended_route);
        ret = true;
      } else if (existing_route.is_new_route_better(appended_route)) {
        it2.set(i, appended_route);
        ret = true;
      } else if (!(existing_route.equals(appended_route))
          && existing_route.from_same_neighbor(appended_route)) {
        // Old route no longer exists as it's been overwritten
        it2.set(i, appended_route);
        int neighbor = appended_route.getNeighbor();

        // maybe one of the neighbors has offered a better route
        neigh2route = alt_routes_.get(dst_as);
        for (Map.Entry<Integer, Route> p : neigh2route.entrySet()) {
          int altneighbor = p.getKey();
          if (altneighbor == neighbor) {
            continue;
          }
          Route altroute = p.getValue();
          if (it2.get(i).is_new_route_better(altroute)) {
            it2.set(i, altroute);
          }
        }
        ret = true;
      }
    }
    return ret;
  }

  public Route get_route_or_null(int dst_as, ADVERTISEMENT_DEST neighbour) {
    List<Route> routes = routing_table_.get(dst_as);
    if (routes == null || routes.isEmpty()) { // TODO check?
      return null;
    }
    return routes.get(neighbour.ordinal());
  }

  public Route get_my_route_or_null(int dst_as_number) {
    Route best = null;
    for (int i = 0; i < 3; i++) {
      Route next = get_route_or_null(dst_as_number, ADVERTISEMENT_DEST.values()[i]);
      if (next == null) {
        continue;
      }

      if ((best == null) || (best.is_new_route_better(next))) {
        best = next;
      }
    }
    return best;
  }

  public boolean received_only_malicious(int dst_as_number) {
    if (!heard_legitimate_path_.containsKey(dst_as_number)) return true;
    return !heard_legitimate_path_.get(dst_as_number);
  }

  public int size() {
    return routing_table_.size();
  }

  public Map<Integer, List<Route>> getRT() {
    return routing_table_;
  }

  public static void set_sorted_ases(SortedASVector sorted_ases) {
    kSortedAses = sorted_ases;
  }

  private void update_legitimate_route_table(int dst_as_number, Route route) {
    if (!route.malicious()) {
      heard_legitimate_path_.put(dst_as_number, true);
    }
  }
}
