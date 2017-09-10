package com.entit;

import java.util.Map;

/**
 * Created by andrius on 10/09/2017.
 */
public class Chunk extends EntityBase {

    private Long seqNbr;
    private Long fromLineNbr;
    private Long toLineNbr;

    private Map<String, Long> statistics;

    private String runnerUUID;
    private Boolean calculated;

    public Chunk(Long fromLineNbr, Long toLineNbr, String runnerUUID) {
        this.fromLineNbr = fromLineNbr;
        this.toLineNbr = toLineNbr;
        this.runnerUUID = runnerUUID;
        this.calculated = false;
    }

    public Long getSeqNbr() {
        return seqNbr;
    }

    public void setSeqNbr(Long seqNbr) {
        this.seqNbr = seqNbr;
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

    public Map<String, Long> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Long> statistics) {
        this.statistics = statistics;
    }

    public String getRunnerUUID() {
        return runnerUUID;
    }

    public void setRunnerUUID(String runnerUUID) {
        this.runnerUUID = runnerUUID;
    }

    public Boolean getCalculated() {
        return calculated;
    }

    public void setCalculated(Boolean calculated) {
        this.calculated = calculated;
    }
}
