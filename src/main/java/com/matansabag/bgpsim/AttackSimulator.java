package com.matansabag.bgpsim;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.matansabag.bgpsim.AS.RIR;
import com.matansabag.bgpsim.BGPGraph.BGPGraphBuilder;
import java.util.Arrays;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

public class AttackSimulator {
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
  private static int numSuccess = 0;
  private static int numFailed = 0;
  private static Map<Integer, Map<Integer, Set<Integer>>> tripletsCalculates = new HashMap<>();


  public static void main(String[] args) throws Exception {
    System.out.println("Starting attack simulator");
    AttackSimulator sim = new AttackSimulator();
    System.out.println("Creating BGPGraph");
    BGPGraph bgpGraph = sim.createBgpGraph();
    Set<Integer> allAsesNums = bgpGraph.get_all_ases().stream().map(a -> a.number()).collect(Collectors.toSet());
    System.out.println("There are " + allAsesNums.size() + " ASes in the BGP graph");
    Map<Integer, Long> reflectors = sim.getPotentialReflectors();
    System.out.println("There are " + reflectors.size() + " reflectors from shodan data");
    if(!isMiniExample){
      reflectors.entrySet().removeIf(e -> !allAsesNums.contains(e.getKey()));
    }
    System.out.println("There are " + reflectors.size() + " reflectors after filtering");
    Set<Integer> tier1s = sim.getTier1Ases(bgpGraph);
    System.out.println("There are " + tier1s.size() + " tier 1 ASes");

    Map<Integer, Pair<AS, Long>> reflectorToNumIps = bgpGraph.get_all_ases().stream()
        .filter(as -> reflectors.containsKey(as.number()))
        .collect(Collectors.toMap(AS::number, as -> Pair.of(as, reflectors.get(as.number()))));

    GraphProcessor gp = new GraphProcessor(bgpGraph, RIR.ALL, RIR.ALL, false);
    Set<Integer> destinations = reflectors.keySet();
    System.out.println("Starting calculation of all ASes to reflectors");
    StopWatch watch = new StopWatch();
    watch.start();
    // destinations.addAll(tier1s);
    Set<Integer> set = new HashSet<>();
    set.addAll(destinations);
    set.addAll(tier1s);
    Table<Integer, Integer, Route> paths = gp.pathsToDestinations(set);
    watch.stop();
    // System.out.println("Time Elapsed: " + watch.getTime(TimeUnit.SECONDS)); // Prints: Time Elapsed: 2501
    // System.out.println(paths.columnKeySet().size());
    // System.out.println("XX");
    // for (Integer integer : paths.columnKeySet()) {
    //   System.out.println(integer + ": " + paths.column(integer).size());
    // }
    // System.out.println("XX");

    Multiset<Integer> bookStore = HashMultiset.create();
    for(int attacker : allAsesNums) {
      for (int reflector : reflectors.keySet()) {
        for(int victim: sim.getVictims()){

          boolean contains = tripletsCalculates.computeIfAbsent(attacker, k -> new HashMap<>())
              .computeIfAbsent(reflector, k -> new HashSet<>()).contains(victim);
          if(contains){
            String tripletStr = "( attacker = " +attacker +" , reflector = " + reflector +" , victim = " + victim + " )";
            System.out.println("XXXXX");
            System.out.println(tripletStr);
            System.out.println("XXXXX");
            throw new Exception();
          }

          int i = checkTriplet(attacker, reflector, victim, paths, tier1s, gp, bgpGraph);
          if(i<=0) bookStore.add(i);
          bookStore.add(1);
        }
      }
    }
    System.out.println(numSuccess);
    System.out.println(numFailed);
    System.out.println(numFailed * 100 / (float) (numSuccess + numFailed));
    bookStore.elementSet().forEach(i -> System.out.println(i + " : " + bookStore.count(i)));
    System.out.println(bookStore.count(1) * 100 / (float) (bookStore.count(1) + bookStore.count(0)));
    //
    //
    // for(int potentialAttacker : allAsesNums){
    //   for(int reflector : reflectors.keySet()){
    //     if(potentialAttacker == reflector) continue;
    //     Route asToRefRoute = paths.get(potentialAttacker, reflector);
    //     List<Integer> asPath = asToRefRoute.get_as_list();
    //     for (int as : asPath){
    //       if(!tier1s.contains(as)) continue;
    //       for (int v : sim.getVictims()){
    //         if(potentialAttacker == as){
    //           System.out.println(potentialAttacker + " MITIGATED  " + v + "to " + reflector);
    //           continue;
    //         }
    //         Route route = paths.get(v, as);
    //         int beforeLastHop = route.getBeforeLastHop();
    //         Integer integer = asPath.get(asPath.indexOf(as) + 1);
    //         if(integer == beforeLastHop) {
    //           System.out.println(potentialAttacker + " SPOOFED " + v + " to " + reflector);
    //         } else {
    //           System.out.println(potentialAttacker + " MITIGATED  " + v + "to " + reflector);
    //         }
    //         // beforeLastHop
    //         // System.out.println("y");
    //       }
    //     }
    //   }
    // }


    // List<Integer> victims = sim.getVictims();
    // System.out.println(tier1s.size());
    // HashSet<Integer> tier1set = new HashSet<>(tier1s);
    // System.out.println(tier1set.size());
    // tier1s.sort(Comparator.naturalOrder());
    // System.out.println("XX");
    // tier1s.forEach(i -> System.out.println(i));
  }

  // -1 - irrelevant
  // -2 - missing route attacker to reflector
  // 0 - spoofed (check that AS0 is gone)
  private static int checkTriplet(int attacker, int reflector, int victim, Table<Integer, Integer, Route> paths, Set<Integer> corbServers, GraphProcessor gp, BGPGraph graph) {
    // System.out.println("attacker = " + attacker + " reflector = " + reflector + " victim = " + victim);
    String tripletStr = "( attacker = " +attacker +" , reflector = " + reflector +" , victim = " + victim + " )";
    if(attacker == victim || attacker == reflector) {
      System.out.println("SKIP: Attacker is same as reflector or victim "  + tripletStr);
      return -1;
    }
    boolean mitigated = false;
    int mitigationAs = -1;
    Route attackerToReflectorRoute = paths.get(attacker, reflector);
    if(attackerToReflectorRoute == null){
      System.out.println("SKIP: No path from attacker to reflector. " + tripletStr);
      numFailed++;
      return -2;
    }
    List<Integer> dotsServersOnPathFromAttackerToReflector = attackerToReflectorRoute.get_as_list().stream().filter(corbServers::contains).collect(Collectors.toList());
    if(dotsServersOnPathFromAttackerToReflector.isEmpty()){
      System.out.println("SPOOFED: (NO DOTS ON PATH) " + tripletStr);
      numSuccess++;
      return 0;
    }
    for (Integer dotServer : dotsServersOnPathFromAttackerToReflector) {
      if(dotServer == attacker) continue;
      Route route = paths.get(victim, dotServer);
      if(route == null){
        System.out.println("SKIP: No path from victim to DOTS server. " + tripletStr);
        numFailed++;
        return -2;
      }
      int beforeLastHopVictim = route.getBeforeLastHop();
      int beforeLastHopAttacker = attackerToReflectorRoute.get_as_list().get(attackerToReflectorRoute.get_as_list().indexOf(dotServer) + 1);
      if(beforeLastHopAttacker != beforeLastHopVictim){
        mitigated = true;
        mitigationAs = dotServer;
        break;
      }
    }

    numSuccess++;
    if(!mitigated) {
      System.out.println("SPOOFED: (SAME LAST HOP FOR ALL DOTS SERVERS) " + tripletStr);
      return 0;
    } else {
      System.out.println("MITIGATED: (DIFFERENT LAST HOP FOR " + mitigationAs +  " ) " + tripletStr);
      return mitigationAs;
    }
  }

  private List<Integer> getVictims() throws Exception{
    if(isMiniExample){
      return Arrays.asList(16);
    }
    return Arrays.asList(16509, 36459);
  }

  private Map<Integer, Long> getPotentialReflectors() throws Exception{
    if(isMiniExample){
      Map<Integer, Long> map = new HashMap<>();
      map.put(11, 1L);
      map.put(13, 1L);
      map.put(8, 1L);
      return map;
    }
    Map<Integer, Long> attackers = new ShodanParser(
        "/Users/matans/Downloads/shodan-export.json").parseShodanParser();
    attackers.values().removeIf(v -> v<=10);
    return attackers;
  }

  private BGPGraph createBgpGraph() {
    String relationsFile = isMiniExample ? "/Users/matans/disco/bgp-sim/data/20190801.as-rel.txt" : "/Users/matans/disco/bgp-sim/data/20190801.as-rel.txt.bakk";
    return
        new BGPGraphBuilder()
            .withAdditionalLinks(false)
            .withInfile(relationsFile)
            .withKASRegionsFile(kASRegionsFile)
            .withKASRegionsFile32bit(kASRegionsFile32bit)
            .withKASExtraRelationshipsFile(kASExtraRelationshipsFile)
            .withKASExtraRelationshipsCaidaFile(kASExtraRelationshipsCaidaFile)
            .withKVantagePointsFile(kVantagePointsFile)
            .build();
  }

  private Set<Integer> getTier1Ases(BGPGraph graph){
    return graph.get_all_ases().stream().filter(a -> a.providers().size() == 0).map(AS::number).collect(Collectors.toSet());
  }
}
