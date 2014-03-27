package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

import javax.inject.Inject;

public class IssueComment extends Entity {
    private String comment;
    private long issueId;
    private Reference<User> user = ValueReference.absent();

    @Inject
    public IssueComment(DataModel dataModel) {
        super(dataModel);
    }

    public void init(long issueId, String comment, User author){
        setIssueId(issueId);
        setComment(comment);
        setUser(author);
    }

    public User getUser() {
        return user.orNull();
    }

    public void setUser(User user) {
        this.user.set(user);
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
