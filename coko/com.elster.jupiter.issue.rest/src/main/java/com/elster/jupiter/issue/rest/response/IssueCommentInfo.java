/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.users.User;

public class IssueCommentInfo {
    private long id;
    private String comment;
    private UserInfo author;
    private long creationDate;
    private long version;

    public IssueCommentInfo(IssueComment issueComment) {
        if (issueComment != null) {
            this.id = issueComment.getId();
            this.comment = issueComment.getComment();
            this.creationDate = issueComment.getCreateTime().toEpochMilli();
            this.version = issueComment.getVersion();
            this.author = new UserInfo(issueComment.getUser());
        }
    }

    public long getVersion() {
        return version;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getComment() {
        return comment;
    }

    public UserInfo getAuthor() {
        return author;
    }

    public long getId() {
        return id;
    }

    private static class UserInfo {
        private long id;
        private String name;

        UserInfo(User user) {
            this.id = user.getId();
            this.name = user.getName();
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }
    }
}
