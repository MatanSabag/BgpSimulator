package com.matansabag.bgpsim;

import static com.matansabag.bgpsim.AttackSimulatorMain.getResourceFullPath;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.matansabag.bgpsim.BGPGraph.BGPGraphBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BgpMain {
  private static String kASRelationshipsFile = getResourceFullPath("data/20190801.as-rel.txt.bakk");
  private static String kASRegionsFile = getResourceFullPath("data/as-numbers-1.csv");
  private static String kASRegionsFile32bit = getResourceFullPath("data/as-numbers-2.csv");
  private static String kASExtraRelationshipsFile =
      getResourceFullPath("data/AS_link_extended.txt");;
  private static String kASExtraRelationshipsCaidaFile =
      getResourceFullPath("data/mlp-Dec-2014.txt");
  private static String kVantagePointsFile = getResourceFullPath("data/vantage-points-list.txt");

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
    GraphProcessor gp = new GraphProcessor(graph);
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
    GraphProcessor gp = new GraphProcessor(graph);
    Set<Integer> destinations = new HashSet<>(Arrays.asList(1, 5));
    Table<Integer, Integer, Route> integerIntegerRouteTable = gp.pathsToDestinations(destinations);
    Table<Integer, Integer, Route> integerIntegerRouteTable1 = gp.completeRoutingMap();
    for (Cell<Integer, Integer, Route> cell : integerIntegerRouteTable1.cellSet()) {
      System.out.println(
          cell.getRowKey() + "->" + cell.getColumnKey() + " ===> " + cell.getValue());
    }
  }
}
