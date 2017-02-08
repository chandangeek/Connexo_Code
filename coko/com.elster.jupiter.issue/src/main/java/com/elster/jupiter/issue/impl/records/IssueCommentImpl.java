/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class IssueCommentImpl extends EntityImpl implements IssueComment{
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, message = "{" + MessageSeeds.Keys.ISSUE_COMMENT_COMMENT_SIZE + "}")
    private String comment;
    @Min(value = 1, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private long issueId;
    private Reference<User> user = ValueReference.absent();

    @Inject
    public IssueCommentImpl(DataModel dataModel) {
        super(dataModel);
    }

    public IssueCommentImpl init(long issueId, String comment, User author){
        setIssueId(issueId);
        setComment(comment);
        setUser(author);
        return this;
    }

    @Override
    public User getUser() {
        return user.orNull();
    }

    @Override
    public void setUser(User user) {
        this.user.set(user);
    }

    @Override
    public long getIssueId() {
        return issueId;
    }

    @Override
    public void setIssueId(long issueId) {
        this.issueId = issueId;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }
}
