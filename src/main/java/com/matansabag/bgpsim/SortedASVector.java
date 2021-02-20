package com.matansabag.bgpsim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortedASVector {
  enum COMPARISON_METHOD {
    BY_NUMBER,
    BY_CUSTOMERS,
    BY_PEERS,
    BY_PROVIDERS
  }

  private static Map<Integer, AS> kPlainGraph;
  private final List<Integer> as_vector_;
  private final COMPARISON_METHOD method_;
  private final Map<Integer, Integer> percentiles_;
  public static final int kResolution = 10000;

  SortedASVector(List<Integer> as_vector, COMPARISON_METHOD method, Map<Integer, AS> graph) {
    this.as_vector_ = as_vector;
    this.method_ = method;
    kPlainGraph = graph;
    this.percentiles_ = new HashMap<>();
    sort_ases();
    compute_ranks();
  }

  public int get_as_rank_group(int as_number) {
    return percentiles_.get(as_number);
  }

  private void compute_ranks() {
    for (int i = 0; i < as_vector_.size(); i++) {

      int percentile = ((kResolution * i) / as_vector_.size()) + 1;
      if (percentile > kResolution) {
        percentile = kResolution;
      }
      percentiles_.put(as_vector_.get(i), percentile);
    }
  }

  private void sort_ases() {
    switch (method_) {
      case BY_CUSTOMERS:
        as_vector_.sort(
            (o1, o2) -> {
              return compare_ases_by_customers(o1, o2) ? 1 : 0; // TODO CHECK
            });
        break;
      default:
        throw new IllegalArgumentException("unsupported sort method");
    }
  }

  private static boolean compare_ases_by_customers(int as_a, int as_b) {
    return compare_ases(as_a, as_b, COMPARISON_METHOD.BY_CUSTOMERS);
  }

  private static boolean compare_ases(int as_a, int as_b, COMPARISON_METHOD method) {
    try {
      switch (method) {
        case BY_NUMBER:
          return as_a > as_b;
        case BY_CUSTOMERS:
          return kPlainGraph.get(as_a).customers().size()
              > kPlainGraph.get(as_b).customers().size();
        case BY_PEERS:
          return kPlainGraph.get(as_a).peers().size() > kPlainGraph.get(as_b).peers().size();
        case BY_PROVIDERS:
          return kPlainGraph.get(as_a).providers().size()
              > kPlainGraph.get(as_b).providers().size();
      }
      return false;
    } catch (Exception e) {
      System.out.println(e);
    }
    return false;
  }
}
