/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.actions;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Order;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.impl.BaseTest;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointOpenIssueDataValidationImpl;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloseUsagePointIssueActionTest extends BaseTest {
    public static final String ISSUE_DEFAULT_TYPE_UUID = "usagepointdatavalidation";
    public static final String ISSUE_DEFAULT_REASON = "reason.default";
    public static final TranslationKey MESSAGE_SEED_DEFAULT_TRANSLATION = new TranslationKey() {
        @Override
        public String getKey() {
            return "issue.entity.default.translation";
        }

        @Override
        public String getDefaultFormat() {
            return "Default entity";
        }
    };

    private IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(CloseUsagePointIssueAction.class.getName());

    }

    private CreationRuleTemplate mockCreationRuleTemplate() {
        CreationRuleTemplate creationRuleTemplate = mock(CreationRuleTemplate.class);
        when(creationRuleTemplate.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(creationRuleTemplate.getName()).thenReturn("Template");
        when(creationRuleTemplate.getContent()).thenReturn("Content");
        ((IssueServiceImpl) getIssueService()).addCreationRuleTemplate(creationRuleTemplate);
        return creationRuleTemplate;
    }

    private CreationRule createCreationRule(String name) {
        IssueCreationService.CreationRuleBuilder builder = getIssueService().getIssueCreationService().newCreationRule();
        builder.setName(name);
        builder.setTemplate(mockCreationRuleTemplate().getName());
        builder.setIssueType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        builder.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        builder.setPriority(Priority.DEFAULT);
        builder.activate();
        return builder.complete();
    }

    protected UsagePointOpenIssueDataValidation createIssueMinInfo() {
        IssueServiceImpl issueService = (IssueServiceImpl) getIssueService();
        IssueType type = issueService.findIssueType(ISSUE_DEFAULT_TYPE_UUID).get();
        issueService.createReason(ISSUE_DEFAULT_REASON, type, MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
        OpenIssueImpl issue = issueService.getDataModel().getInstance(OpenIssueImpl.class);
        issue.setReason(issueService.findReason(ISSUE_DEFAULT_REASON).orElse(null));
        issue.setPriority(Priority.DEFAULT);
        issue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElse(null));
        CreationRule rule = createCreationRule("creation rule" + Instant.now());
        issue.setRule(rule);
        issue.save();

        UsagePointOpenIssueDataValidationImpl issueDC = getDataModel().getInstance(UsagePointOpenIssueDataValidationImpl.class);
        issueDC.setIssue(issue);
        issueDC.save();
        return issueDC;
    }

    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        Map<String, Object> properties = new HashMap<>();
        IssueStatus resolvedStatus = getIssueService().findStatus(IssueStatus.RESOLVED).get();
        properties.put(CloseUsagePointIssueAction.CLOSE_STATUS, new CloseUsagePointIssueAction.Status(resolvedStatus));
        properties.put(CloseUsagePointIssueAction.COMMENT, new CloseUsagePointIssueAction.Comment("Comment"));
        OpenIssue issue = createIssueMinInfo();

        LdapUserDirectory local = getUserService().createApacheDirectory("local");
        local.setSecurity("sec");
        local.setUrl("url");
        local.setDirectoryUser("dirUser");
        local.setPassword("pass");
        local.update();
        User user = getUserService().findOrCreateUser("user", "local", "APD");
        getThreadPrincipalService().set(user);

        assertThat(issue.getStatus().getKey()).isNotEqualTo(resolvedStatus.getKey());

        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);

        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getStatus().getKey()).isEqualTo(resolvedStatus.getKey());
        Assertions.assertThat(getIssueComments(issue)).hasSize(1);
        Assertions.assertThat(getIssueComments(issue).get(0).getComment()).isEqualTo("Comment");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_NOT_POSSIBLE_VALUE + "}", property = "properties.CloseUsagePointIssueAction.status", strict = true)
    public void testExecuteActionWrongClosingStatus() {
        Map<String, Object> properties = new HashMap<>();
        IssueStatus inProgressStatus = getIssueService().findStatus(IssueStatus.IN_PROGRESS).get();
        properties.put(CloseUsagePointIssueAction.CLOSE_STATUS, new CloseUsagePointIssueAction.Status(inProgressStatus));

        action.initAndValidate(properties);
    }

    @Test
    @Transactional
    public void testExecuteActionIssueIsNotApplicable() {
        Map<String, Object> properties = new HashMap<>();
        IssueStatus resolvedStatus = getIssueService().findStatus(IssueStatus.RESOLVED).get();
        properties.put(CloseUsagePointIssueAction.CLOSE_STATUS, new CloseUsagePointIssueAction.Status(resolvedStatus));
        Issue issue = mock(Issue.class);
        when(issue.getStatus()).thenReturn(resolvedStatus);

        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);

        assertThat(actionResult.isSuccess()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING + "}", property = "properties.CloseUsagePointIssueAction.status", strict = true)
    public void testValidateMandatoryParameters() {
        action.initAndValidate(new HashMap<>());
    }

    protected List<IssueComment> getIssueComments(Issue issue) {
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        return query.select(where("issueId").isEqualTo(issue.getId()), Order.ascending("createTime"));
    }

    @Test
    public void testActionAvailabilityDependingOnStatsus() {
        Issue issue = mock(Issue.class);
        IssueStatus status = mock(IssueStatus.class);
        when(issue.getStatus()).thenReturn(status);
        when(status.getKey()).thenReturn(IssueStatus.OPEN);

        assertThat(action.isApplicable(issue)).isTrue();
        when(status.getKey()).thenReturn(IssueStatus.IN_PROGRESS);
        assertThat(action.isApplicable(issue)).isFalse();
        when(status.getKey()).thenReturn(IssueStatus.RESOLVED);
        assertThat(action.isApplicable(issue)).isFalse();
        when(status.getKey()).thenReturn(IssueStatus.WONT_FIX);
        assertThat(action.isApplicable(issue)).isFalse();
    }

    @Test
    public void testUserHasPrivileges() {
        User hasPrivilege = mock(User.class);
        Privilege assignPrivilege = mock(Privilege.class);
        when(assignPrivilege.getName()).thenReturn(Privileges.Constants.CLOSE_ISSUE);
        Privilege actionPrivilege = mock(Privilege.class);
        when(actionPrivilege.getName()).thenReturn(Privileges.Constants.ACTION_ISSUE);
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(assignPrivilege);
        privileges.add(actionPrivilege);
        when(hasPrivilege.getPrivileges()).thenReturn(privileges);

        User hasNoPrivileges = mock(User.class);

        assertThat(action.isApplicableForUser(hasPrivilege)).isTrue();
        assertThat(action.isApplicableForUser(hasNoPrivileges)).isFalse();
    }
}
