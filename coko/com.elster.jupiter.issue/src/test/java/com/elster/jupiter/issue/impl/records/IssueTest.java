/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class IssueTest extends BaseTest {

    @Test
    @Transactional
    public void createIssueManualNullRuleSuccess() {
        OpenIssueImpl issue = getDataModel().getInstance(OpenIssueImpl.class);
        issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orElse(null));
        issue.setPriority(Priority.DEFAULT);
        issue.setType(getIssueService().findIssueType(IssueService.MANUAL_ISSUE_TYPE).orElse(null));
        issue.save();
        assertThat(issue.getRule()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void createIssueNotManualNullRuleFailure() {
        OpenIssueImpl issue = getDataModel().getInstance(OpenIssueImpl.class);
        issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orElse(null));
        issue.setPriority(Priority.DEFAULT);
        issue.setType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        issue.save();
    }

    @Test
    @Transactional
    public void createIssueNotManualNotNullRuleSuccess() {
        OpenIssue issue = createIssueMinInfo();
        assertThat(issue.getRule()).isNotNull();
    }
}
