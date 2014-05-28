package com.elster.jupiter.issue.share.cep;

public interface IssueActionFactory {

    String getId();

    IssueAction createIssueAction(String issueActionClassName);
}
