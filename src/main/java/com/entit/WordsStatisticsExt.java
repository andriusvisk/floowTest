package com.entit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrius on 09/09/2017.
 */
public class WordsStatisticsExt {

    private Map<Long, Long> intervals;

    public WordsStatisticsExt(Map<String, Long> intervals) {
        this.intervals = new HashMap<>();
        for (String key : intervals.keySet()) {
            Long keyL = new Long(key);
            this.intervals.put(keyL, new Long(intervals.get(key)));
        }
    }

    public Map<Long, Long> getIntervals() {
        return intervals;
    }

    public void setIntervals(Map<Long, Long> intervals) {
        this.intervals = intervals;
    }
}
