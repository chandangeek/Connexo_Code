/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import java.time.Instant;
import java.util.List;

public class CloseAllIssues extends TranslatableServerMicroAction {

    private final IssueService issueService;

    public CloseAllIssues(Thesaurus thesaurus, IssueService issueService) {
        super(thesaurus);
        this.issueService = issueService;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        List<OpenIssue> openIssues = device.getOpenIssues();
        if (!openIssues.isEmpty()) {
            IssueStatus wontFix = this.issueService.findStatus(IssueStatus.WONT_FIX).get();
            openIssues.stream().forEach(baseIssue -> baseIssue.close(wontFix));
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.CLOSE_ALL_ISSUES;
    }
}