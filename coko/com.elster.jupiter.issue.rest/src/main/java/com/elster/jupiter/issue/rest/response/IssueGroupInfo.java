/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueGroup;

public class IssueGroupInfo {
    public Object id;
    public String description;
    public long number;

    public IssueGroupInfo(IssueGroup entity) {
        if (entity != null) {
            this.id = entity.getGroupKey();
            this.description = entity.getGroupName();
            this.number = entity.getCount();
        }
    }

    public IssueGroupInfo(Object id, String description, long number) {
        this.id = id;
        this.description = description;
        this.number = number;
    }
}
