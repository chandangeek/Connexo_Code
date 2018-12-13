/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

public interface AssignmentRule extends Entity {

    int getPriority();

    void setPriority(int priority);

    String getDescription();

    void setDescription(String description);

    String getTitle();

    void setTitle(String title);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    byte[] getRuleData();

    void setRuleData(String ruleData);

    String getRuleBody();

    IssueAssignee getAssignee();
}
