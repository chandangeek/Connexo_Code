/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionFailInfo {
    private String reason;
    private final List<IssueShortInfo> issues = new ArrayList<>();

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<IssueShortInfo> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public void add(IssueShortInfo issue) {
        this.issues.add(issue);
    }

    public void setIssues(List<IssueShortInfo> issues) {
        this.issues.clear();
        this.issues.addAll(issues);
    }

}