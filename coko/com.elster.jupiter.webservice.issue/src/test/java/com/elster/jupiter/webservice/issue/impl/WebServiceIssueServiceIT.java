/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.webservice.issue.WebServiceHistoricalIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssueFilter;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventDescription;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WebServiceIssueServiceIT extends BaseIT {
    @Test
    @Transactional
    public void testFindIssueById() {
        WebServiceCallOccurrence another = createWebServiceCallOccurrence(endPointConfiguration);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence));
        issueCreationService.processIssueCreationEvent(creationRule.getId(), mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, another));

        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(2);

        long firstIssue = issues.get(0).getId();
        long secondIssue = issues.get(1).getId();

        Optional<? extends WebServiceIssue> issue = webServiceIssueService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof WebServiceOpenIssue).isTrue();

        //close first issue
        ((WebServiceOpenIssue) issue.get()).close(issueService.findStatus(IssueStatus.RESOLVED).get());

        issue = webServiceIssueService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof WebServiceHistoricalIssue).isTrue();

        issue = webServiceIssueService.findIssue(secondIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof WebServiceOpenIssue).isTrue();

        issue = webServiceIssueService.findOpenIssue(firstIssue);
        assertThat(issue).isEmpty();

        issue = webServiceIssueService.findHistoricalIssue(firstIssue);
        assertThat(issue).isPresent();

        issue = webServiceIssueService.findOpenIssue(secondIssue);
        assertThat(issue).isPresent();
    }

    @Test
    @Transactional
    public void testFindAllWebServiceIssuesFilterByAssignee() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.setUnassignedOnly();
        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);

        LdapUserDirectory local = inMemoryPersistence.getService(UserService.class).createApacheDirectory("APD");
        local.setSecurity("sec");
        local.setUrl("url");
        local.setDirectoryUser("dirUser");
        local.setPassword("pass");
        local.update();

        User assignee = inMemoryPersistence.getService(UserService.class).findOrCreateUser("User", "APD", "APD");
        assignee.update();
        WebServiceIssue issue = webServiceIssueService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.assignTo(assignee.getId(), null);
        issue.update();

        filter = new WebServiceIssueFilter();
        filter.setUnassignedOnly();
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();

        filter = new WebServiceIssueFilter();
        filter.setAssignee(assignee);
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(creationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getAssignee().getUser().getId()).isEqualTo(assignee.getId());

        filter = new WebServiceIssueFilter();
        User anotherAssignee = inMemoryPersistence.getService(UserService.class).findOrCreateUser("AnotherUser", "APD", "APD");
        anotherAssignee.update();
        filter.setAssignee(anotherAssignee);
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllWebServiceIssuesFilterByReason() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.setIssueReason(issueService.findReason(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON).get());
        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);
        WebServiceIssue issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(creationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        filter = new WebServiceIssueFilter();
        IssueReason reason = issueService.createReason("somereason", issueService.findIssueType(WebServiceIssueService.ISSUE_TYPE_NAME).get(),
                TranslationKeys.WEB_SERVICE_ISSUE_REASON, TranslationKeys.WEB_SERVICE_ISSUE_REASON_DESCRIPTION);
        reason.update();
        filter.setIssueReason(reason);
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllWebServiceIssuesFilterByEndPointConfiguration() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.addEndPointConfigurationId(endPointConfiguration.getId());
        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);
        WebServiceIssue issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(creationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getWebServiceCallOccurrence()).isEqualTo(webServiceCallOccurrence);

        filter = new WebServiceIssueFilter();
        filter.addEndPointConfigurationId(endPointConfiguration.getId() + 1);
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllWebServiceIssuesFilterByWebServiceCallOccurrence() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.addWebServiceCallOccurrenceId(webServiceCallOccurrence.getId());
        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);
        WebServiceIssue issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(creationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getWebServiceCallOccurrence()).isEqualTo(webServiceCallOccurrence);

        filter = new WebServiceIssueFilter();
        filter.addEndPointConfigurationId(webServiceCallOccurrence.getId() + 1);
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllWebServiceIssuesFilterByStatus() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);
        WebServiceIssue issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(creationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        filter = new WebServiceIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).hasSize(1);

        filter = new WebServiceIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();

        filter = new WebServiceIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        filter.addStatus(issueService.findStatus(IssueStatus.WONT_FIX).get());
        issues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testIssueProvider() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        IssueProvider issueProvider = (WebServiceIssueServiceImpl) webServiceIssueService;

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        OpenIssue openBaseIssue = baseIssues.get(0);

        Optional<? extends OpenIssue> openIssue = issueProvider.getOpenIssue(openBaseIssue);
        assertThat(openIssue).isPresent();
        assertThat(openIssue.get() instanceof WebServiceOpenIssue).isTrue();
        assertThat(openIssue.get().getId()).isEqualTo(openBaseIssue.getId());

        assertThat(issueProvider.getOpenIssue(openIssue.get()).get()).isEqualTo(openIssue.get());

        openIssue.get().close(issueService.findStatus(IssueStatus.WONT_FIX).get());

        HistoricalIssue historicalBaseIssue = issueService.query(HistoricalIssue.class).select(Condition.TRUE).get(0);

        Optional<? extends HistoricalIssue> historicalIssue = issueProvider.getHistoricalIssue(historicalBaseIssue);
        assertThat(historicalIssue).isPresent();
        assertThat(historicalIssue.get() instanceof WebServiceHistoricalIssue).isTrue();
        assertThat(historicalIssue.get().getId()).isEqualTo(historicalBaseIssue.getId());

        assertThat(issueProvider.getHistoricalIssue(historicalIssue.get()).get()).isEqualTo(historicalIssue.get());
    }

    @Test
    @Transactional
    public void testCloseIssue() {
        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        List<? extends WebServiceIssue> openIssues = webServiceIssueService.findAllWebServiceIssues(filter).find();
        assertThat(openIssues).hasSize(1);
        WebServiceOpenIssue openIssue = (WebServiceOpenIssue) openIssues.get(0);
        openIssue.close(issueService.findStatus(IssueStatus.WONT_FIX).get());

        filter = new WebServiceIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        assertThat(webServiceIssueService.findAllWebServiceIssues(filter).find()).isEmpty();
        assertThat(webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find()).hasSize(1);

        Optional<WebServiceHistoricalIssue> historicalIssue = webServiceIssueService.findHistoricalIssue(openIssue.getId());
        assertThat(historicalIssue).isPresent();
        assertThat(historicalIssue.get().getId()).isEqualTo(openIssue.getId());
        assertThat(historicalIssue.get().getRule().getId()).isEqualTo(creationRule.getId());
        assertThat(historicalIssue.get().getReason().getKey()).isEqualTo(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON);
        assertThat(historicalIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        assertThat(historicalIssue.get().getWebServiceCallOccurrence()).isEqualTo(webServiceCallOccurrence);
    }

    @Test
    @Transactional
    public void testCloseBaseIssue() {
        ((IssueServiceImpl) issueService).addIssueProvider((IssueProvider) webServiceIssueService);

        IssueEvent event = mockIssueEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        issueCreationService.processIssueCreationEvent(creationRule.getId(), event);

        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(1);
        Optional<? extends Issue> baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof OpenIssueImpl).isTrue();
        ((OpenIssue) baseIssue.get()).close(issueService.findStatus(IssueStatus.WONT_FIX).get());
        baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof HistoricalIssueImpl).isTrue();
        assertThat(baseIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        assertThat(webServiceIssueService.findHistoricalIssue(baseIssue.get().getId())).isPresent();
    }
}
