/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.insight.issue.datavalidation.UsagePointDataValidationIssueFilter;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Ignore
public class UsagePointDataValidationIssueCreationRuleTemplateTest {

    protected static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static final Instant fixedTime = LocalDateTime.of(2015, 6, 16, 0, 0).toInstant(ZoneOffset.UTC);
    static InMemoryIntegrationPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(UsagePointDataValidationIssueCreationRuleTemplateTest.getTransactionService());

    private UsagePointDataValidationIssueCreationRuleTemplate template;
    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    private MessageHandler messageHandler;
    private MetrologyConfiguration metrologyConfiguration;
    private UsagePoint usagePoint;
    private Channel channel;
    private ReadingType readingType;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeClock();
        inMemoryPersistence.initializeDatabase("UsagePointDataValidationIssueCreationRuleTemplateTest", false);
        when(inMemoryPersistence.getClock().instant()).thenReturn(fixedTime);
        try (TransactionContext ctx = inMemoryPersistence.getTransactionService().getContext()) {
            inMemoryPersistence.getService(FiniteStateMachineService.class);
            inMemoryPersistence.getService(UsagePointIssueDataValidationService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void setUp() throws Exception {
        issueService = inMemoryPersistence.getService(IssueService.class);
        template = inMemoryPersistence.getService(UsagePointDataValidationIssueCreationRuleTemplate.class);
        ((IssueServiceImpl) issueService).addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        usagePointIssueDataValidationService = inMemoryPersistence.getService(UsagePointIssueDataValidationService.class);
        messageHandler = inMemoryPersistence.getService(DataValidationEventHandlerFactory.class).newMessageHandler();

        metrologyConfiguration = createMetrologyConfiguration("Default", mock(List.class));
        String readingTypeCode = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .flow(FlowDirection.FORWARD)
                .accumulate(Accumulation.BULKQUANTITY)
                .period(TimeAttribute.MINUTE1)
                .code();
        readingType = inMemoryPersistence.getService(MeteringService.class).createReadingType(readingTypeCode, "RT");
        usagePoint = createUsagePoint(metrologyConfiguration, "Device #1", fixedTime);
        channel = usagePoint.getCurrentMeterActivations().stream().findFirst().get().getChannelsContainer().createChannel(readingType);

        createRuleForMetrologyConfiguration("Rule #1", metrologyConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
    }

    protected static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Test
    @Transactional
    public void testTemplateGetters() {
        assertThat(template.getIssueType().getId()).isEqualTo(issueService.findIssueType(UsagePointIssueDataValidationService.ISSUE_TYPE_NAME).get().getId());
    }

    @Test
    @Transactional
    @SuppressWarnings("unchecked")
    public void testTemplatePropertySpecs() {
        List<PropertySpec> propertySpecs = template.getPropertySpecs();

        assertThat(propertySpecs).hasSize(1);

        PropertySpec propertySpec = propertySpecs.get(0);
        assertThat(propertySpec.getName()).isEqualTo(UsagePointDataValidationIssueCreationRuleTemplate.METROLOGY_CONFIGS);

        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues.getAllValues()).hasSize(1);

        //TODO UsagePointInfo
    }

    @Test
    @Transactional
    public void testCreateIssue() {
        Instant now = Instant.now();
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, now);
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        UsagePointIssueDataValidation usagePointIssueDataValidation = issues.get(0);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks().get(0).getChannel()).isEqualTo(channel);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks().get(0).getReadingType()).isEqualTo(readingType);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now);
    }

    @Test
    @Transactional
    public void testFilterByMetrologyConfiguration() {
        MetrologyConfiguration altMetroConfig = createMetrologyConfiguration("Alternative", mock(List.class));
        UsagePoint usagePoint = createUsagePoint(altMetroConfig, "UP #2", fixedTime);
        Channel channel = usagePoint.getCurrentMeterActivations().stream().findFirst().get().getChannelsContainer().createChannel(readingType);

        Instant now = Instant.now();
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(0);//nothing has been created because device configuration in not mentioned in the rule

        CreationRule rule = createRuleForMetrologyConfiguration("Rule #2", metrologyConfiguration, altMetroConfig);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
        messageHandler.process(message);
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getDevice().getId()).isEqualTo(usagePoint.getId());
        assertThat(issues.get(0).getRule().get().getId()).isEqualTo(rule.getId());
    }

    @Test
    @Transactional
    public void testNotDuplicateIssueButUpdate() {
        Instant now = Instant.now();
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, now);
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        UsagePointIssueDataValidation usagePointIssueDataValidation = issues.get(0);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks()).hasSize(1);

        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, now.plus(2, ChronoUnit.MINUTES));
        message = mockCannotEstimateDataMessage(now.plus(2, ChronoUnit.MINUTES), now.plus(2, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);

        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        usagePointIssueDataValidation = issues.get(0);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks()).hasSize(2);
    }

    @Test
    @Transactional
    public void testCreateNewIssueWhileHistoricalExists() {
        Instant now = Instant.now();
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, now);
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        UsagePointIssueDataValidation usagePointIssueDataValidation = issues.get(0);
        assertThat(usagePointIssueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        usagePointIssueDataValidationService.findOpenIssue(usagePointIssueDataValidation.getId()).get().close(issueService.findStatus(IssueStatus.RESOLVED).get());

        message = mockCannotEstimateDataMessage(now.plus(2, ChronoUnit.MINUTES), now.plus(2, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);

        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(2);//open and closed
    }

    @Test
    @Transactional
    public void testResolveIssue() {
        MeterReadingImpl reading = MeterReadingImpl.newInstance();
        IntervalBlockImpl block = IntervalBlockImpl.of(readingType.getMRID());
        block.addIntervalReading(IntervalReadingImpl.of(fixedTime, BigDecimal.valueOf(50L)));
        block.addIntervalReading(IntervalReadingImpl.of(fixedTime.plus(1, ChronoUnit.MINUTES), BigDecimal.valueOf(50L)));
        reading.addIntervalBlock(block);

        Message message;
        //create issue
        ReadingQualityRecord readingQuality = channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, fixedTime);
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, fixedTime.plus(1, ChronoUnit.MINUTES));
        message = mockCannotEstimateDataMessage(fixedTime, fixedTime.plus(1, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);
        List<? extends UsagePointIssueDataValidation> issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        //update issue by removing one suspect interval of two
        readingQuality.delete();
        message = mockSuspectDeletedMessage(fixedTime, channel, "2.5.258", readingType);
        messageHandler.process(message);
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(new UsagePointDataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issues.get(0).getNotEstimatedBlocks()).hasSize(1);
        assertThat(issues.get(0).getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(fixedTime);
        assertThat(issues.get(0).getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(fixedTime.plus(1, ChronoUnit.MINUTES));

        //resolve issue completely
        message = mockSuspectDeletedMessage(fixedTime.plus(1, ChronoUnit.MINUTES), channel, "2.5.258", readingType);
        messageHandler.process(message);
        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        issues = usagePointIssueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.RESOLVED);
        assertThat(issues.get(0).getNotEstimatedBlocks()).isEmpty();
    }

    private Message mockCannotEstimateDataMessage(Instant start, Instant end, Channel channel, ReadingType readingType) {
        Message message = mock(Message.class);
        Map<String, Object> map = new HashMap<>();
        map.put("event.topics", "com/elster/jupiter/estimation/estimationblock/FAILURE");
        map.put("startTime", start.toEpochMilli());
        map.put("endTime", end.toEpochMilli());
        map.put("channelId", channel.getId());
        map.put("readingType", readingType.getMRID());
        String payload = inMemoryPersistence.getService(JsonService.class).serialize(map);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    private Message mockSuspectDeletedMessage(Instant timeStamp, Channel channel, String readingQuality, ReadingType readingType) {
        Message message = mock(Message.class);
        Map<String, Object> map = new HashMap<>();
        map.put("event.topics", "com/elster/jupiter/metering/readingquality/DELETED");
        map.put("readingTimestamp", timeStamp.toEpochMilli());
        map.put("channelId", channel.getId());
        map.put("readingQualityTypeCode", readingQuality);
        map.put("readingType", readingType.getMRID());
        String payload = inMemoryPersistence.getService(JsonService.class).serialize(map);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    private MetrologyConfiguration createMetrologyConfiguration(String name, List<ReadingType> readingTypes) {
        ServiceCategory serviceCategory = inMemoryPersistence.getService(MeteringService.class).getServiceCategory(ServiceKind.ELECTRICITY).get();
        MetrologyConfigurationService metrologyConfigurationService = inMemoryPersistence.getService(MetrologyConfigurationService.class);
        MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION).get();
        MeterRole meterRoleDefault = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();

        UsagePointMetrologyConfiguration mc = metrologyConfigurationService.newUsagePointMetrologyConfiguration(name, serviceCategory).create();
        mc.addMeterRole(meterRoleDefault);
        MetrologyContract metrologyContract = mc.addMandatoryMetrologyContract(purpose);
        for (ReadingType readingType : readingTypes) {
            FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = mc.newReadingTypeRequirement(readingType.getFullAliasName(), meterRoleDefault)
                    .withReadingType(readingType);
            ReadingTypeDeliverableBuilder builder = metrologyContract.newReadingTypeDeliverable(readingType.getFullAliasName(), readingType, Formula.Mode.AUTO);
            builder.build(builder.requirement(fullySpecifiedReadingTypeRequirement));
        }
        mc.activate();
        return mc;
    }

    private UsagePoint createUsagePoint(MetrologyConfiguration metrologyConfiguration, String name, Instant creationTime) {
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new IllegalStateException(ServiceKind.ELECTRICITY.getDisplayName() + " is not available"));
        UsagePointBuilder usagePointBuilder = serviceCategory.newUsagePoint(name, Instant.now());
        return spy(usagePointBuilder.create());
    }

    private CreationRule createRuleForMetrologyConfiguration(String name, MetrologyConfiguration... deviceConfigurations) {
        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        List<HasIdAndName> value = new ArrayList<>();
        for (MetrologyConfiguration config : deviceConfigurations) {
            HasIdAndName deviceConfig = mock(HasIdAndName.class);
            when(deviceConfig.getId()).thenReturn(config.getId());
            value.add(deviceConfig);
        }
        props.put(UsagePointDataValidationIssueCreationRuleTemplate.METROLOGY_CONFIGS, value);
        return ruleBuilder.setTemplate(UsagePointDataValidationIssueCreationRuleTemplate.NAME)
                .setName(name)
                .setIssueType(issueService.findIssueType(UsagePointIssueDataValidationService.ISSUE_TYPE_NAME).get())
                .setReason(issueService.findReason(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                .setPriority(Priority.DEFAULT)
                .activate()
                .setDueInTime(DueInType.YEAR, 5)
                .setProperties(props)
                .complete();
    }

}
