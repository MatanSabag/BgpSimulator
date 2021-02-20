package com.matansabag.bgpsim;

import static com.matansabag.bgpsim.BGPGraph.Link_Type.LINK_TO_CUSTOMER;
import static com.matansabag.bgpsim.BGPGraph.Link_Type.LINK_TO_PEER;
import static com.matansabag.bgpsim.BGPGraph.Link_Type.LINK_TO_PROVIDER;

import com.matansabag.bgpsim.AS.RIR;
import com.matansabag.bgpsim.RoutingTable.ADVERTISEMENT_DEST;
import com.matansabag.bgpsim.SortedASVector.COMPARISON_METHOD;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphProcessor {
  enum PATH_TYPE {
    LEGITIMATE,
    MALICIOUS
  }

  private final BGPGraph graph_;
  // mutable mutex lock_;
  private final List<Integer> all_ases_;
  private final SortedASVector sorted_ases_;
  private final RIR attackers_region_;
  private final RIR victims_region_;
  private final boolean filter_two_neighbours_;
  // protected
  int vp_all_ = 0;
  int noattack_vp_all_ = 0;
  int vp_fooled_ = 0;
  int noattack_vp_fooled_ = 0;
  int vp_optattr_ = 0;
  int noattack_vp_optattr_ = 0;
  // Map<Integer, Integer> distance_Map_hijack_;
  // Map<Integer, Integer> distance_Map_legit_;
  // List<Double> results_ = new ArrayList<>(); // [2];
  // List< List < List <Double> > > path_lengths_ = new ArrayList<>();// path_lengths_[2];
  // List< List <Double> > path_diffs_;

  GraphProcessor(
      BGPGraph graph, RIR attackers_region, RIR victims_region, boolean filter_two_neighbours) {
    this.graph_ = graph;
    this.all_ases_ = graph_.get_all_ases(RIR.ALL);
    this.sorted_ases_ =
        new SortedASVector(all_ases_, COMPARISON_METHOD.BY_CUSTOMERS, graph.get_plain());
    this.attackers_region_ = attackers_region;
    this.victims_region_ = victims_region;
    this.filter_two_neighbours_ = filter_two_neighbours;
    RoutingTable.set_sorted_ases(sorted_ases_);
  }

  Map<Integer, RoutingTable> pathsToDest(int dest_as_number) {
    Queue<Integer> q = new LinkedList<>();
    Map<Integer, RoutingTable> route_tables = new HashMap<>();
    Set<Integer> in_queue = new HashSet<>();

    // insert the destination to processing queue
    route_tables.put(
        dest_as_number, new RoutingTable(dest_as_number, graph_, filter_two_neighbours_, true));
    in_queue.add(dest_as_number);
    q.add(dest_as_number);
    while (!q.isEmpty()) {
      int current = q.peek();
      AS currAS = graph_.get(current);
      q.poll();
      in_queue.remove(current);

      // iterate customers
      Route optional_route =
          route_tables
              .get(currAS.number())
              .get_route_or_null(dest_as_number, ADVERTISEMENT_DEST.ADVERTISE_TO_CUSTOMER);
      boolean prot_prev;
      if (optional_route != null) {
        prot_prev = optional_route.optattr_protected;
        if (optional_route.optattr_protected && currAS.optattr_discard_attr()) {
          // cout << "Discarding the opt attribute\n";
          optional_route.optattr_protected = false;
        } // else
        // cout << "NOT Discarding the opt attribute\n";

        for (Integer customer : currAS.customers()) {

          if ((customer == dest_as_number)) {
            continue;
          }
          if (!route_tables.containsKey(customer)) {
            route_tables.put(customer, new RoutingTable(customer, graph_, filter_two_neighbours_));
          }
          if (route_tables.get(customer).consider_new_route(optional_route, LINK_TO_PROVIDER)) {
            if (!in_queue.contains(customer)) {
              q.add(customer);
              in_queue.add(customer);
            }
          }
        }
        optional_route.optattr_protected = prot_prev;
      }

      // iterate peers
      optional_route =
          route_tables
              .get(currAS.number())
              .get_route_or_null(dest_as_number, ADVERTISEMENT_DEST.ADVERTISE_TO_PEER);

      if (optional_route != null) {
        prot_prev = optional_route.optattr_protected;
        if (optional_route.optattr_protected && currAS.optattr_discard_attr()) {
          optional_route.optattr_protected = false;
          // cout << "Discarding the opt attribute\n";
        } // else
        // cout << "NOT Discarding the opt attribute\n";

        for (Integer peer : currAS.peers()) {

          if ((peer == dest_as_number)) {
            continue;
          }
          if (!route_tables.containsKey(peer)) {
            route_tables.put(peer, new RoutingTable(peer, graph_, filter_two_neighbours_));
          }
          if ((route_tables).get(peer).consider_new_route(optional_route, LINK_TO_PEER)) {
            if (!in_queue.contains(peer)) {
              q.add(peer);
              in_queue.add(peer);
            }
          }
        }
        optional_route.optattr_protected = prot_prev;
      }

      // iterate providers
      optional_route =
          route_tables
              .get(currAS.number())
              .get_route_or_null(dest_as_number, ADVERTISEMENT_DEST.ADVERTISE_TO_PROVIDER);

      if (optional_route != null) {
        prot_prev = optional_route.optattr_protected;
        if (optional_route.optattr_protected && currAS.optattr_discard_attr()) {
          optional_route.optattr_protected = false;
          // cout << "Discarding the opt attribute\n";
        } // else
        // cout << "NOT Discarding the opt attribute\n";

        for (Integer provider : currAS.providers()) {
          if ((provider == dest_as_number)) {
            continue;
          }
          if (!route_tables.containsKey(provider)) {
            route_tables.put(provider, new RoutingTable(provider, graph_, filter_two_neighbours_));
          }
          if ((route_tables).get(provider).consider_new_route(optional_route, LINK_TO_CUSTOMER)) {
            if (!in_queue.contains(provider)) {
              q.add(provider);
              in_queue.add(provider);
            }
          }
        }
        optional_route.optattr_protected = prot_prev;
      }
    }
    return route_tables;
  }

  Map<Integer, RoutingTable> Dijekstra(
      int dest_as_number,
      int attacker_as_number,
      int hops,
      boolean filter_by_length,
      boolean first_legacy,
      boolean skip_attacker) {

    Queue<Integer> q = new LinkedList<>();
    Map<Integer, RoutingTable> route_tables = new HashMap<>();
    Set<Integer> in_queue = new HashSet<>();

    // insert the destination to processing queue
    route_tables.put(
        dest_as_number, new RoutingTable(dest_as_number, graph_, filter_two_neighbours_, true));
    in_queue.add(dest_as_number);

    // insert the attacker to processing queue
    // try {
    //   // route_tables->insert(pair<int, shared_ptr<RoutingTable> >
    //   // (attacker_as_number, shared_ptr<RoutingTable>(new RoutingTable(attacker_as_number,
    // graph_, filter_by_length, filter_two_neighbours_))));
    //   route_tables.put(attacker_as_number, new RoutingTable(attacker_as_number, graph_,
    // filter_by_length, filter_two_neighbours_));
    //   List<Integer> spoofed_route = get_spoofed_route_by_hops(dest_as_number, attacker_as_number,
    // hops, first_legacy);
    //   route_type route_type = Route.route_type.MALICIOUS;
    //   if (hops == 0) {
    //     route_type = Route.route_type.PREFIX_HIJACK;
    //   }
    //   Route malicious_route = new Route(Link_Type.LINK_NONE, spoofed_route, route_type);
    //   route_tables.get(attacker_as_number).announce_spoofed_route(malicious_route);
    //   in_queue.add(attacker_as_number);
    // } catch (Exception e) {
    //   // all neighbours of dest support the protocol, hence attacker always loses (does not enter
    // the queue).
    //   skip_attacker = true;
    // }

    if (dest_as_number < attacker_as_number) {
      q.add(dest_as_number);
      if (!skip_attacker) {
        q.add(attacker_as_number);
      }
    } else {
      if (!skip_attacker) {
        q.add(attacker_as_number);
      }
      q.add(dest_as_number);
    }

    while (!q.isEmpty()) {
      int current = q.peek();
      AS currAS = graph_.get(current);
      q.poll();
      in_queue.remove(current);

      // iterate customers
      Route optional_route =
          route_tables
              .get(currAS.number())
              .get_route_or_null(dest_as_number, ADVERTISEMENT_DEST.ADVERTISE_TO_CUSTOMER);
      boolean prot_prev;
      if (optional_route != null) {
        prot_prev = optional_route.optattr_protected;
        if (optional_route.optattr_protected && currAS.optattr_discard_attr()) {
          // cout << "Discarding the opt attribute\n";
          optional_route.optattr_protected = false;
        } // else
        // cout << "NOT Discarding the opt attribute\n";

        for (Integer customer : currAS.customers()) {

          if ((customer == dest_as_number) || (customer == attacker_as_number)) {
            continue;
          }
          if (!route_tables.containsKey(customer)) {
            route_tables.put(
                customer,
                new RoutingTable(customer, graph_, filter_by_length, filter_two_neighbours_));
          }
          if (route_tables.get(customer).consider_new_route(optional_route, LINK_TO_PROVIDER)) {
            if (!in_queue.contains(customer)) {
              q.add(customer);
              in_queue.add(customer);
            }
          }
        }
        optional_route.optattr_protected = prot_prev;
      }

      // iterate peers
      optional_route =
          route_tables
              .get(currAS.number())
              .get_route_or_null(dest_as_number, ADVERTISEMENT_DEST.ADVERTISE_TO_PEER);

      if (optional_route != null) {
        prot_prev = optional_route.optattr_protected;
        if (optional_route.optattr_protected && currAS.optattr_discard_attr()) {
          optional_route.optattr_protected = false;
          // cout << "Discarding the opt attribute\n";
        } // else
        // cout << "NOT Discarding the opt attribute\n";

        for (Integer peer : currAS.peers()) {

          if ((peer == dest_as_number) || (peer == attacker_as_number)) {
            continue;
          }
          if (!route_tables.containsKey(peer)) {
            route_tables.put(
                peer, new RoutingTable(peer, graph_, filter_by_length, filter_two_neighbours_));
          }
          if ((route_tables).get(peer).consider_new_route(optional_route, LINK_TO_PEER)) {
            if (!in_queue.contains(peer)) {
              q.add(peer);
              in_queue.add(peer);
            }
          }
        }
        optional_route.optattr_protected = prot_prev;
      }

      // iterate providers
      optional_route =
          route_tables
              .get(currAS.number())
              .get_route_or_null(dest_as_number, ADVERTISEMENT_DEST.ADVERTISE_TO_PROVIDER);

      if (optional_route != null) {
        prot_prev = optional_route.optattr_protected;
        if (optional_route.optattr_protected && currAS.optattr_discard_attr()) {
          optional_route.optattr_protected = false;
          // cout << "Discarding the opt attribute\n";
        } // else
        // cout << "NOT Discarding the opt attribute\n";

        for (Integer provider : currAS.providers()) {
          if ((provider == dest_as_number) || (provider == attacker_as_number)) {
            continue;
          }
          if (!route_tables.containsKey(provider)) {
            route_tables.put(
                provider,
                new RoutingTable(provider, graph_, filter_by_length, filter_two_neighbours_));
          }
          if ((route_tables).get(provider).consider_new_route(optional_route, LINK_TO_CUSTOMER)) {
            if (!in_queue.contains(provider)) {
              q.add(provider);
              in_queue.add(provider);
            }
          }
        }
        optional_route.optattr_protected = prot_prev;
      }
    }
    return route_tables;
  }

  // Map<Integer, shared_ptr<RoutingTable> >* Dijekstra_avichai(int dst_as_number, int
  // attacker_as_number, int hops, boolean filter_by_length) const;
  double analyze_attacker_success(
      Map<Integer, RoutingTable> attacker_results, int dst_as_number, int filtering_mode) {
    return 1;
  }
}
