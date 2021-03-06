package com.entit;

/**
 * Created by andrius on 10/09/2017.
 */
public class Runner extends EntityBase {

    private String runnerUUID;
    private Long startTimeInMs;
    private Long pingTimeInMs;
    private Integer freeMemPerc;

    public Runner(){

    }

    public Runner(String runnerUUID, Long startTimeInMs) {
        this.runnerUUID = runnerUUID;
        this.startTimeInMs = startTimeInMs;
        this.pingTimeInMs = startTimeInMs;
    }

    public String getRunnerUUID() {
        return runnerUUID;
    }

    public void setRunnerUUID(String runnerUUID) {
        this.runnerUUID = runnerUUID;
    }

    public Long getStartTimeInMs() {
        return startTimeInMs;
    }

    public void setStartTimeInMs(Long startTimeInMs) {
        this.startTimeInMs = startTimeInMs;
    }

    public Long getPingTimeInMs() {
        return pingTimeInMs;
    }

    public void setPingTimeInMs(Long pingTimeInMs) {
        this.pingTimeInMs = pingTimeInMs;
    }

    public Integer getFreeMemPerc() {
        return freeMemPerc;
    }

    public void setFreeMemPerc(Integer freeMemPerc) {
        this.freeMemPerc = freeMemPerc;
    }
}
