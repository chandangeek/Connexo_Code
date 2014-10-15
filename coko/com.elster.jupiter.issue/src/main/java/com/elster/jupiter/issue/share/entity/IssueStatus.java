package com.elster.jupiter.issue.share.entity;


public interface IssueStatus extends Entity {

    public static final String OPEN = "status.open";
    public static final String RESOLVED = "status.resolved";
    public static final String WONT_FIX = "status.wont.fix";
    public static final String IN_PROGRESS = "status.in.progress";

    /**
     * Use the {@link #getKey} method instead
     */
    @Override
    @Deprecated
    long getId();

    String getKey();
    String getName();
    boolean isHistorical();
}
