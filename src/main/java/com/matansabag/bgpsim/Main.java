package com.matansabag.bgpsim;

import com.matansabag.bgpsim.AS.RIR;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    boolean create_additional_links = false;
    BGPGraph graph = new BGPGraph(create_additional_links);
    printGraph(graph);
    GraphProcessor gp = new GraphProcessor(graph, RIR.ALL, RIR.ALL, false);
    Map<Integer, RoutingTable> dijekstra = gp.Dijekstra(17, 100, 0, false, false, true);
    System.out.println("x");
  }

  private static void printGraph(BGPGraph graph) {
    Map<Integer, AS> plain = graph.get_plain();
    StringBuilder sb = new StringBuilder();
    for (AS as : plain.values()) {
      sb.append("---------------------\n");
      sb.append("Printing relationship of AS:").append(as.number()).append("\n");
      sb.append("customers:\n").append(as.customers()).append("\n");
      sb.append("providers:\n").append(as.providers()).append("\n");
      sb.append("peers:\n").append(as.peers()).append("\n");
      sb.append("---------------------\n");
    }
    System.out.println(sb.toString());
  }
}
