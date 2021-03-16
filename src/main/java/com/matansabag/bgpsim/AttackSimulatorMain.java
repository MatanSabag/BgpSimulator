package com.matansabag.bgpsim;

import com.matansabag.bgpsim.BGPGraph.BGPGraphBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AttackSimulatorMain {
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
  private static boolean isMiniExample = false;


  public static void main(String[] args) throws Exception {
    System.out.println("Starting attack simulator");
    BGPGraph graph = createBgpGraph();

    Set<Integer> dots = getTier1Ases(graph);
    AttackSimulator sim = new AttackSimulator(graph);
    sim.simulate(getPotentialReflectors(), dots, getVictims());
    //
    // List<Integer> allAsns = graph.get_all_ases().stream().map(AS::number)
    //     .collect(Collectors.toList());
    // AttackSimulator sim = new AttackSimulator(graph);
    // int i = 3000;
    // System.out.println("Choosing randomly " + i + " items");
    // sim.simulate(getPotentialReflectors(), new HashSet<>(pickNRandomElements(allAsns, i)), getVictims());
  }

  public static <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
    int length = list.size();

    if (length < n) return null;

    //We don't need to shuffle the whole list
    for (int i = length - 1; i >= length - n; --i)
    {
      Collections.swap(list, i , r.nextInt(i + 1));
    }
    return list.subList(length - n, length);
  }

  public static <E> List<E> pickNRandomElements(List<E> list, int n) {
    return pickNRandomElements(list, n, ThreadLocalRandom.current());
  }

  private static Set<Integer> getVictims() throws Exception {
    if (isMiniExample) {
      return new HashSet<>(Arrays.asList(16));
    }
    return new HashSet<>(Arrays.asList(16509, 36459));
  }

  private static Map<Integer, Long> getPotentialReflectors() throws Exception {
    if (isMiniExample) {
      Map<Integer, Long> map = new HashMap<>();
      map.put(11, 1L);
      map.put(13, 1L);
      map.put(8, 1L);
      return map;
    }
    Map<Integer, Long> attackers =
        new ShodanParser("/Users/matans/Downloads/shodan-export.json").parseShodanParser();
    attackers.values().removeIf(v -> v <= 10);
    return attackers;
  }

  private static BGPGraph createBgpGraph() {
    String relationsFile =
        isMiniExample
            ? "/Users/matans/disco/bgp-sim/data/20190801.as-rel.txt"
            : "/Users/matans/disco/bgp-sim/data/20190801.as-rel.txt.bakk";
    return new BGPGraphBuilder()
        .withAdditionalLinks(false)
        .withInfile(relationsFile)
        .withKASRegionsFile(kASRegionsFile)
        .withKASRegionsFile32bit(kASRegionsFile32bit)
        .withKASExtraRelationshipsFile(kASExtraRelationshipsFile)
        .withKASExtraRelationshipsCaidaFile(kASExtraRelationshipsCaidaFile)
        .withKVantagePointsFile(kVantagePointsFile)
        .build();
  }

  private static Set<Integer> getTier1Ases(BGPGraph graph) {
    return graph.get_all_ases().stream()
        .filter(a -> a.providers().size() == 0)
        .map(AS::number)
        .collect(Collectors.toSet());
  }
}
