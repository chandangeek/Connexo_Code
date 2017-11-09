package com.elster.jupiter.time;

import java.time.Instant;
import java.util.Objects;

public class RelativePeriodUsageInfo {

    private final String task;
    private final String type;
    private final String application;
    private final Instant nextRun;


    public RelativePeriodUsageInfo(String task, String displayName, String app, Instant run){
        this.type = displayName;
        this.task = task;
        this.application = app;
        this.nextRun = run;
    }

    public String getTask() {
        return task;
    }

    public String getType(){ return type; }

    public String getApplication() {
        return application;
    }

    public Instant getNextRun() {
        return nextRun;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;

        if (!(obj instanceof RelativePeriodUsageInfo))
            return false;

        final RelativePeriodUsageInfo other = (RelativePeriodUsageInfo) obj;

        return Objects.equals(task, other.getTask()) &&
                Objects.equals(type,other.getType()) &&
                Objects.equals(application, other.getApplication()) &&
                Objects.equals(nextRun, other.getNextRun());

    }

    @Override
    public int hashCode() {
        return Objects.hash(task, application, nextRun);
    }
}
