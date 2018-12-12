/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.insight.issue.datavalidation.UsagePointDataValidationIssueFilter;
import com.elster.insight.issue.datavalidation.UsagePointHistoricalIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.UsagePointNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class UsagePointUsagePointIssueDataValidationServiceTest {

    protected static InMemoryIntegrationPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(UsagePointDataValidationIssueCreationRuleTemplateTest.getTransactionService());
    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    private CreationRule issueCreationRule;

    @BeforeClass
    public static void initialize() throws SQLException {
        UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence = new InMemoryIntegrationPersistence(mock(MetrologyConfigurationService.class));
        UsagePointDataValidationIssueCreationRuleTemplateTest.initializeClock();
        UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.initializeDatabase("UsagePointUsagePointIssueDataValidationServiceTest", false);

        try (TransactionContext ctx = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getTransactionService().getContext()) {
            UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(FiniteStateMachineService.class);
            UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UsagePointIssueDataValidationService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getTransactionService();
    }

    @Before
    public void setUp() throws Exception {
        UsagePoint usagePoint = mock(UsagePoint.class);
        issueService = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(IssueService.class);
        UsagePointDataValidationIssueCreationRuleTemplate template = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UsagePointDataValidationIssueCreationRuleTemplate.class);
        ((IssueServiceImpl) issueService).addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        usagePointIssueDataValidationService = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UsagePointIssueDataValidationService.class);
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        long metrologyConfigurationId = 1L;

        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        List<HasIdAndName> value = new ArrayList<>();
        HasIdAndName deviceConfig = mock(HasIdAndName.class);
        when(deviceConfig.getId()).thenReturn(metrologyConfigurationId);
        value.add(deviceConfig);
        props.put(UsagePointDataValidationIssueCreationRuleTemplate.METROLOGY_CONFIGS, value);
        issueCreationRule = ruleBuilder.setTemplate(UsagePointDataValidationIssueCreationRuleTemplate.NAME)
                .setName("Test")
                .setIssueType(issueService.findIssueType(UsagePointIssueDataValidationService.ISSUE_TYPE_NAME).get())
                .setReason(issueService.findReason(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                .setPriority(Priority.DEFAULT)
                .activate()
                .setDueInTime(DueInType.YEAR, 5)
                .setProperties(props)
                .complete();
    }

    @Test
    @Transactional
    public void testFindIssueById() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(2);

        long firstIssue = issues.get(0).getId();
        long secondIssue = issues.get(1).getId();

        Optional<? extends UsagePointIssueDataValidation> issue = usagePointIssueDataValidationService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof UsagePointOpenIssueDataValidation).isTrue();

        //close first issue
        UsagePointOpenIssueDataValidation closedIssue = (UsagePointOpenIssueDataValidation) issue.get();
        closedIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get());

        issue = usagePointIssueDataValidationService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof UsagePointHistoricalIssueDataValidation).isTrue();

        issue = usagePointIssueDataValidationService.findIssue(secondIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof UsagePointOpenIssueDataValidation).isTrue();

        issue = usagePointIssueDataValidationService.findOpenIssue(firstIssue);
        assertThat(issue).isEmpty();

        issue = usagePointIssueDataValidationService.findHistoricalIssue(firstIssue);
        assertThat(issue).isPresent();

        issue = usagePointIssueDataValidationService.findOpenIssue(secondIssue);
        assertThat(issue).isPresent();
    }

    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByAssignee() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        filter.setUnassignedOnly();
        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);

        LdapUserDirectory local = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UserService.class).createApacheDirectory("APD");
        local.setSecurity("sec");
        local.setUrl("url");
        local.setDirectoryUser("dirUser");
        local.setPassword("pass");
        local.update();

        User assignee = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UserService.class).findOrCreateUser("User", "APD", "APD");
        assignee.update();
        UsagePointIssueDataValidation issue = usagePointIssueDataValidationService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.assignTo(assignee.getId(), null);
        issue.update();

        filter = new UsagePointDataValidationIssueFilter();
        filter.setUnassignedOnly();
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();

        filter = new UsagePointDataValidationIssueFilter();
        filter.setAssignee(assignee);
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getAssignee().getUser().getId()).isEqualTo(assignee.getId());

        filter = new UsagePointDataValidationIssueFilter();
        User anotherAssignee = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UserService.class).findOrCreateUser("AnotherUser", "APD", "APD");
        anotherAssignee.update();
        filter.setAssignee(anotherAssignee);
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByReason() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        filter.setIssueReason(issueService.findReason(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get());
        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        UsagePointIssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        filter = new UsagePointDataValidationIssueFilter();
        IssueReason reason = issueService.createReason("somereason", issueService.findIssueType(UsagePointIssueDataValidationService.ISSUE_TYPE_NAME).get(),
                TranslationKeys.DATA_VALIDATION_ISSUE_REASON, TranslationKeys.DATA_VALIDATION_ISSUE_REASON_DESCRIPTION);
        reason.update();
        filter.setIssueReason(reason);
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByDevice() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        AmrSystem amrSystem = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class).findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        //EndDevice endDevice = amrSystem.createEndDevice("360", "METER");
        // endDevice.update();
        UsagePoint usagePoint = mock(UsagePoint.class);
        UsagePointIssueDataValidation issue = usagePointIssueDataValidationService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.setUsagePoint(usagePoint);
        issue.update();

        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        // filter.setUsagePoint(endDevice);
        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getDevice()).isEqualTo(usagePoint);

        //endDevice = amrSystem.createEndDevice("180", "ANOTHER METER");
        //endDevice.update();
        filter = new UsagePointDataValidationIssueFilter();
        filter.setUsagePoint(usagePoint);
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByStatus() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        UsagePointIssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        filter = new UsagePointDataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);

        filter = new UsagePointDataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();

        filter = new UsagePointDataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        filter.addStatus(issueService.findStatus(IssueStatus.WONT_FIX).get());
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateIssueWithNonEstimatedBlocks() {
        Instant now = Instant.now();

        MeteringService meteringService = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter", "myName").create();
        ReadingType readingType1Min = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        ReadingType readingType3Min = meteringService.createReadingType("0.0.14.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 3-minute");
        ReadingType registerReadingType = meteringService.createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake register RT");
        MeterActivation meterActivation = newMeter.activate(now);
        Channel channelRT1 = meterActivation.getChannelsContainer().createChannel(readingType1Min);
        Channel channelRT2 = meterActivation.getChannelsContainer().createChannel(readingType3Min);
        Channel registerChannel = meterActivation.getChannelsContainer().createChannel(registerReadingType);

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now));
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now.plus(10, ChronoUnit.MINUTES)));
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now.plus(30, ChronoUnit.MINUTES)));
        newMeter.store(QualityCodeSystem.MDM, meterReading);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        UsagePointOpenIssueDataValidation dataValidationIssue = usagePointIssueDataValidationService.findOpenIssue(issues.get(0).getId()).get();

        dataValidationIssue.addNotEstimatedBlock(channelRT1, readingType1Min, now);//(now-1min, now]
        dataValidationIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now);//(now-3min, now]
        dataValidationIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now.plus(3, ChronoUnit.MINUTES));//(now, now+3min]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now);//(EPOCH, now]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(30, ChronoUnit.MINUTES));//(now+10min, now+30min]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(10, ChronoUnit.MINUTES));//(now, now+10min]
        dataValidationIssue.update();

        dataValidationIssue = usagePointIssueDataValidationService.findOpenIssue(dataValidationIssue.getId()).get();
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(3);
        Map<Channel, List<UsagePointNotEstimatedBlock>> blocks = dataValidationIssue.getNotEstimatedBlocks().stream()
                .collect(Collectors.groupingBy(UsagePointNotEstimatedBlock::getChannel));

        UsagePointNotEstimatedBlock block1 = blocks.get(registerChannel).get(0);
        assertThat(block1.getChannel()).isEqualTo(registerChannel);
        assertThat(block1.getReadingType()).isEqualTo(registerReadingType);
        assertThat(block1.getStartTime()).isEqualTo(Instant.EPOCH);
        assertThat(block1.getEndTime()).isEqualTo(now.plus(30, ChronoUnit.MINUTES));

        UsagePointNotEstimatedBlock block2 = blocks.get(channelRT1).get(0);
        assertThat(block2.getChannel()).isEqualTo(channelRT1);
        assertThat(block2.getReadingType()).isEqualTo(readingType1Min);
        assertThat(block2.getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(block2.getEndTime()).isEqualTo(now);

        UsagePointNotEstimatedBlock block3 = blocks.get(channelRT2).get(0);
        assertThat(block3.getChannel()).isEqualTo(channelRT2);
        assertThat(block3.getReadingType()).isEqualTo(readingType3Min);
        assertThat(block3.getStartTime()).isEqualTo(now.minus(3, ChronoUnit.MINUTES));
        assertThat(block3.getEndTime()).isEqualTo(now.plus(3, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testUpdateIssueWithNonEstimatedBlocks() {
        Instant now = Instant.now();

        MeteringService meteringService = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter", "myName").create();
        ReadingType readingType = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        Channel channel = newMeter.activate(now).getChannelsContainer().createChannel(readingType);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        UsagePointOpenIssueDataValidation dataValidationIssue = usagePointIssueDataValidationService.findOpenIssue(issues.get(0).getId()).get();

        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));//(now-2min, now-1min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(1, ChronoUnit.MINUTES));//(now, now+1min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(3, ChronoUnit.MINUTES));//(now+2min, now+3min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now);//(now-1min, now]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(2, ChronoUnit.MINUTES));//(now+1min, now+2min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(4, ChronoUnit.MINUTES));//(now+3min, now+4min]
        dataValidationIssue.update();//(now-2min, now+4min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(2, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));

        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now);//(now-1min, now]
        dataValidationIssue.update();//(now-2min, now-1min] + (now, now+4min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(2, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));

        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now);//(now-1min, now]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.minus(300, ChronoUnit.MINUTES));//(now-299min, now-300min]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));//(now-2min, now-1min]
        dataValidationIssue.update();//(now-1min, now+4min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));

        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));//(now-2min, now-1min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now);//(now-1min, now]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.plus(300, ChronoUnit.MINUTES));//(now+299min, now+300min]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.plus(1, ChronoUnit.MINUTES));//(now, now+1min]
        dataValidationIssue.update();//(now-2min, now] (now+1min, now+4min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(2, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now.plus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testUpdateIssueWithNonEstimatedBlocksOnRegister() {
        Instant now = Instant.now();

        MeteringService meteringService = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter", "myName").create();
        ReadingType registerReadingType = meteringService.createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake register RT");
        MeterActivation meterActivation = newMeter.activate(now);
        Channel registerChannel = meterActivation.getChannelsContainer().createChannel(registerReadingType);

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now));
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now.plus(10, ChronoUnit.MINUTES)));
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now.plus(30, ChronoUnit.MINUTES)));
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now.plus(50, ChronoUnit.MINUTES)));
        meterReading.addReading(ReadingImpl.of(registerReadingType.getMRID(), BigDecimal.valueOf(1), now.plus(55, ChronoUnit.MINUTES)));
        newMeter.store(QualityCodeSystem.MDM, meterReading);//[now, now+10min, now+30min, now+50min, now+55min]

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);

        UsagePointOpenIssueDataValidation dataValidationIssue = usagePointIssueDataValidationService.findOpenIssue(issues.get(0).getId()).get();
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(0);

        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now);//(EPOCH, now]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(10, ChronoUnit.MINUTES));//(EPOCH, now+10min]
        dataValidationIssue.update();

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(Instant.EPOCH);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(10, ChronoUnit.MINUTES));

        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(50, ChronoUnit.MINUTES));//(now+30min, now+55min]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(55, ChronoUnit.MINUTES));//(now+50min, now+55min]
        dataValidationIssue.update();//(EPOCH, now] + (now+30min, now+55min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(Instant.EPOCH);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(10, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now.plus(30, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(55, ChronoUnit.MINUTES));

        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(30, ChronoUnit.MINUTES));//(now+10min, now+30min]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(55, ChronoUnit.MINUTES));//(now+50min, now+55min]
        dataValidationIssue.update();//(EPOCH, now+55min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(Instant.EPOCH);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(55, ChronoUnit.MINUTES));

        dataValidationIssue.removeNotEstimatedBlock(registerChannel, registerReadingType, now.plus(10, ChronoUnit.MINUTES));//(now, now+10min]
        dataValidationIssue.update();//(EPOCH, now] + (now+10min, now+55min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(Instant.EPOCH);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now.plus(10, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(55, ChronoUnit.MINUTES));

        dataValidationIssue.removeNotEstimatedBlock(registerChannel, registerReadingType, now);//(EPOCH, now]
        dataValidationIssue.update();//(now+10min, now+55min]

        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.plus(10, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(55, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testIssueProvider() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        IssueProvider issueProvider = (UsagePointIssueDataValidationServiceImpl) usagePointIssueDataValidationService;

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        OpenIssue openBaseIssue = baseIssues.get(0);

        Optional<? extends OpenIssue> openIssueDV = issueProvider.getOpenIssue(openBaseIssue);
        assertThat(openIssueDV).isPresent();
        assertThat(openIssueDV.get() instanceof UsagePointOpenIssueDataValidation).isTrue();
        assertThat(openIssueDV.get().getId()).isEqualTo(openBaseIssue.getId());

        assertThat(issueProvider.getOpenIssue(openIssueDV.get()).get()).isEqualTo(openIssueDV.get());

        openIssueDV.get().close(issueService.findStatus(IssueStatus.WONT_FIX).get());

        HistoricalIssue historicalBaseIssue = issueService.query(HistoricalIssue.class).select(Condition.TRUE).get(0);

        Optional<? extends HistoricalIssue> historicalIssueDV = issueProvider.getHistoricalIssue(historicalBaseIssue);
        assertThat(historicalIssueDV).isPresent();
        assertThat(historicalIssueDV.get() instanceof UsagePointHistoricalIssueDataValidation).isTrue();
        assertThat(historicalIssueDV.get().getId()).isEqualTo(historicalBaseIssue.getId());

        assertThat(issueProvider.getHistoricalIssue(historicalIssueDV.get()).get()).isEqualTo(historicalIssueDV.get());
    }

    @Test
    @Transactional
    public void testCloseIssue() {
        MeteringService meteringService = UsagePointDataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter", "myName").create();
        ReadingType readingType1Min = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        ReadingType readingType3Min = meteringService.createReadingType("0.0.14.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 3-minute");
        MeterActivation meterActivation = newMeter.activate(Instant.now());
        Channel channelRT1 = meterActivation.getChannelsContainer().createChannel(readingType1Min);
        Channel channelRT2 = meterActivation.getChannelsContainer().createChannel(readingType3Min);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        List<? extends UsagePointIssueDataValidation> openIssues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(openIssues).hasSize(1);
        UsagePointOpenIssueDataValidation openIssue = (UsagePointOpenIssueDataValidation) openIssues.get(0);

        Instant now = Instant.now();
        openIssue.addNotEstimatedBlock(channelRT1, readingType1Min, now);//(now-1min, now]
        openIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now.plus(300, ChronoUnit.MINUTES));//(now+297min, now+300min]
        openIssue.update();

        openIssue = usagePointIssueDataValidationService.findOpenIssue(openIssue.getId()).get();
        openIssue.close(issueService.findStatus(IssueStatus.WONT_FIX).get());

        filter = new UsagePointDataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        assertThat(usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find()).isEmpty();
        assertThat(usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find()).hasSize(1);

        Optional<UsagePointHistoricalIssueDataValidation> historicalIssue = usagePointIssueDataValidationService.findHistoricalIssue(openIssue.getId());
        assertThat(historicalIssue).isPresent();
        assertThat(historicalIssue.get().getId()).isEqualTo(openIssue.getId());
        assertThat(historicalIssue.get().getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(historicalIssue.get().getReason().getKey()).isEqualTo(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(historicalIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        List<UsagePointNotEstimatedBlock> usagePointNotEstimatedBlocks = historicalIssue.get().getNotEstimatedBlocks();
        assertThat(usagePointNotEstimatedBlocks).hasSize(2);

        UsagePointNotEstimatedBlock block1 = usagePointNotEstimatedBlocks.get(0);
        assertThat(block1.getChannel()).isEqualTo(channelRT1);
        assertThat(block1.getReadingType()).isEqualTo(readingType1Min);
        assertThat(block1.getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(block1.getEndTime()).isEqualTo(now);

        UsagePointNotEstimatedBlock block2 = usagePointNotEstimatedBlocks.get(1);
        assertThat(block2.getChannel()).isEqualTo(channelRT2);
        assertThat(block2.getReadingType()).isEqualTo(readingType3Min);
        assertThat(block2.getStartTime()).isEqualTo(now.plus(297, ChronoUnit.MINUTES));
        assertThat(block2.getEndTime()).isEqualTo(now.plus(300, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testCloseBaseIssue() {
        ((IssueServiceImpl) issueService).addIssueProvider((IssueProvider) usagePointIssueDataValidationService);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        Optional<? extends Issue> baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof OpenIssueImpl).isTrue();
        ((OpenIssue) baseIssue.get()).close(issueService.findStatus(IssueStatus.WONT_FIX).get());
        baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof HistoricalIssueImpl).isTrue();
        assertThat(baseIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        assertThat(usagePointIssueDataValidationService.findHistoricalIssue(baseIssue.get().getId())).isPresent();
    }
}
