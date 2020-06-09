/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl.template;

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
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.BareMinimumDeviceProtocol;
import com.energyict.mdc.issue.datavalidation.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;
import com.energyict.mdc.issue.datavalidation.impl.template.DataValidationIssueCreationRuleTemplate.DeviceConfigurationInfo;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataValidationIssueCreationRuleTemplateIT {
    protected static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static final Instant fixedTime = LocalDateTime.of(2015, 6, 16, 0, 0).toInstant(ZoneOffset.UTC);
    public static InMemoryIntegrationPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(DataValidationIssueCreationRuleTemplateIT.getTransactionService());

    private DataValidationIssueCreationRuleTemplate template;
    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private IssueDataValidationService issueDataValidationService;
    private MessageHandler messageHandler;
    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;
    private Meter meter;
    private Channel channel;
    private ReadingType readingType;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeClock();
        inMemoryPersistence.initializeDatabase("DataValidationIssueCreationRuleTemplateTest", false);
        when(inMemoryPersistence.getClock().instant()).thenReturn(fixedTime);
        try (TransactionContext ctx = inMemoryPersistence.getTransactionService().getContext()) {
            inMemoryPersistence.getService(FiniteStateMachineService.class);
            inMemoryPersistence.getService(IssueDataValidationService.class);
            inMemoryPersistence.getService(DeviceDataModelService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void setUp() throws Exception {
        issueService = inMemoryPersistence.getService(IssueService.class);
        template = inMemoryPersistence.getService(DataValidationIssueCreationRuleTemplate.class);
        issueService.addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        issueDataValidationService = inMemoryPersistence.getService(IssueDataValidationService.class);
        messageHandler = inMemoryPersistence.getService(DataValidationEventHandlerFactory.class).newMessageHandler();

        deviceType = createDeviceType();
        deviceConfiguration = createDeviceConfiguration(deviceType, "Default");
        String readingTypeCode = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .flow(FlowDirection.FORWARD)
                .accumulate(Accumulation.BULKQUANTITY)
                .period(TimeAttribute.MINUTE1)
                .code();
        readingType = inMemoryPersistence.getService(MeteringService.class).createReadingType(readingTypeCode, "RT");
        meter = createMeter(deviceConfiguration, "Device #1", fixedTime);
        channel = meter.getCurrentMeterActivation().get().getChannelsContainer().createChannel(readingType);

        createRuleForDeviceConfiguration("Rule #1", deviceConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
    }

    public static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Test
    @Transactional
    public void testTemplateGetters() {
        assertThat(template.getIssueType().getId()).isEqualTo(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get().getId());
    }

    @Test
    @Transactional
    @SuppressWarnings("unchecked")
    public void testTemplatePropertySpecs() {
        List<PropertySpec> propertySpecs = template.getPropertySpecs();

        assertThat(propertySpecs).hasSize(2);

        PropertySpec propertySpec = propertySpecs.get(1);
        assertThat(propertySpec.getName()).isEqualTo(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS);

        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues.getAllValues()).hasSize(1);

        DeviceConfigurationInfo value = (DeviceConfigurationInfo) possibleValues.getAllValues().get(0);
        assertThat(value.getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(value.getName()).isEqualTo(deviceConfiguration.getName());
        assertThat(value.isActive()).isEqualTo(deviceConfiguration.isActive());
        assertThat(value.getDeviceTypeId()).isEqualTo(deviceType.getId());
        assertThat(value.getDeviceTypeName()).isEqualTo(deviceType.getName());
    }

    @Test
    @Transactional
    public void testCreateIssue() {
        Instant now = Instant.now();
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, now);
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getChannel()).isEqualTo(channel);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getReadingType()).isEqualTo(readingType);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now);
    }

    @Test
    @Transactional
    public void testFilterByDeviceConfiguration() {
        DeviceConfiguration altDeviceConfiguration = createDeviceConfiguration(deviceType, "Alternative");
        Meter meter = createMeter(altDeviceConfiguration, "Device #2", fixedTime);
        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().createChannel(readingType);

        Instant now = Instant.now();
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(0);//nothing has been created because device configuration in not mentioned in the rule

        CreationRule rule = createRuleForDeviceConfiguration("Rule #2", deviceConfiguration, altDeviceConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
        messageHandler.process(message);
        issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getDevice().getId()).isEqualTo(meter.getId());
        assertThat(issues.get(0).getRule().get().getId()).isEqualTo(rule.getId());
    }

    @Test
    @Transactional
    public void testNotDuplicateIssueButUpdate() {
        Instant now = Instant.now();
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, now);
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(1);

        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, now.plus(2, ChronoUnit.MINUTES));
        message = mockCannotEstimateDataMessage(now.plus(2, ChronoUnit.MINUTES), now.plus(2, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);

        issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(2);
    }

    @Test
    @Transactional
    public void testCreateNewIssueWhileHistoricalExists() {
        Instant now = Instant.now();
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, now);
        Message message = mockCannotEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        issueDataValidationService.findOpenIssue(issueDataValidation.getId()).get().close(issueService.findStatus(IssueStatus.RESOLVED).get());

        message = mockCannotEstimateDataMessage(now.plus(2, ChronoUnit.MINUTES), now.plus(2, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);

        issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
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
        meter.store(QualityCodeSystem.MDC, reading);

        Message message;
        //create issue
        ReadingQualityRecord readingQuality = channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, fixedTime);
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, fixedTime.plus(1, ChronoUnit.MINUTES));
        message = mockCannotEstimateDataMessage(fixedTime, fixedTime.plus(1, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.OPEN);

        //update issue by removing one suspect interval of two
        readingQuality.delete();
        message = mockSuspectDeletedMessage(fixedTime, channel, "2.5.258", readingType);
        messageHandler.process(message);
        issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issues.get(0).getNotEstimatedBlocks()).hasSize(1);
        assertThat(issues.get(0).getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(fixedTime);
        assertThat(issues.get(0).getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(fixedTime.plus(1, ChronoUnit.MINUTES));

        //resolve issue completely
        message = mockSuspectDeletedMessage(fixedTime.plus(1, ChronoUnit.MINUTES), channel, "2.5.258", readingType);
        messageHandler.process(message);
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
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

    private DeviceType createDeviceType() {
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getService(DeviceConfigurationService.class);
        ProtocolPluggableService protocolPluggableService = inMemoryPersistence.getService(ProtocolPluggableService.class);
        protocolPluggableService.addDeviceProtocolService(new BareMinimumDeviceProtocolService());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("DVALIDATION", BareMinimumDeviceProtocol.class.getName());
        deviceProtocolPluggableClass.save();
        return deviceConfigurationService.newDeviceType("DeviceType", deviceProtocolPluggableClass);
    }

    private DeviceConfiguration createDeviceConfiguration(DeviceType deviceType, String name) {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration(name).add();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        return deviceConfiguration;
    }

    private Meter createMeter(DeviceConfiguration deviceConfiguration, String name, Instant creationTime) {
        DeviceService deviceService = inMemoryPersistence.getService(DeviceService.class);
        Device device = deviceService.newDevice(deviceConfiguration, name, name, creationTime.minusMillis(1));
        device.save();
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());

        User user = mock(User.class);
        when(user.getName()).thenReturn("name");
        when(user.getLocale()).thenReturn(Optional.empty());
        inMemoryPersistence.getThreadPrincipalService().set(user);
        when(user.hasPrivilege(anyString(), (Privilege) Matchers.any())).thenReturn(true);

        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();
        // Business method
        installAndActivateAction.execute(creationTime, Collections.emptyList());

        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        return amrSystem.findMeter(String.valueOf(device.getId())).get();
    }

    private CreationRule createRuleForDeviceConfiguration(String name, DeviceConfiguration... deviceConfigurations) {
        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        List<HasIdAndName> value = new ArrayList<>();
        for (DeviceConfiguration config : deviceConfigurations) {
            HasIdAndName deviceConfig = mock(HasIdAndName.class);
            when(deviceConfig.getId()).thenReturn(config.getId());
            value.add(deviceConfig);
        }
        List<HasIdAndName> value2 = new ArrayList<>();
        HasIdAndName deviceLife = mock(HasIdAndName.class);
        String deviceLifeId = deviceType.getId() + ":" + deviceType.getDeviceLifeCycle().getId() + ":" + deviceType.getDeviceLifeCycle()
                .getFiniteStateMachine()
                .getStates()
                .stream()
                .sorted(Comparator.comparing(State::getId))
                .map(HasId::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        when(deviceLife.getId()).thenReturn(deviceLifeId);
        value2.add(deviceLife);
        props.put(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, value);
        props.put(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, value2);
        return ruleBuilder.setTemplate(DataValidationIssueCreationRuleTemplate.NAME)
                .setName(name)
                .setIssueType(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get())
                .setReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                .setPriority(Priority.DEFAULT)
                .activate()
                .setDueInTime(DueInType.YEAR, 5)
                .setProperties(props)
                .complete();
    }

    private static class BareMinimumDeviceProtocolService implements DeviceProtocolService {
        @Override
        public Object createProtocol(String className) {
            if (BareMinimumDeviceProtocol.class.getName().equals(className)) {
                return new BareMinimumDeviceProtocol();
            } else {
                return null;
            }
        }
    }

}
