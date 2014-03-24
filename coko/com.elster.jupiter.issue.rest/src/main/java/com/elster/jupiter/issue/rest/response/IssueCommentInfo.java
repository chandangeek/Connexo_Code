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
        this.id = issueComment.getId();
        this.comment = issueComment.getComment();
        this.creationDate = issueComment.getCreateTime().getTime();
        this.version = issueComment.getVersion();
        this.author = new UserInfo(issueComment.getUser());

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
        long id;
        String name;
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
