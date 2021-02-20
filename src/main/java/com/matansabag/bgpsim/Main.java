package com.matansabag.bgpsim;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.matansabag.bgpsim.AS.RIR;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.Table.Cell;
public class Main {

  public static void main(String[] args) {
    boolean create_additional_links = false;
    BGPGraph graph = new BGPGraph(create_additional_links);
    // printGraph(graph);
    GraphProcessor gp = new GraphProcessor(graph, RIR.ALL, RIR.ALL, false);
    Map<Integer, RoutingTable> dijekstra = gp.pathsToDest(5);
    Map<Integer, RoutingTable> dijekstra2 = gp.Dijekstra(1, 100, 0, false, false, true);
    Table<Integer, Integer, Route> sourceToDestRoutes = HashBasedTable.create();
    for (Map.Entry<Integer, RoutingTable> integerRoutingTableEntry : dijekstra.entrySet()) {
      sourceToDestRoutes.put(integerRoutingTableEntry.getKey(), 5, integerRoutingTableEntry.getValue().get_my_route_or_null(5));
    }
    for (Map.Entry<Integer, RoutingTable> integerRoutingTableEntry : dijekstra2.entrySet()) {
      sourceToDestRoutes.put(integerRoutingTableEntry.getKey(), 1, integerRoutingTableEntry.getValue().get_my_route_or_null(1));
    }
    for (Cell<Integer, Integer, Route> cell: sourceToDestRoutes.cellSet()){
      System.out.println(cell.getRowKey()+"->" + cell.getColumnKey()+" ===> "+cell.getValue());
    }
    // printPathToDestFromAllSources(dijekstra, 5);
    // System.out.println("x");
  }

  private static void printPathToDestFromAllSources(Map<Integer, RoutingTable> dijekstra, int i) {
    for (Integer integer : dijekstra.keySet()) {
      System.out.println(
          "Path from "
              + integer
              + " to: "
              + i
              + " is: "
              + dijekstra.get(integer).get_my_route_or_null(i).get_as_list());
    }
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
