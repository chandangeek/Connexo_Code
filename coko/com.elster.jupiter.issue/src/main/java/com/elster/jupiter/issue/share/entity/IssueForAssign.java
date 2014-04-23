package com.elster.jupiter.issue.share.entity;

public interface IssueForAssign {
    long getId();
    long getVersion();
    String getOutageRegion();
    String getCustomer();
    String getReason();

    boolean isProcessed();
    void assignTo(String type, long assigneeId);

}
