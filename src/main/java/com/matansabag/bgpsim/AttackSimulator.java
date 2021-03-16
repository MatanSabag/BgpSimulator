package com.matansabag.bgpsim;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.matansabag.bgpsim.AS.RIR;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.StopWatch;

public class AttackSimulator {
  private final Set<Integer> allAsns;
  private final GraphProcessor gp;
  private int numSuccess = 0;
  private int numFailed = 0;

  AttackSimulator(BGPGraph graph){
    this.allAsns = graph.get_all_ases().stream().map(AS::number).collect(Collectors.toSet());
    this.gp = new GraphProcessor(graph, RIR.ALL, RIR.ALL, false);
  }

  public void simulate(Map<Integer, Long> reflectors, Set<Integer> dotsServers, Set<Integer> victims) {
    System.out.println("Starting attack simulation");
    StopWatch watch = new StopWatch();
    watch.start();
    Preconditions.checkArgument(allAsns.containsAll(victims), "Not all victims are in the graph. Aborting simulation");
    filterExistingReflectors(reflectors);
    System.out.println("Calculating paths from all ASes to reflectors and dots-servers");
    // Table<Integer, Integer, Route> paths = gp.pathsToDestinations(Sets.union(reflectors.keySet(), dotsServers));
    Multiset<Integer> integers = simulate0(reflectors.keySet(), victims, dotsServers, gp);
    System.out.println(numSuccess);
    System.out.println(numFailed);
    System.out.println(numFailed * 100 / (float) (numSuccess + numFailed));
    integers.elementSet().forEach(i -> System.out.println(i + " : " + integers.count(i)));
    System.out.println(integers.count(1) * 100 / (float) (integers.count(1) + integers.count(0)));
    watch.stop();
    System.out.println("Simulation took " + watch.getTime(TimeUnit.SECONDS) + " seconds.");
  }

  private Multiset<Integer> simulate0(Set<Integer> reflectors, Set<Integer> victims, Set<Integer> dotsServers, GraphProcessor gp){
    Multiset<Integer> res = HashMultiset.create();
    for (Integer reflector : reflectors) {
      for (Integer attacker : allAsns) {
        for (Integer victim : victims) {
          int tripletRes = checkTriplet(attacker, reflector, victim, gp, dotsServers);
          if (tripletRes <= 0) {
            res.add(tripletRes);
          } else {
            res.add(1);
          }
        }
      }
      // gp.cache.invalidate(reflector); // ? TRY ??
    }
    return res;
  }

  private void filterExistingReflectors(Map<Integer, Long> reflectors) {
    int origSize = reflectors.size();
    reflectors.entrySet().removeIf(e -> !allAsns.contains(e.getKey()));
    int afterFilterSize = reflectors.size();
    if(origSize == afterFilterSize){
      return;
    }
    System.out.println(String.format("There were %d reflectors originally. And %d were left after filtering only reflectors that exists in the graph.", origSize, afterFilterSize));
  }



  // -1 - irrelevant
  // -2 - missing route attacker to reflector
  // 0 - spoofed (check that AS0 is gone)
  private int checkTriplet(
      Integer attacker,
      Integer reflector,
      Integer victim,
      GraphProcessor gp,
      Set<Integer> corbServers) {
    // System.out.println("attacker = " + attacker + " reflector = " + reflector + " victim = " +
    // victim);
    // String tripletStr =
    //     "( attacker = " + attacker + " , reflector = " + reflector + " , victim = " + victim + " )";
    if (attacker.equals(victim) || attacker.equals(reflector)) {
      // System.out.println("SKIP: Attacker is same as reflector or victim " + tripletStr);
      return -1;
    }
    boolean mitigated = false;
    Integer mitigationAs = -1;
    Route attackerToReflectorRoute = gp.getPath(attacker, reflector);
    if (attackerToReflectorRoute == null) {
      // System.out.println("SKIP: No path from attacker to reflector. " + tripletStr);
      numFailed++;
      return -2;
    }
    List<Integer> dotsServersOnPathFromAttackerToReflector =
        attackerToReflectorRoute.get_as_list().stream()
            .filter(corbServers::contains)
            .collect(Collectors.toList());
    if (dotsServersOnPathFromAttackerToReflector.isEmpty()) {
      // System.out.println("SPOOFED: (NO DOTS ON PATH) " + tripletStr);
      numSuccess++;
      return 0;
    }
    for (Integer dotServer : dotsServersOnPathFromAttackerToReflector) {
      if (dotServer.equals(attacker)) continue;
      Route route = gp.getPath(victim, dotServer);
      if (route == null) {
        // System.out.println("SKIP: No path from victim to DOTS server. " + tripletStr);
        numFailed++;
        return -2;
      }
      int beforeLastHopVictim = route.getBeforeLastHop();
      int beforeLastHopAttacker =
          attackerToReflectorRoute
              .get_as_list()
              .get(attackerToReflectorRoute.get_as_list().indexOf(dotServer) + 1);
      if (beforeLastHopAttacker != beforeLastHopVictim) {
        mitigated = true;
        mitigationAs = dotServer;
        break;
      }
    }

    numSuccess++;
    if (!mitigated) {
      // System.out.println("SPOOFED: (SAME LAST HOP FOR ALL DOTS SERVERS) " + tripletStr);
      return 0;
    } else {
      // System.out.println("MITIGATED: (DIFFERENT LAST HOP FOR " + mitigationAs + " ) " + tripletStr);
      return mitigationAs;
    }
  }


}
