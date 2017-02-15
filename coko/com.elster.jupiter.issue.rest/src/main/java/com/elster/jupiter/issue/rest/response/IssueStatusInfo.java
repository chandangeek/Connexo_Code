/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueStatus;

public class IssueStatusInfo {
    public String id;
    public String name;
    public boolean allowForClosing;

    public IssueStatusInfo(IssueStatus status) {
        if (status != null) {
            this.id = status.getKey();
            this.name = status.getName();
            this.allowForClosing = status.isHistorical();
        }
    }

    public IssueStatusInfo() {}
}
