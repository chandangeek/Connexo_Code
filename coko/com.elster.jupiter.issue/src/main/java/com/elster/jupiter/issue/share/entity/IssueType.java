package com.elster.jupiter.issue.share.entity;

public interface IssueType extends Entity {
    /**
     * Please use {@link IssueType#getUUID} instead
     * @return
     */
    @Override
    @Deprecated
    long getId();

    String getUUID();
    String getName();
}
