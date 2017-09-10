package com.entit;

/**
 * Created by andrius on 10/09/2017.
 */
public class Runner extends EntityBase {

    private String runnerUUID;
    private String pingTimeInMs;

    public Runner(String runnerUUID, String pingTimeInMs) {
        this.runnerUUID = runnerUUID;
        this.pingTimeInMs = pingTimeInMs;
    }

    public String getRunnerUUID() {
        return runnerUUID;
    }

    public void setRunnerUUID(String runnerUUID) {
        this.runnerUUID = runnerUUID;
    }

    public String getPingTimeInMs() {
        return pingTimeInMs;
    }

    public void setPingTimeInMs(String pingTimeInMs) {
        this.pingTimeInMs = pingTimeInMs;
    }

}
