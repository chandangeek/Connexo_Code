package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueForAssign {
    long getId();
    long getVersion();
    String getOutageRegion();
    String getCustomer();
    String getReason();

    boolean isProcessed();
    void assignTo(Long userId, Long workGroupId);
    void assignTo(String type, long assigneeId);

}
