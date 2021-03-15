package com.matansabag.bgpsim;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.matansabag.bgpsim.AS.RIR;
import com.matansabag.bgpsim.BGPGraph.BGPGraphBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BgpMain {
  private static String kASRelationshipsFile =
      "/Users/matans/disco/bgp-sim/data/20190801.as-rel.txt.bakk";
  private static String kASRegionsFile = "/Users/matans/disco/bgp-sim/data/as-numbers-1.csv";
  private static String kASRegionsFile32bit = "/Users/matans/disco/bgp-sim/data/as-numbers-2.csv";
  private static String kASExtraRelationshipsFile =
      "/Users/matans/disco/bgp-sim/data/AS_link_extended.txt";
  private static String kASExtraRelationshipsCaidaFile =
      "/Users/matans/disco/bgp-sim/data/mlp-Dec-2014.txt";
  private static String kVantagePointsFile =
      "/Users/matans/disco/bgp-sim/data/vantage-points-list.txt";

  GraphProcessor createGraphProcessor() {
    BGPGraph graph =
        new BGPGraphBuilder()
            .withAdditionalLinks(false)
            .withInfile(kASRelationshipsFile)
            .withKASRegionsFile(kASRegionsFile)
            .withKASRegionsFile32bit(kASRegionsFile32bit)
            .withKASExtraRelationshipsFile(kASExtraRelationshipsFile)
            .withKASExtraRelationshipsCaidaFile(kASExtraRelationshipsCaidaFile)
            .withKVantagePointsFile(kVantagePointsFile)
            .build();
    GraphProcessor gp = new GraphProcessor(graph, RIR.ALL, RIR.ALL, false);
    return gp;
  }

  public static void main(String[] args) {
    BGPGraph graph =
        new BGPGraphBuilder()
            .withAdditionalLinks(false)
            .withInfile(kASRelationshipsFile)
            .withKASRegionsFile(kASRegionsFile)
            .withKASRegionsFile32bit(kASRegionsFile32bit)
            .withKASExtraRelationshipsFile(kASExtraRelationshipsFile)
            .withKASExtraRelationshipsCaidaFile(kASExtraRelationshipsCaidaFile)
            .withKVantagePointsFile(kVantagePointsFile)
            .build();
    GraphProcessor gp = new GraphProcessor(graph, RIR.ALL, RIR.ALL, false);
    Set<Integer> destinations = new HashSet<>(Arrays.asList(1, 5));
    Table<Integer, Integer, Route> integerIntegerRouteTable = gp.pathsToDestinations(destinations);
    Table<Integer, Integer, Route> integerIntegerRouteTable1 = gp.completeRoutingMap();
    for (Cell<Integer, Integer, Route> cell : integerIntegerRouteTable1.cellSet()) {
      System.out.println(
          cell.getRowKey() + "->" + cell.getColumnKey() + " ===> " + cell.getValue());
    }
  }
}
