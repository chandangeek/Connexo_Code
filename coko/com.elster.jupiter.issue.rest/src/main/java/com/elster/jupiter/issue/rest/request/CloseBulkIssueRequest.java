package com.elster.jupiter.issue.rest.request;

/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.users.User;

import java.util.List;
import java.util.stream.Collectors;

public class CloseBulkIssueRequest {

    private List<Long> issueIds;
    private String issueStatus;
    private String comment;
    private String user;

    private CloseBulkIssueRequest(List<Long> issueIds, String issueStatus, String comment, String user) {
        this.issueIds = issueIds;
        this.issueStatus = issueStatus;
        this.comment = comment;
        this.user = user;
    }


    public static CloseBulkIssueRequest getRequest(List<OpenIssue> issues, String issueStatus, String comment, User user) {
        return new CloseBulkIssueRequest(issues.stream().map(Issue::getId).collect(Collectors.toList()), issueStatus, comment, user.getName());
    }

    public List<Long> getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(List<Long> issueIds) {
        this.issueIds = issueIds;
    }

    public String getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
