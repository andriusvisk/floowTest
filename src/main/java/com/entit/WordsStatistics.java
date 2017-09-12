package com.entit;

import java.util.Map;

/**
 * Created by andrius on 09/09/2017.
 */
public class WordsStatistics extends EntityBase {

    private String myId;

    private Long fromLineNbr;
    private Long toLineNbr;

    private Map<String, Long> counts;

    public WordsStatistics(){

    }

    public WordsStatistics(String myId, Long fromLineNbr, Long toLineNbr, Map<String, Long> counts) {
        this.myId = myId;
        this.fromLineNbr = fromLineNbr;
        this.toLineNbr = toLineNbr;
        this.counts = counts;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public Long getFromLineNbr() {
        return fromLineNbr;
    }

    public void setFromLineNbr(Long fromLineNbr) {
        this.fromLineNbr = fromLineNbr;
    }

    public Long getToLineNbr() {
        return toLineNbr;
    }

    public void setToLineNbr(Long toLineNbr) {
        this.toLineNbr = toLineNbr;
    }

    public Map<String, Long> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Long> counts) {
        this.counts = counts;
    }

}
