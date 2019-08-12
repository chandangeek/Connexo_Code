/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import java.util.List;

public class BulkAddIssueRequest {
    private List<AddIssueRequest> issues;

    public List<AddIssueRequest> getIssues() {
        return issues;
    }

    public void setIssues(List<AddIssueRequest> issues) {
        this.issues = issues;
    }
}
