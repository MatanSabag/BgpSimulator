package com.matansabag.bgpsim;

import static com.matansabag.bgpsim.AS.RIR.ALL;
import static com.matansabag.bgpsim.AS.RIR.OTHER;

import com.matansabag.bgpsim.AS.AS_DISCARD_OPTATTR;
import com.matansabag.bgpsim.AS.AS_STATE;
import com.matansabag.bgpsim.AS.RIR;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class BGPGraph {
  // Constants
  private static final int LARGE_CUSTOMERS = 250;
  private static final int MEDIUM_CUSTOMERS = 25;
  // public static Map<Integer, AS> kPlainGraph; Needed for deploy()
  // type of link in the graph
  enum Link_Type {
    LINK_NONE,
    LINK_TO_CUSTOMER,
    LINK_TO_PEER,
    LINK_TO_PROVIDER;
  }
  // members
  private final List<Integer> vantage_points = new ArrayList<>();
  private final Map<Integer, AS> graph = new ConcurrentHashMap<>();
  private final Map<RIR, Set<Integer>> regions_ = new ConcurrentHashMap<>();

  public Map<Integer, AS> get_plain() {
    return graph;
  }

  private BGPGraph(
      boolean additionalLinks,
      String infile,
      String kASRegionsFile,
      String kASRegionsFile32bit,
      String kASExtraRelationshipsFile,
      String kASExtraRelationshipsCaidaFile,
      String kVantagePointsFile) {
    regions_.put(ALL, new HashSet<>());
    try {
      LineIterator it = FileUtils.lineIterator(Paths.get(infile).toFile(), "UTF-8");
      while (it.hasNext()) {
        String line = it.nextLine();
        if (!line.startsWith("#") && line.length() > 0) {
          List<String> tokens = new ArrayList<>();
          SplitToRelationship(line, tokens);
          int as_a = Integer.parseInt(tokens.get(0));
          int as_b = Integer.parseInt(tokens.get(1));
          int rel = Integer.parseInt(tokens.get(2));
          // allocate new ASes.
          if (!graph.containsKey(as_a)) {
            graph.put(as_a, new AS(as_a));
            regions_.get(ALL).add(as_a);
          }
          if (!graph.containsKey(as_b)) {
            graph.put(as_b, new AS(as_b));
            regions_.get(ALL).add(as_b);
          }
          // peers
          if (rel == 0) {
            graph.get(as_a).AddPeer(as_b);
            graph.get(as_b).AddPeer(as_a);
          } else if (rel == -1) {
            graph.get(as_a).AddCustomer(as_b);
            graph.get(as_b).AddProvider(as_a);
          } else {
            throw new IllegalStateException("Unknown Relationship");
          }
        }
      }
      it.close();
    } catch (IOException e) {
      throw new IllegalStateException("No relations file 1");
    }

    if (additionalLinks) {
      create_additional_caida_links(kASExtraRelationshipsCaidaFile);
    }
    parse_regions(kASRegionsFile, kASRegionsFile32bit);
    // set_size_regions();
    set_biggest_cps();
    read_vantage_points(kVantagePointsFile);
  }

  private void count_ISPs() {
    int counter_NA = 0;
    int counter_EU = 0;
    int count_others = 0;
    for (Map.Entry<Integer, AS> it : graph.entrySet()) {
      int number_of_customers = it.getValue().customers().size();
      if (number_of_customers >= LARGE_CUSTOMERS) {
        if (it.getValue().region() == RIR.ARIN) {
          counter_NA++;
        } else if (it.getValue().region() == RIR.ARIN) {
          counter_EU++;
        } else {
          System.out.println("other regions is " + AS.region_to_txt(it.getValue().region()));
          count_others++;
        }
      }
    }
    System.out.println();
    System.out.println("Large ISPs north America: " + counter_NA);
    System.out.println("Large ISPs Europe: " + counter_EU);
    System.out.println("Large ISP other: " + count_others);
    System.out.println("total: " + counter_NA + counter_EU + count_others);
    System.out.println();
  }

  private void create_additional_caida_links(String kASExtraRelationshipsCaidaFile) {
    try {
      LineIterator it =
          FileUtils.lineIterator(Paths.get(kASExtraRelationshipsCaidaFile).toFile(), "UTF-8");
      while (it.hasNext()) {
        String line = it.nextLine();
        if (!line.startsWith("#") && line.length() > 0) {
          List<String> tokens = new ArrayList<>();
          SplitByToken(line, tokens, ' ');
          int as_a = Integer.parseInt(tokens.get(0));
          int as_b = Integer.parseInt(tokens.get(1));
          // allocate new ASes.
          if (!graph.containsKey(as_a)) {
            graph.put(as_a, new AS(as_a));
            regions_.get(ALL).add(as_a);
          }
          if (!graph.containsKey(as_b)) {
            graph.put(as_a, new AS(as_b));
            regions_.get(ALL).add(as_b);
          }
          // peers
          graph.get(as_a).AddPeer(as_b);
          graph.get(as_b).AddPeer(as_a);
          // as_a(provider) -> as_b (customer)
        }
      }
      it.close();
    } catch (IOException e) {
      throw new IllegalStateException("No relations file 2");
    }
  }

  // public BGPGraph(bool additional_links, const string& infile = kASRelationshipsFile){}
  //
  // vector<int> get_all_ases(AS::RIR region) const;

  //
  // map<int, shared_ptr<AS> >* get_plain() const { return graph_.get(); }
  //

  //
  //

  //

  // void get_size_region(vector<int> &ases, AS::RIR region) const;
  //
  // std::vector<int> vantage_points;

  private void create_additional_links(String kASExtraRelationshipsFile) {
    // TODO
  }

  // extern double optattr_prefixdiscard_prob; FIXME
  // extern double optattr_attrdiscard_prob; FIXME

  void deploy(RIR region, int number_top_ases, double adoption_prob) {
    // TODO
    // FIXME
    //
    //     //srand(static_cast<int>(time(NULL)));
    //
    //     Random random = new Random(1453151544);//srand(1453151544);
    //     System.out.println("seed is: ???" ); // cout << "seed is: " <<
    // static_cast<int>(time(NULL)) << endl;
    //
    //     //number_top_ases = static_cast<int>(ceil(number_top_ases / adoption_prob));
    //     kPlainGraph = graph_.get();
    //     kPlainGraph = graph;
    //
    //     List<Integer>  all_ases = get_all_ases(region);
    //     SortedASVector sortedASVector = new SortedASVector(all_ases,
    // COMPARISON_METHOD.BY_CUSTOMERS, kPlainGraph);
    //
    //     int count = 0;
    //     for (int i = 0; i< all_ases.size() && count < number_top_ases; i++){
    //       // if (static_cast<double>(rand()) / static_cast<double>(RAND_MAX) <= adoption_prob) {
    // // FIXME
    //       //   get_mutable(all_ases[i])->set_state(AS::AS_ADOPTER);
    //       //   ++count;
    //       // }
    //     }
    //
    //     Random random2 = new Random(1453151544);
    //     for (Integer as : all_ases) {
    // //       if (static_cast<double>(rand()) / static_cast<double>(RAND_MAX) <=
    // optattr_attrdiscard_prob*(1+optattr_prefixdiscard_prob)) { //FIXME
    // // //	   cout << "Adding AS with attrdiscard.\n";
    // //         get_mutable(all_ases[i])->set_optattr_processing(AS::DISCARD_OPTATTR);
    // //       }
    // //       if (static_cast<double>(rand()) / static_cast<double>(RAND_MAX) <=
    // optattr_prefixdiscard_prob) { //FIXME
    // //         get_mutable(all_ases[i])->set_optattr_processing(AS::DISCARD_PREFIX);
    // // //	   cout << "Adding AS with prefixdiscard.\n";
    // //       }
    //     }
    //
    //     int count = 0;
    //     for (size_t i = 0; i < all_ases.size() && count < number_top_ases ; i++) {
    //         /*
    //         if( number_top_ases == 10 ) {
    //             if( i < 10 ){
    //                 continue;
    //             }
    //         }
    //         if( number_top_ases == 60 ) {
    //             if( i >= 50 && i < 60 ){
    //                 continue;
    //             }
    //         }
    //         if( number_top_ases == 90 ) {
    //             if( i >= 80 && i < 90 ){
    //                 continue;
    //             }
    //         }
    //         */
    //       if (static_cast<double>(rand()) / static_cast<double>(RAND_MAX) <= adoption_prob) {
    //         get_mutable(all_ases[i])->set_state(AS::AS_ADOPTER);
    //         ++count;
    //       }
    //     }
    //
    //     srand(1453151544);
    //     for (size_t i = number_top_ases; i < all_ases.size(); i++) {
    //       if (static_cast<double>(rand()) / static_cast<double>(RAND_MAX) <=
    // optattr_attrdiscard_prob*(1+optattr_prefixdiscard_prob)) {
    // //	   cout << "Adding AS with attrdiscard.\n";
    //         get_mutable(all_ases[i])->set_optattr_processing(AS::DISCARD_OPTATTR);
    //       }
    //
    //       if (static_cast<double>(rand()) / static_cast<double>(RAND_MAX) <=
    // optattr_prefixdiscard_prob) {
    //         get_mutable(all_ases[i])->set_optattr_processing(AS::DISCARD_PREFIX);
    // //	   cout << "Adding AS with prefixdiscard.\n";
    //       }
    //     }
    //     if ( count < number_top_ases ) {
    //       System.out.println(String.format("NOT ENOUGH ADOPTERS: only %d out of %d", count,
    // number_top_ases));
    //     }
  }

  void clear_all_deployments() {
    List<Integer> all_ases = get_all_ases(ALL);
    int deployed_ases = 0;
    for (Integer all_ase : all_ases) {
      if (get_mutable(all_ase).adopter()) {
        deployed_ases++;
      }
      get_mutable(all_ase).setState(AS_STATE.AS_LEGACY);
      get_mutable(all_ase).set_optattr_processing(AS_DISCARD_OPTATTR.PASS);
    }
    System.out.println("deployed ASes at claer: " + deployed_ases);
  }

  static void SplitToRelationship(String s, List<String> elems) {
    SplitByToken(s, elems, '|');
  }

  static void SplitByToken(String s, List<String> elems, char token) {
    elems.addAll(Arrays.asList(StringUtils.split(s, token)));
  }

  void set_biggest_cps() {
    regions_.put(RIR.BIGGEST_CPS, new HashSet<>());
    regions_.get(RIR.BIGGEST_CPS).add(15169); // google
    regions_.get(RIR.BIGGEST_CPS).add(22822); // limelight
    regions_.get(RIR.BIGGEST_CPS).add(20940); // akamai
    regions_.get(RIR.BIGGEST_CPS).add(8075); // microsoft
    regions_.get(RIR.BIGGEST_CPS).add(10310); // yahoo
    regions_.get(RIR.BIGGEST_CPS).add(16265); // leaseweb
    regions_.get(RIR.BIGGEST_CPS).add(15133); // edgecast
    regions_.get(RIR.BIGGEST_CPS).add(16509); // amazon
    regions_.get(RIR.BIGGEST_CPS).add(32934); // facebook
    regions_.get(RIR.BIGGEST_CPS).add(2906); // netflix
    regions_.get(RIR.BIGGEST_CPS).add(4837); // qq
    regions_.get(RIR.BIGGEST_CPS).add(13414); // twitter
    regions_.get(RIR.BIGGEST_CPS).add(40428); // pandora
    regions_.get(RIR.BIGGEST_CPS).add(14907); // wikipedia
    regions_.get(RIR.BIGGEST_CPS).add(714); // apple
    regions_.get(RIR.BIGGEST_CPS).add(23286); // hulu
    regions_.get(RIR.BIGGEST_CPS).add(38365); // baidu
  }

  public AS get(int as_number) {
    return get_mutable(as_number);
  }

  AS get_mutable(int as_number) {
    if (!graph.containsKey(as_number)) {
      throw new IllegalArgumentException("Unknown AS number");
    }
    return graph.get(as_number);
  }

  public Collection<AS> get_all_ases() {
    return graph.values();
  }

  public List<Integer> get_all_ases(RIR region) {

    /*
    if( region != AS::ALL ) {
    	parse_regions();
    	set_size_regions();
    }
     */

    List<Integer> as_array = new ArrayList<>();
    Set<Integer> region_ases = null;

    if (regions_.containsKey(region)) {
      region_ases = regions_.get(region);
    }
    if (region_ases == null) {
      return as_array;
    }
    as_array.addAll(region_ases);
    return as_array;
  }

  public RIR get_as_region(Integer as_number) {
    for (Map.Entry<RIR, Set<Integer>> it : regions_.entrySet()) {
      if ((it.getKey() != ALL) && (it.getKey() != OTHER)) {
        if (it.getValue().contains(as_number)) {
          return it.getKey();
        }
      }
    }
    return OTHER;
  }

  public void get_size_region(List<Integer> ases, RIR region) {
    ases.clear();

    int min_customers, max_customers;

    switch (region) {
      case LARGE_ISPS:
        {
          min_customers = LARGE_CUSTOMERS;
          max_customers = graph.size();
          break;
        }
      case MEDIUM_ISPS:
        {
          min_customers = MEDIUM_CUSTOMERS;
          max_customers = LARGE_CUSTOMERS;
          break;
        }
      case SMALL_ISPS:
        {
          min_customers = 1;
          max_customers = MEDIUM_CUSTOMERS;
          break;
        }
      case STUBS:
        {
          min_customers = 0;
          max_customers = 1;
          break;
        }
      default:
        return;
    }

    for (Map.Entry<Integer, AS> asEntry : graph.entrySet()) {
      int customers_number = asEntry.getValue().customers().size();
      if (customers_number >= min_customers && customers_number < max_customers) {
        ases.add(asEntry.getKey());
      }
    }
  }

  void parse_regions(String kASRegionsFile, String kASRegionsFile32bit) {
    parse_regions(kASRegionsFile);
    parse_regions(kASRegionsFile32bit);
    reverse_map_regions();
  }

  void parse_regions(String input_file) {
    try {
      try (LineIterator it = FileUtils.lineIterator(Paths.get(input_file).toFile(), "UTF-8")) {
        while (it.hasNext()) {
          String line = it.nextLine();
          if (line.length() == 0) continue;
          String[] tokens = line.split(",");
          if (tokens.length < 2) continue;
          String as_range = tokens[0];
          String as_region = tokens[1];
          RIR region;
          switch (as_region) {
            case "ARIN":
              region = RIR.ARIN;
              break;
            case "RIPE NCC":
              region = RIR.RIPE_NCC;
              break;
            case "AFRINIC":
              region = RIR.AFRINIC;
              break;
            case "APNIC":
              region = RIR.APNIC;
              break;
            case "LACNIC":
              region = RIR.LACNIC;
              break;
            case "Unallocated":
              region = OTHER;
              break;
            default:
              region = OTHER;
              break;
          }
          try {
            int dash_mark = as_range.indexOf('-');
            if (dash_mark == -1) {
              int as_num = Integer.parseInt(as_range);
              map_as_to_region(region, as_num);
            } else {
              int low = Integer.parseInt(as_range.substring(0, dash_mark));
              int high = Integer.parseInt(as_range.substring(dash_mark + 1));
              for (int i = low; i <= high; i++) {
                map_as_to_region(region, i);
              }
            }
          } catch (Exception e) {
            // System.out.println("x"); // TODO
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void map_as_to_region(RIR region, int as_number) {
    if (!regions_.get(ALL).contains(as_number)) { // TODO: understand this condition better
      return;
    }

    if (!regions_.containsKey(region)) {
      regions_.put(region, new HashSet<>());
    }
    regions_.get(region).add(as_number);
  }

  private void reverse_map_regions() {
    for (Map.Entry<Integer, AS> it : graph.entrySet()) {
      it.getValue().set_region(get_as_region(it.getKey()));
    }
  }

  boolean are_ases_in_same_region(int as_a, int as_b) {
    AS a = get(as_a);
    AS b = get(as_b);
    return a.region().equals(b.region()) && !a.region().equals(OTHER);
  }

  public Link_Type get_link_between_ASes(int as_a, int as_b) {
    if (is_customer_to_provider(as_a, as_b)) {
      return Link_Type.LINK_TO_PROVIDER;
    } else if (are_peers(as_a, as_b)) {
      return Link_Type.LINK_TO_PEER;
    } else if (is_provider_to_customer(as_a, as_b)) {
      return Link_Type.LINK_TO_CUSTOMER;
    } else return Link_Type.LINK_NONE;
  }

  void set_size_regions() {
    regions_.put(RIR.LARGE_ISPS, new HashSet<>());
    regions_.put(RIR.MEDIUM_ISPS, new HashSet<>());
    regions_.put(RIR.SMALL_ISPS, new HashSet<>());
    regions_.put(RIR.STUBS, new HashSet<>());

    for (Map.Entry<Integer, AS> asEntry : graph.entrySet()) {
      int number_of_cutomers = asEntry.getValue().customers().size();
      if (number_of_cutomers >= LARGE_CUSTOMERS) {
        regions_.get(RIR.LARGE_ISPS).add(asEntry.getKey());
      } else if (number_of_cutomers >= MEDIUM_CUSTOMERS) {
        regions_.get(RIR.MEDIUM_ISPS).add(asEntry.getKey());
      } else if (number_of_cutomers >= 1) {
        regions_.get(RIR.SMALL_ISPS).add(asEntry.getKey());
      } else {
        regions_.get(RIR.STUBS).add(asEntry.getKey());
      }
    }
    // TODO add prints?
  }

  private boolean is_customer_to_provider(int customer, int provider) {
    return get(customer).providers().contains(provider);
  }

  private boolean are_peers(int peer_a, int peer_b) {
    return get(peer_a).peers().contains(peer_b);
  }

  private boolean is_provider_to_customer(int provider, int customer) {
    return get(provider).customers().contains(customer);
  }

  private void read_vantage_points(String filename) {
    try {
      Files.readAllLines(Paths.get(filename)).stream()
          .map(Integer::parseInt)
          .filter(n -> n > 0)
          .forEach(n -> vantage_points.add(n));
      System.out.println(
          "Success reading vantage points file. Read " + vantage_points.size() + " records");
    } catch (IOException e) {
      System.out.println("Unable to open vantage points file.");
    }
  }

  //   private:

  //

  // 		const std::string &s, std::vector<std::string> &elems, char token);

  // };

  public static class BGPGraphBuilder {

    private boolean additionalLinks;
    private String infile;
    private String kASRegionsFile;
    private String kASRegionsFile32bit;
    private String kASExtraRelationshipsFile;
    private String kASExtraRelationshipsCaidaFile;
    private String kVantagePointsFile;

    BGPGraphBuilder withAdditionalLinks(boolean additionalLinks) {
      this.additionalLinks = additionalLinks;
      return this;
    }

    BGPGraphBuilder withInfile(String infile) {
      this.infile = infile;
      return this;
    }

    BGPGraphBuilder withKASRegionsFile(String kASRegionsFile) {
      this.kASRegionsFile = kASRegionsFile;
      return this;
    }

    BGPGraphBuilder withKASRegionsFile32bit(String kASRegionsFile32bit) {
      this.kASRegionsFile32bit = kASRegionsFile32bit;
      return this;
    }

    BGPGraphBuilder withKASExtraRelationshipsFile(String kASExtraRelationshipsFile) {
      this.kASExtraRelationshipsFile = kASExtraRelationshipsFile;
      return this;
    }

    BGPGraphBuilder withKASExtraRelationshipsCaidaFile(String kASExtraRelationshipsCaidaFile) {
      this.kASExtraRelationshipsCaidaFile = kASExtraRelationshipsCaidaFile;
      return this;
    }

    BGPGraphBuilder withKVantagePointsFile(String kVantagePointsFile) {
      this.kVantagePointsFile = kVantagePointsFile;
      return this;
    }

    BGPGraph build() {
      return new BGPGraph(
          additionalLinks,
          infile,
          kASRegionsFile,
          kASRegionsFile32bit,
          kASExtraRelationshipsFile,
          kASExtraRelationshipsCaidaFile,
          kVantagePointsFile);
    }
  }
}
