/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.Finder;
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
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;

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
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueDataValidationServiceTest {

    protected static InMemoryIntegrationPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(DataValidationIssueCreationRuleTemplateTest.getTransactionService());
    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private IssueDataValidationService issueDataValidationService;
    private CreationRule issueCreationRule;

    @BeforeClass
    public static void initialize() throws SQLException {
        DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence = new InMemoryIntegrationPersistence(mock(DeviceConfigurationService.class));
        DataValidationIssueCreationRuleTemplateTest.initializeClock();
        DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.initializeDatabase("IssueDataValidationServiceTest", false);

        try (TransactionContext ctx = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getTransactionService().getContext()) {
            DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(FiniteStateMachineService.class);
            DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(IssueDataValidationService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getTransactionService();
    }

    @Before
    public void setUp() throws Exception {
        issueService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(IssueService.class);
        DataValidationIssueCreationRuleTemplate template = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(DataValidationIssueCreationRuleTemplate.class);
        ((IssueServiceImpl) issueService).addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        issueDataValidationService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(IssueDataValidationService.class);
        DeviceConfigurationService deviceConfigurationService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(DeviceConfigurationService.class);
        Finder deviceTypeFinder = mock(Finder.class);
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        long deviceConfigurationId = 1L;
        when(deviceConfiguration.getId()).thenReturn(deviceConfigurationId);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
        deviceConfigurations.add(deviceConfiguration);
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceTypeFinder.stream()).thenAnswer(invocationOnMock -> Stream.of(deviceType));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);
        when(deviceConfigurationService.findDeviceConfiguration(deviceConfigurationId)).thenReturn(Optional.of(deviceConfiguration));

        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        List<HasIdAndName> value = new ArrayList<>();
        HasIdAndName deviceConfig = mock(HasIdAndName.class);
        when(deviceConfig.getId()).thenReturn(deviceConfigurationId);
        value.add(deviceConfig);
        props.put(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, value);
        issueCreationRule = ruleBuilder.setTemplate(DataValidationIssueCreationRuleTemplate.NAME)
                .setName("Test")
                .setIssueType(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get())
                .setReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                .setPriority(Priority.DEFAULT)
                .setStatus(true)
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

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(2);

        long firstIssue = issues.get(0).getId();
        long secondIssue = issues.get(1).getId();

        Optional<? extends IssueDataValidation> issue = issueDataValidationService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof OpenIssueDataValidation).isTrue();

        //close first issue
        OpenIssueDataValidation closedIssue = (OpenIssueDataValidation) issue.get();
        closedIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get());

        issue = issueDataValidationService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof HistoricalIssueDataValidation).isTrue();

        issue = issueDataValidationService.findIssue(secondIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof OpenIssueDataValidation).isTrue();

        issue = issueDataValidationService.findOpenIssue(firstIssue);
        assertThat(issue).isEmpty();

        issue = issueDataValidationService.findHistoricalIssue(firstIssue);
        assertThat(issue).isPresent();

        issue = issueDataValidationService.findOpenIssue(secondIssue);
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

        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setUnassignedOnly();
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);

        LdapUserDirectory local = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UserService.class).createApacheDirectory("APD");
        local.setSecurity("sec");
        local.setUrl("url");
        local.setDirectoryUser("dirUser");
        local.setPassword("pass");
        local.update();

        User assignee = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UserService.class).findOrCreateUser("User", "APD", "APD");
        assignee.update();
        IssueDataValidation issue = issueDataValidationService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.assignTo(assignee.getId(), null);
        issue.update();

        filter = new DataValidationIssueFilter();
        filter.setUnassignedOnly();
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();

        filter = new DataValidationIssueFilter();
        filter.setAssignee(assignee);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getAssignee().getUser().getId()).isEqualTo(assignee.getId());

        filter = new DataValidationIssueFilter();
        User anotherAssignee = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(UserService.class).findOrCreateUser("AnotherUser", "APD", "APD");
        anotherAssignee.update();
        filter.setAssignee(anotherAssignee);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
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

        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setIssueReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get());
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        filter = new DataValidationIssueFilter();
        IssueReason reason = issueService.createReason("somereason", issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get(),
                TranslationKeys.DATA_VALIDATION_ISSUE_REASON, TranslationKeys.DATA_VALIDATION_ISSUE_REASON_DESCRIPTION);
        reason.update();
        filter.setIssueReason(reason);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
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

        AmrSystem amrSystem = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class).findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        EndDevice endDevice = amrSystem.createEndDevice("360", "METER");
        endDevice.update();
        IssueDataValidation issue = issueDataValidationService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.setDevice(endDevice);
        issue.update();

        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setDevice(endDevice);
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getDevice()).isEqualTo(endDevice);

        endDevice = amrSystem.createEndDevice("180", "ANOTHER METER");
        endDevice.update();
        filter = new DataValidationIssueFilter();
        filter.setDevice(endDevice);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
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

        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);

        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();

        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        filter.addStatus(issueService.findStatus(IssueStatus.WONT_FIX).get());
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateIssueWithNonEstimatedBlocks() {
        Instant now = Instant.now();

        MeteringService meteringService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
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
        newMeter.store(QualityCodeSystem.MDC, meterReading);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        OpenIssueDataValidation dataValidationIssue = issueDataValidationService.findOpenIssue(issues.get(0).getId()).get();

        dataValidationIssue.addNotEstimatedBlock(channelRT1, readingType1Min, now);//(now-1min, now]
        dataValidationIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now);//(now-3min, now]
        dataValidationIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now.plus(3, ChronoUnit.MINUTES));//(now, now+3min]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now);//(EPOCH, now]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(30, ChronoUnit.MINUTES));//(now+10min, now+30min]
        dataValidationIssue.addNotEstimatedBlock(registerChannel, registerReadingType, now.plus(10, ChronoUnit.MINUTES));//(now, now+10min]
        dataValidationIssue.update();

        dataValidationIssue = issueDataValidationService.findOpenIssue(dataValidationIssue.getId()).get();
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(3);
        Map<Channel, List<NotEstimatedBlock>> blocks = dataValidationIssue.getNotEstimatedBlocks().stream()
                .collect(Collectors.groupingBy(NotEstimatedBlock::getChannel));

        NotEstimatedBlock block1 = blocks.get(registerChannel).get(0);
        assertThat(block1.getChannel()).isEqualTo(registerChannel);
        assertThat(block1.getReadingType()).isEqualTo(registerReadingType);
        assertThat(block1.getStartTime()).isEqualTo(Instant.EPOCH);
        assertThat(block1.getEndTime()).isEqualTo(now.plus(30, ChronoUnit.MINUTES));

        NotEstimatedBlock block2 = blocks.get(channelRT1).get(0);
        assertThat(block2.getChannel()).isEqualTo(channelRT1);
        assertThat(block2.getReadingType()).isEqualTo(readingType1Min);
        assertThat(block2.getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(block2.getEndTime()).isEqualTo(now);

        NotEstimatedBlock block3 = blocks.get(channelRT2).get(0);
        assertThat(block3.getChannel()).isEqualTo(channelRT2);
        assertThat(block3.getReadingType()).isEqualTo(readingType3Min);
        assertThat(block3.getStartTime()).isEqualTo(now.minus(3, ChronoUnit.MINUTES));
        assertThat(block3.getEndTime()).isEqualTo(now.plus(3, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testUpdateIssueWithNonEstimatedBlocks() {
        Instant now = Instant.now();

        MeteringService meteringService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter", "myName").create();
        ReadingType readingType = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        Channel channel = newMeter.activate(now).getChannelsContainer().createChannel(readingType);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        OpenIssueDataValidation dataValidationIssue = issueDataValidationService.findOpenIssue(issues.get(0).getId()).get();

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

        MeteringService meteringService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
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
        newMeter.store(QualityCodeSystem.MDC, meterReading);//[now, now+10min, now+30min, now+50min, now+55min]

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);

        OpenIssueDataValidation dataValidationIssue = issueDataValidationService.findOpenIssue(issues.get(0).getId()).get();
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

        IssueProvider issueProvider = (IssueDataValidationServiceImpl) issueDataValidationService;

        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);

        OpenIssue openBaseIssue = baseIssues.get(0);

        Optional<? extends OpenIssue> openIssueDV = issueProvider.getOpenIssue(openBaseIssue);
        assertThat(openIssueDV).isPresent();
        assertThat(openIssueDV.get() instanceof OpenIssueDataValidation).isTrue();
        assertThat(openIssueDV.get().getId()).isEqualTo(openBaseIssue.getId());

        assertThat(issueProvider.getOpenIssue(openIssueDV.get()).get()).isEqualTo(openIssueDV.get());

        openIssueDV.get().close(issueService.findStatus(IssueStatus.WONT_FIX).get());

        HistoricalIssue historicalBaseIssue = issueService.query(HistoricalIssue.class).select(Condition.TRUE).get(0);

        Optional<? extends HistoricalIssue> historicalIssueDV = issueProvider.getHistoricalIssue(historicalBaseIssue);
        assertThat(historicalIssueDV).isPresent();
        assertThat(historicalIssueDV.get() instanceof HistoricalIssueDataValidation).isTrue();
        assertThat(historicalIssueDV.get().getId()).isEqualTo(historicalBaseIssue.getId());

        assertThat(issueProvider.getHistoricalIssue(historicalIssueDV.get()).get()).isEqualTo(historicalIssueDV.get());
    }

    @Test
    @Transactional
    public void testCloseIssue() {
        MeteringService meteringService = DataValidationIssueCreationRuleTemplateTest.inMemoryPersistence.getService(MeteringService.class);
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

        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        List<? extends IssueDataValidation> openIssues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(openIssues).hasSize(1);
        OpenIssueDataValidation openIssue = (OpenIssueDataValidation) openIssues.get(0);

        Instant now = Instant.now();
        openIssue.addNotEstimatedBlock(channelRT1, readingType1Min, now);//(now-1min, now]
        openIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now.plus(300, ChronoUnit.MINUTES));//(now+297min, now+300min]
        openIssue.update();

        openIssue = issueDataValidationService.findOpenIssue(openIssue.getId()).get();
        openIssue.close(issueService.findStatus(IssueStatus.WONT_FIX).get());

        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        assertThat(issueDataValidationService.findAllDataValidationIssues(filter).find()).isEmpty();
        assertThat(issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find()).hasSize(1);

        Optional<HistoricalIssueDataValidation> historicalIssue = issueDataValidationService.findHistoricalIssue(openIssue.getId());
        assertThat(historicalIssue).isPresent();
        assertThat(historicalIssue.get().getId()).isEqualTo(openIssue.getId());
        assertThat(historicalIssue.get().getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(historicalIssue.get().getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(historicalIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        List<NotEstimatedBlock> notEstimatedBlocks = historicalIssue.get().getNotEstimatedBlocks();
        assertThat(notEstimatedBlocks).hasSize(2);

        NotEstimatedBlock block1 = notEstimatedBlocks.get(0);
        assertThat(block1.getChannel()).isEqualTo(channelRT1);
        assertThat(block1.getReadingType()).isEqualTo(readingType1Min);
        assertThat(block1.getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(block1.getEndTime()).isEqualTo(now);

        NotEstimatedBlock block2 = notEstimatedBlocks.get(1);
        assertThat(block2.getChannel()).isEqualTo(channelRT2);
        assertThat(block2.getReadingType()).isEqualTo(readingType3Min);
        assertThat(block2.getStartTime()).isEqualTo(now.plus(297, ChronoUnit.MINUTES));
        assertThat(block2.getEndTime()).isEqualTo(now.plus(300, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testCloseBaseIssue() {
        ((IssueServiceImpl) issueService).addIssueProvider((IssueProvider) issueDataValidationService);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        Optional<? extends Issue> baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof OpenIssueImpl).isTrue();
        ((OpenIssue) baseIssue.get()).close(issueService.findStatus(IssueStatus.WONT_FIX).get());
        baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof HistoricalIssueImpl).isTrue();
        assertThat(baseIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        assertThat(issueDataValidationService.findHistoricalIssue(baseIssue.get().getId())).isPresent();
    }
}
