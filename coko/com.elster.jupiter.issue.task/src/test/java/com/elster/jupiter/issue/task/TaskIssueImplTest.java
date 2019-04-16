/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.entity.OpenTaskIssueImpl;
import com.elster.jupiter.transaction.TransactionContext;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskIssueImplTest extends com.elster.jupiter.issue.task.BaseTest {

    @Test
    @ExpectedConstraintViolation(messageId = "{com.elster.jupiter.orm.associations.isPresent}", property = "baseIssue")
    public void testIDCCreationWithoutBaseIssue() {
        try (TransactionContext context = getContext()) {
            OpenTaskIssueImpl taskIssue = getDataModel().getInstance(OpenTaskIssueImpl.class);
            taskIssue.save();
        }
    }

    @Test
    public void testIDCSuccessfullCreation() {
        try (TransactionContext context = getContext()) {
            CreationRule rule = getCreationRule("testIDCSuccessfullCreation", ModuleConstants.REASON_TASK_FAILED);
            Issue baseIssue = createBaseIssue(rule);
            OpenTaskIssueImpl taskIssue = getDataModel().getInstance(OpenTaskIssueImpl.class);
            taskIssue.setIssue((OpenIssue) baseIssue);
            taskIssue.save();
        }
    }

    @Test
    public void testITKCloseOperation() {
        OpenTaskIssueImpl taskIssue = null;
        try (TransactionContext context = getContext()) {
            CreationRule rule = getCreationRule("testITKCloseOperation", ModuleConstants.REASON_TASK_FAILED);
            Issue baseIssue = createBaseIssue(rule);
            taskIssue = getDataModel().getInstance(OpenTaskIssueImpl.class);
            taskIssue.setIssue((OpenIssue) baseIssue);
            taskIssue.save();
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            HistoricalTaskIssue closed = taskIssue.close(getIssueService().findStatus(IssueStatus.RESOLVED).get());
            assertThat(closed.getId()).isEqualTo(taskIssue.getId());
            assertThat(closed.getReason().getKey()).isEqualTo(ModuleConstants.REASON_TASK_FAILED);

            Optional<OpenTaskIssue> openIssueRef = getIssueTaskIssueService().findOpenIssue(taskIssue.getId());
            assertThat(openIssueRef.isPresent()).isFalse();

            Optional<HistoricalTaskIssue> historicalIssueRef = getIssueTaskIssueService().findHistoricalIssue(taskIssue.getId());
            assertThat(historicalIssueRef.isPresent()).isTrue();
        }
    }

    protected Issue createBaseIssue(CreationRule rule) {
        OpenIssueImpl baseIssue = getIssueDataModel().getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setRule(rule);
        baseIssue.setPriority(Priority.DEFAULT);
        baseIssue.save();
        return baseIssue;
    }
}
