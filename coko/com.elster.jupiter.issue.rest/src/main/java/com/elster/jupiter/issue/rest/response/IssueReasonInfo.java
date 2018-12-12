/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;


import com.elster.jupiter.issue.share.entity.IssueReason;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueReasonInfo {
    public String id;
    public String name;

    public IssueReasonInfo(IssueReason reason) {
        this.id = reason.getKey();
        this.name = reason.getName();
    }

    public IssueReasonInfo() {}
}
