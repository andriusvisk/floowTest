package com.entit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrius on 09/09/2017.
 */
public class WordsStatistics extends EntityBase {

    private Map<String, Long> counts;
    private Map<String, Long> intervals;

    public WordsStatistics(){
        counts = new HashMap<>();
        intervals = new HashMap<>();
    }

    public Map<String, Long> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Long> counts) {
        this.counts = counts;
    }

    public Map<String, Long> getIntervals() {
        return intervals;
    }

    public void setIntervals(Map<String, Long> intervals) {
        this.intervals = intervals;
    }
}
