/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.issue.share.entity.Issue;

import javax.inject.Inject;

public class IssueShortInfoFactory {

    private final IssueStatusInfoFactory issueStatusInfoFactory;

    @Inject
    public IssueShortInfoFactory(IssueStatusInfoFactory issueStatusInfoFactory) {
        this.issueStatusInfoFactory = issueStatusInfoFactory;
    }
        public IssueShortInfo asInfo(Issue issue){
            IssueShortInfo info = new IssueShortInfo();
            info.id = issue.getId();
            info.title = issue.getTitle();
            info.status = issueStatusInfoFactory.from(issue.getStatus(),null,null);
            info.version = issue.getVersion();
            return info;
        }
}
