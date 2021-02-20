package com.matansabag.bgpsim;

import com.matansabag.bgpsim.BGPGraph.Link_Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Route {

  enum route_type {
    LEGITIMATE,
    MALICIOUS,
    PREFIX_HIJACK
  };
  private Link_Type last_link_;
  private final List<Integer> as_list_;
  private route_type is_malicious_;

  // public
  public boolean optattr_protected = false;

  Route(Link_Type last_link, List<Integer> as_list, route_type is_malicious) {
    this.last_link_ = last_link;
    this.as_list_ = as_list;
    this.is_malicious_ = is_malicious;
  }

  Route(Route other){
    this.last_link_ = other.last_link_;
    this.as_list_ = new ArrayList<>(other.as_list_);
    this.is_malicious_ = other.is_malicious_;
    this.optattr_protected = other.optattr_protected;
  }

  int getDestAS()  { return as_list_.get(0); }

  boolean malicious()  { return is_malicious_ != route_type.LEGITIMATE; }

  boolean hijacked()  { return is_malicious_ == route_type.PREFIX_HIJACK; }

  int length()  { return as_list_.size(); }

  List<Integer> get_as_list()  { return as_list_; }

  // Link_Type get_prev_link_type(int intermediate_as) const; FIXME ??

  boolean from_same_neighbor(Route other){
    int this_neigh = getNeighbor();
    int other_neigh = other.getNeighbor();
    return this_neigh == other_neigh;
  }

  boolean is_new_route_better(Route other) {

    // InSecurity Second..
    // assuming adopters always filter malicious route before getting here
    // (when hops <= 1)

    // @@Matan: Prefer customer over peer over provider
    if (other.last_link_.ordinal() < last_link_.ordinal()) {
      return true;
    }

    if (other.last_link_.ordinal() > last_link_.ordinal()) {
      return false;
    }

    // @@Matan: Prefer shorter paths
    if (other.as_list_.size() < as_list_.size()) {
      return true;
    }

    if (other.as_list_.size() > as_list_.size()) {
      return false;
    }

    // Destination only route...
    if (as_list_.size() == 1) {
      return false;
    }

    // final break..
    return  as_list_.get(as_list_.size() - 2) > other.as_list_.get(as_list_.size() - 2);
  }

  void append(int as_number, Link_Type link_type, BGPGraph graph) {
    as_list_.add(as_number);
    last_link_ = link_type;
    if (graph.get(as_number).malicious()) {
      is_malicious_ = route_type.MALICIOUS;
    }
  }

  int getLastHop(){
    return as_list_.get(0);
  }

  int getBeforeLastHop(){
    return as_list_.get(1);
  }

  public int getNeighbor() {
    return as_list_.get(as_list_.size()-2);
  }

  // bool operator==(Route& Oother) const;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Route route = (Route) o;
    return Objects.equals(as_list_, route.as_list_);
  }

  @Override
  public int hashCode() {
    return Objects.hash(as_list_);
  }

  public String toString() {
    return as_list_.stream().
        map(Object::toString).
        collect(Collectors.joining(", "));
  }
}
