package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.share.entity.Issue;

public interface IssueAction {
    public void execute();
    public void setIssue(Issue issue);
}
