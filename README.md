# BGPSimulator

## Citation

If you find this tool useful in your research, please consider citing our papers:

```
@INPROCEEDINGS{demo_paper,
    AUTHOR="Matan Sabag and Anat Bremler",
    TITLE="Preventing the Flood: Incentive-Based Collaborative Mitigation for DRDoS Attacks",
    BOOKTITLE="IFIP 2022",
    YEAR="2022",
    PUBLISHER = "IFIP"
} 
```


### What is BGPSimulator and why should you use it?

BGP simulator is an application that provides several features based on BGP information. 

First, given raw data of BGP Graph from CAIDA, BGP Simulator is able to parse it and generate an in-memory, object-oriented representation so users may process it as they desire.

In addition, BGP Simulator is able to calculate BGP routes between ASes according to Gao's valley-free property, with shorter routes being used as a tie-breaker. 

The BGP Simulator is written in Java and uses efficient techniques such as parallelism and caching. However, bear in mind that as more routes are calculated, the more memory will be consumed, so a full graph calculation ( O(N^2) where N is ~60K) is typically not practicable to store locally in memory.

BGP Simulator is heavily influenced by [disco](https://github.com/yossigi/disco) tool.

This reserach was done as part of the <strong>DEEPNESS Lab</strong>
<br>Come and visit us! https://deepness-lab.org/

<img src="images/RGB_Efi_Arazi_School_Of_Computer_Science_EN (2).jpg" width="40%" height="40%" style="object-fit:scale-down;">
<br><img src="images/deepness_lab.PNG" width="40%" height="40%" style="object-fit:scale-down;">
