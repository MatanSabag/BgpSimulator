package com.matansabag.bgpsim;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class ShodanParser {

  private final String path;

  ShodanParser(String shodanDataPath){
    this.path = shodanDataPath;
  }

  LinkedHashMap<Integer, Long> parseShodanParser() throws Exception{
    LineIterator it = FileUtils.lineIterator(Paths.get(path).toFile(), "UTF-8");
    List<Integer> asns = new ArrayList<>();
    int i = 0;
    while (it.hasNext()){
      String line = it.nextLine();
      try {
        if(!line.contains("\"asn\"")){
          // System.out.println("not contains asn");
          continue;
        }
        String as = parseAs(line);
        asns.add(Integer.parseInt(as));
      } catch (Exception e){
        System.out.println("ERROR: " + line);
        i++;
      }
    }
    Map<Integer, Long> count = asns.stream()
        .collect(groupingBy(p -> p, counting()));
    return count.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .collect(Collectors
            .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  private static String parseAs(String line) {
    return line.substring(line.indexOf("\"asn\"")).split(":")[1].split(",")[0].replace("AS", "")
        .replace("\"", "").trim();
  }

  public static void main(String[] args)  throws Exception{
    LineIterator it = FileUtils.lineIterator(Paths.get("/Users/matans/Downloads/shodan-export.json").toFile(), "UTF-8");
    int x = 0;
    int i = 0;
    List<Integer> asns = new ArrayList<>();
    while (it.hasNext()){
      String s = it.nextLine();
      try {
        if(!s.contains("\"asn\"")){
          System.out.println("not contains asn");
          continue;
        }
        x = 1;
        String as1 = parseAs(s);

        asns.add(Integer.parseInt(as1));
      } catch (Exception e){
        System.out.println("ERROR: " + x + s);
        i++;
      }
    }
    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    System.out.println(asns.size());
    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    Set<Integer> collect = new HashSet<>(asns);
    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    System.out.println(collect.size());
    System.out.println(collect.contains(16276));

    System.out.println(collect.contains(14061));
    System.out.println(collect.contains(7684));
    System.out.println(collect.contains(9370));
    System.out.println(collect.contains(12876));
    System.out.println(collect.contains(9371));

    System.out.println(collect.contains(16509));
    System.out.println(collect.contains(24940));
    System.out.println(collect.contains(36351));
    System.out.println(collect.contains(20473));
    System.out.println(collect.contains(49981));
    System.out.println(collect.contains(51167));
    System.out.println(collect.contains(33070));
    System.out.println(collect.contains(19994));
    System.out.println(collect.contains(14618));

    // System.out.println(collect);
    Map<Integer, Long> count = asns.stream()
        .collect(groupingBy(p -> p, counting()));
    Ordering<Integer> integerOrdering = Ordering.natural().onResultOf(Functions.forMap(count));

    LinkedHashMap<Integer, Long> collect1 = count.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .collect(Collectors
            .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    List<Integer> integers = Arrays
        .asList(14618, 16276, 7684, 9370, 12876, 9371, 16509, 24940, 36351, 20473, 49981, 51167,
            33070, 19994, 14618);
    integers.forEach(j -> System.out.println(collect1.get(j)));
    int ones = 0;
    int lessThan10 = 0;
    int lessThan100 = 0;
    int lessThan1000 = 0;
    int over1000 = 0;
    for (Map.Entry<Integer, Long> e : collect1.entrySet()) {
      int val = e.getValue().intValue();
      if(val == 1){
        ones++;
      } else if(val <= 10){
        lessThan10++;
      } else if(val <= 100){
        lessThan100++;
      } else if(val <= 1000){
        lessThan1000++;
      } else {
        over1000++;
      }
    }
    System.out.println("ones = " + ones);
    System.out.println("lessThan10 = " + lessThan10);
    System.out.println("lessThan100 = " + lessThan100);
    System.out.println("lessThan1000 = " + lessThan1000);
    System.out.println("over1000 = " + over1000);

    LinkedHashMap<Integer, Long> integerLongLinkedHashMap = new ShodanParser(
        "/Users/matans/Downloads/shodan-export.json").parseShodanParser();

    System.out.println("X");

  }
}
