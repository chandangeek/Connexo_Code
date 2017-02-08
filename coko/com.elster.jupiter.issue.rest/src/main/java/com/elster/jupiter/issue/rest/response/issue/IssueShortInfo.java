/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.issue;

public class IssueShortInfo {
    public long id;
    public String title;
    public long version;

    public IssueShortInfo() {
    }

    public IssueShortInfo(long id) {
        this.id = id;
    }

    public IssueShortInfo(long id, String title) {
        this.id = id;
        this.title = title;
    }
}
