package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.users.User;

public interface IssueComment extends Entity {

    User getUser();

    void setUser(User user);

    long getIssueId();

    void setIssueId(long issueId);

    String getComment();

    void setComment(String comment);
}
