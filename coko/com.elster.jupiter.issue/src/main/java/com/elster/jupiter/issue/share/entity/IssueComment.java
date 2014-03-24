package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

public class IssueComment extends Entity {
    private String comment;
    private long issueId;
    private Reference<User> user = ValueReference.absent();

    public IssueComment() {
        super();
    }

    public IssueComment(long issueId, String comment, User author) {
        this.issueId = issueId;
        this.comment = comment;
        this.user.set(author);
    }

    public User getUser() {
        return this.user.orNull();
    }

    public long getIssueId() {
        return issueId;
    }

    public void setIssueId(long issueId) {
        this.issueId = issueId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
