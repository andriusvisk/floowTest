package com.entit;

/**
 * Created by andrius on 10/09/2017.
 */
public class Global extends EntityBase {

    private Boolean jobIsFinished;

    public Global() {
        this.jobIsFinished = false;
    }

    public Boolean getJobIsFinished() {
        return jobIsFinished;
    }

    public void setJobIsFinished(Boolean jobIsFinished) {
        this.jobIsFinished = jobIsFinished;
    }
}
