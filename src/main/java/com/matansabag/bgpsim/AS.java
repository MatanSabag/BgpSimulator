package com.matansabag.bgpsim;

import java.util.HashSet;
import java.util.Set;

public class AS {
  enum AS_STATE {
    AS_LEGACY,
    AS_MALICIOUS,
    AS_ADOPTER
  };

  enum AS_DISCARD_OPTATTR {
    PASS,
    DISCARD_OPTATTR,
    DISCARD_PREFIX
  };

  enum RIR {
    ALL,
    AFRINIC,
    APNIC,
    ARIN,
    LACNIC,
    RIPE_NCC,
    LARGE_ISPS,
    MEDIUM_ISPS,
    SMALL_ISPS,
    STUBS,
    BIGGEST_CPS,
    OTHER
  };

  private static final int LARGE_CUSTOMERS = 250;
  private static final int MEDIUM_CUSTOMERS = 25;

  private final int number;
  private final Set<Integer> customers = new HashSet<>();
  private final Set<Integer> peers = new HashSet<>();
  private final Set<Integer> providers = new HashSet<>();
  private final Set<Integer> neighbours = new HashSet<>();

  private RIR region;
  private AS_STATE state;
  AS_DISCARD_OPTATTR optattr_processing_;

  public AS(int number) {
    this.number = number;
    this.region = RIR.OTHER;
    this.state = AS_STATE.AS_LEGACY;
  }

  Set<Integer> customers() {
    return customers;
  };

  Set<Integer> peers() {
    return peers;
  };

  Set<Integer> providers() {
    return providers;
  };

  Set<Integer> neighbours() {
    return neighbours;
  };

  int number() {
    return number;
  }

  void AddCustomer(int as_number) {
    if (isNeighbour(as_number)) {
      return;
    }
    customers.add(as_number);
    neighbours.add(as_number);
  }

  void AddPeer(int as_number) {
    if (isNeighbour(as_number)) {
      return;
    }
    peers.add(as_number);
    neighbours.add(as_number);
  }

  void AddProvider(int as_number) {
    if (isNeighbour(as_number)) {
      return;
    }
    providers.add(as_number);
    neighbours.add(as_number);
  }

  public boolean isNeighbour(int otherAs) {
    return neighbours.contains(otherAs);
  }

  boolean isInRegion(RIR region) {
    if (region == RIR.ALL) {
      return true;
    } else if (region == RIR.OTHER) {
      return false;
    }
    return this.region.equals(region);
  }

  void setState(AS_STATE newState) {
    this.state = newState;
  }

  boolean malicious() {
    return state == AS_STATE.AS_MALICIOUS;
  }

  boolean legacy() {
    return state == AS_STATE.AS_LEGACY;
  }

  boolean adopter() {
    return state == AS_STATE.AS_ADOPTER;
  }

  void set_optattr_processing(AS_DISCARD_OPTATTR discard) {
    optattr_processing_ = discard;
  }

  boolean optattr_discard_attr() {
    return optattr_processing_ == AS_DISCARD_OPTATTR.DISCARD_OPTATTR;
  }

  boolean optattr_discard_prefix() {
    return optattr_processing_ == AS_DISCARD_OPTATTR.DISCARD_PREFIX;
  }

  RIR region() {
    return region;
  }

  void set_region(RIR region) {
    this.region = region;
  }

  static boolean is_geographical_region(RIR region) {
    return (region == RIR.AFRINIC)
        || (region == RIR.APNIC)
        || (region == RIR.ARIN)
        || (region == RIR.LACNIC)
        || (region == RIR.RIPE_NCC);
  }

  static boolean is_size_region(RIR region) {
    return is_categories_region(region) || region == RIR.BIGGEST_CPS;
  }

  static boolean is_categories_region(RIR region) {
    return (region == RIR.LARGE_ISPS)
        || (region == RIR.MEDIUM_ISPS)
        || (region == RIR.SMALL_ISPS)
        || (region == RIR.STUBS);
  }

  static String region_to_txt(RIR region) {
    if (region == RIR.AFRINIC) {
      return "Africa";
    } else if (region == RIR.APNIC) {
      return "Asia";
    } else if (region == RIR.ARIN) {
      return "North America";
    } else if (region == RIR.LACNIC) {
      return "South America";
    } else if (region == RIR.RIPE_NCC) {
      return "Europe";
    } else if (region == RIR.ALL) {
      return "All";
    } else if (region == RIR.LARGE_ISPS) {
      return "Large ISPs";
    } else if (region == RIR.MEDIUM_ISPS) {
      return "Medium ISPs";
    } else if (region == RIR.SMALL_ISPS) {
      return "Small ISPs";
    } else if (region == RIR.STUBS) {
      return "Stubs";
    } else if (region == RIR.BIGGEST_CPS) {
      return "BIGGEST_CPS";
    } else {
      return "other";
    }
  }

  static boolean is_cps_region(RIR region) {
    return region == RIR.BIGGEST_CPS;
  }
}
