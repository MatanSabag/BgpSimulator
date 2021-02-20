package com.matansabag.bgpsim;

public class RouteFilter {

  private final BGPGraph graph_;
  private final SortedASVector sorted_ases_;
  private final boolean filter_by_length_;
  private final boolean two_hop_filtering_extension_;

  private static final int kMaxPathLength = 4;

  public RouteFilter(
      BGPGraph graph, SortedASVector sorted_ases, boolean two_hop_filtering_extension) {
    this.graph_ = graph;
    this.sorted_ases_ = sorted_ases;
    this.filter_by_length_ = false; // TODO removing this from the code
    this.two_hop_filtering_extension_ = two_hop_filtering_extension;
  }

  public boolean should_filter(int filtering_as, Route route) {
    return should_filter(filtering_as, route, -1);
  }

  public boolean should_filter(int filtering_as, Route route, int filtering_as_percentile) {
    // Adopter AS can also validate
    if (!graph_.get(filtering_as).adopter()) {
      return false;
    }

    int dst_as = route.getDestAS();

    if (filtering_as_percentile < 0) {
      filtering_as_percentile = sorted_ases_.get_as_rank_group(filtering_as);
    }

    // filter prefix hijacks
    if (route.hijacked()) {
      return true;
    }

    // Validate that first hop is valid. // TODO: change this validation to iterate the route...
    if ((route.length() > 1) && (!graph_.get(dst_as).isNeighbour(route.getLastHop()))) {
      return true;
    }

    if (two_hop_filtering_extension_) {
      if ((route.length() > 2)
          && (!graph_.get(route.getLastHop()).isNeighbour(route.getBeforeLastHop()))) {
        return true;
      }
    }

    int min_percentile = Math.min(sorted_ases_.get_as_rank_group(dst_as), filtering_as_percentile);
    // We force attackers to increasing path length, then enforce high limit on path length.
    if (filter_by_length_) {
      if (route.length() + 1 > 7) { // 7 threshold is probably best here..
        return true;
      }
      if (graph_.are_ases_in_same_region(filtering_as, dst_as)) {
        if (filtering_as_percentile <= 5) {
          if (route.length() + 1 > 5) {
            return true;
          }
        } else if (filtering_as_percentile <= 15) {
          if (route.length() + 1 > 6) {
            return true;
          }
        }
      } else {
        if (min_percentile <= 3) {
          if (route.length() + 1 > 6) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean did_adopter_lose_because_he_adopted(Route unfiltered_route, Route filtered_route) {
    return false; // @@ NOT IMPL
  }
}
