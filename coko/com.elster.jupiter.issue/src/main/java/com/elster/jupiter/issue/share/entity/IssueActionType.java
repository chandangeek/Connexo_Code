package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.share.cep.IssueAction;

public interface IssueActionType extends Entity {

    String getClassName();
    String getFactoryId();
    IssueType getIssueType();
    IssueReason getIssueReason();
    IssueAction createIssueAction();
}
