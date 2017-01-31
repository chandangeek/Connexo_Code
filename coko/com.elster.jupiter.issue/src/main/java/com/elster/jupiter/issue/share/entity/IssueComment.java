/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.users.User;

@ProviderType
public interface IssueComment extends Entity {

    User getUser();

    void setUser(User user);

    long getIssueId();

    void setIssueId(long issueId);

    String getComment();

    void setComment(String comment);
}
