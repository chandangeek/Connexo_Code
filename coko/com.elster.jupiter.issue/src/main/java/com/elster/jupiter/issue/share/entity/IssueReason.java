package com.elster.jupiter.issue.share.entity;

public interface IssueReason extends Entity {

    /**
     * Use the {@link #getKey} method instead
     */
    @Override
    @Deprecated
    long getId();

    String getKey();
    String getName();
    IssueType getIssueType();
}
