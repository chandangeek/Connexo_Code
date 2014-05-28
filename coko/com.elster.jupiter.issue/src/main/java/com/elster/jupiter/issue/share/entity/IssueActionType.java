package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.share.cep.IssueAction;


public interface IssueActionType extends Entity {

    String getClassName();
    void setClassName(String className);

    IssueType getIssueType();
    void setIssueType(IssueType type);

    String getFactoryId();
    void setFactoryId(String id);
    
    IssueAction createIssueAction();
}
