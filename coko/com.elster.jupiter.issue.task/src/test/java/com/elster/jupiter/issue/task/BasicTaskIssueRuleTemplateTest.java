/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.task.event.TaskFailureEvent;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.impl.event.TaskEventDescription;
import com.elster.jupiter.issue.task.entity.OpenTaskIssueImpl;
import com.elster.jupiter.issue.task.impl.templates.BasicTaskIssueRuleTemplate;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;

import com.google.inject.Injector;
import org.osgi.service.event.EventConstants;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BasicTaskIssueRuleTemplateTest extends BaseTest {

    @Test
    @Transactional
    public void testCanCreateIssue() {
        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.REASON_TASK_FAILED);
        OpenIssue baseIssue = createBaseIssue(rule);

        BasicTaskIssueRuleTemplate template = getInjector().getInstance(BasicTaskIssueRuleTemplate.class);
        TaskFailureEvent event = getTaskFailureEvent(1L);

        OpenTaskIssue issue = template.createIssue(baseIssue, event);
        assertThat(issue.getId()).isEqualTo(baseIssue.getId());

    }


    @Test
    @Transactional
    public void testInProgressToOpenTransition() {
        CreationRule rule = getCreationRule("testInProgressToOpenTransition", ModuleConstants.FAILURE_TIME);
        OpenIssue baseIssue = createBaseIssue(rule);
        OpenTaskIssueImpl taskIssue = getDataModel().getInstance(OpenTaskIssueImpl.class);
        taskIssue.setIssue(baseIssue);
        taskIssue.setStatus(getIssueService().findStatus(IssueStatus.IN_PROGRESS).get());
        taskIssue.save();

        BasicTaskIssueRuleTemplate template = getInjector().getInstance(BasicTaskIssueRuleTemplate.class);
        TaskFailureEvent event = getTaskFailureEvent(1L);
        template.updateIssue(taskIssue, event);

        Optional<OpenTaskIssue> openIssue = getIssueTaskIssueService().findOpenIssue(taskIssue.getId());
        assertThat(openIssue.isPresent()).isTrue();
        assertThat(openIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
    }

    @Test
    @Transactional
    public void testResolveIssue() {
        // Create base issue
        CreationRule rule = getCreationRule("testResolveIssue", ModuleConstants.FAILURE_TIME);
        Meter meter = createMeter("1", "Name");
        Issue baseIssue = createBaseIssue(rule);
        // Create task issue
        OpenTaskIssueImpl taskIssue = getDataModel().getInstance(OpenTaskIssueImpl.class);
        taskIssue.setIssue((OpenIssue) baseIssue);
        taskIssue.save();
        // Mock event
        IssueEvent event = mock(IssueEvent.class);
        Optional<Issue> issueRef = Optional.of(taskIssue);
        doReturn(issueRef).when(event).findExistingIssue();
        // Test template
        BasicTaskIssueRuleTemplate template = getInjector().getInstance(BasicTaskIssueRuleTemplate.class);
        assertThat(template.resolveIssue(event).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCloseBaseIssue() {
        ((IssueServiceImpl) getIssueService()).addIssueProvider((IssueProvider) getIssueTaskIssueService());

        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.TASK_FAILED_EVENT);
        Meter meter = createMeter("1", "Name");
        BasicTaskIssueRuleTemplate template = getInjector().getInstance(BasicTaskIssueRuleTemplate.class);
        TaskFailureEvent event = getTaskFailureEvent(1L);
        OpenIssue issue = template.createIssue(createBaseIssue(rule), event);
        Optional<? extends Issue> baseIssue = getIssueService().findIssue(issue.getId());
        assertThat(baseIssue.get() instanceof OpenIssueImpl).isTrue();
        ((OpenIssue) baseIssue.get()).close(getIssueService().findStatus(IssueStatus.WONT_FIX).get());
        baseIssue = getIssueService().findIssue(issue.getId());
        assertThat(baseIssue.get() instanceof HistoricalIssueImpl).isTrue();
        assertThat(baseIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
    }

    private Meter createMeter(String amrId, String name) {
        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        return amrSystem.newMeter(amrId, name).create();
    }

    private OpenIssue createBaseIssue(CreationRule rule) {
        DataModel isuDataModel = getIssueDataModel();
        OpenIssueImpl baseIssue = isuDataModel.getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setRule(rule);
        baseIssue.setPriority(Priority.DEFAULT);
        baseIssue.save();
        return baseIssue;
    }

    private TaskFailureEvent getTaskFailureEvent(Long taskOccurrenceId) {
        TaskFailureEvent event = new TaskFailureEvent(getIssueTaskIssueService(), getMeteringService(), getTaskService(), getThesaurus(),getIssueService(), mock(Injector.class));
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/elster/jupiter/tasks/taskoccurrence/FAILED");
        messageMap.put(ModuleConstants.TASKOCCURRENCE_ID, taskOccurrenceId.toString());
        messageMap.put(ModuleConstants.ERROR_MESSAGE, "error message");
        messageMap.put(ModuleConstants.FAILURE_TIME, String.valueOf(Clock.systemDefaultZone().instant().toEpochMilli()));

        event.wrap(messageMap, TaskEventDescription.TASK_FAILED);
        return event;
    }
}
