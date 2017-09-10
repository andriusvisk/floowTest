package com.entit;

/**
 * Created by andrius on 10/09/2017.
 */
public class ElectionQueue extends EntityBase {

    private String runnerUUID;
    private Long priority;

    public ElectionQueue(String runnerUUID, Long priority) {
        this.runnerUUID = runnerUUID;
        this.priority = priority;
    }

    public String getRunnerUUID() {
        return runnerUUID;
    }

    public void setRunnerUUID(String runnerUUID) {
        this.runnerUUID = runnerUUID;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }
}
