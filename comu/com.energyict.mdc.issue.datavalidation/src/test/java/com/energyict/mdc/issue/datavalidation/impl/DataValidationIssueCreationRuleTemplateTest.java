package com.energyict.mdc.issue.datavalidation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
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
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate.DeviceConfigurationInfo;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

public class DataValidationIssueCreationRuleTemplateTest extends PersistenceIntegrationTest {
    
    private static final Instant fixedTime = LocalDateTime.of(2015, 6, 16, 0, 0).toInstant(ZoneOffset.UTC);

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

    @Before
    public void setUp() throws Exception {
        issueService = inMemoryPersistence.getService(IssueService.class);
        template = inMemoryPersistence.getService(DataValidationIssueCreationRuleTemplate.class);
        ((IssueServiceImpl)issueService).addCreationRuleTemplate(template);
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
        meter = createMeter(deviceConfiguration, "Device #1");
        channel = meter.activate(fixedTime).createChannel(readingType);
        
        createRuleForDeviceConfiguration("Rule #1", deviceConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
    }
    
    @Test
    @Transactional
    public void testTemplateGetters() {
        Thesaurus thesaurus = inMemoryPersistence.getService(Thesaurus.class);
        
        assertThat(template.getDisplayName()).isEqualTo(MessageSeeds.DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME.getTranslated(thesaurus));
        assertThat(template.getDescription()).isEqualTo(MessageSeeds.DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus));
        assertThat(template.getIssueType().getId()).isEqualTo(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get().getId());        
    }
    
    @Test
    @Transactional
    @SuppressWarnings("unchecked")
    public void testTemplatePropertySpecs() {
        List<PropertySpec> propertySpecs = template.getPropertySpecs();
        
        assertThat(propertySpecs).hasSize(1);
        
        PropertySpec propertySpec = propertySpecs.get(0);
        assertThat(propertySpec.getName()).isEqualTo(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS);
        assertThat(propertySpec.getValueFactory().getClass()).isEqualTo(ListValueFactory.class);
        
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues.getAllValues()).hasSize(1);
        
        DeviceConfigurationInfo value = (DeviceConfigurationInfo) ((ListValue<?>)possibleValues.getAllValues().get(0)).getValue();
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
        Message message = mockCanntEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getChannel()).isEqualTo(channel);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getReadingType()).isEqualTo(readingType);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now);
        assertThat(issueDataValidation.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(1, ChronoUnit.MINUTES));
    }
    
    @Test
    @Transactional
    public void testFilterByDeviceConfiguration() {
        DeviceConfiguration altDeviceConfiguration = createDeviceConfiguration(deviceType, "Alternative");
        Meter meter = createMeter(altDeviceConfiguration, "Device #2");
        Channel channel = meter.activate(fixedTime).createChannel(readingType);
        
        Instant now = Instant.now();
        Message message = mockCanntEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(0);//nothing has been created because device configuration in not mentioned in the rule
        
        CreationRule rule = createRuleForDeviceConfiguration("Rule #2", deviceConfiguration, altDeviceConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
        messageHandler.process(message);
        issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getDevice().getId()).isEqualTo(meter.getId());
        assertThat(issues.get(0).getRule().getId()).isEqualTo(rule.getId());
    }
    
    @Test
    @Transactional
    public void testNotDuplicateIssueButUpdate() {
        Instant now = Instant.now();
        Message message = mockCanntEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        
        message = mockCanntEstimateDataMessage(now.plus(2, ChronoUnit.MINUTES), now.plus(2, ChronoUnit.MINUTES), channel, readingType);
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
        Message message = mockCanntEstimateDataMessage(now, now, channel, readingType);
        messageHandler.process(message);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issueDataValidation = issues.get(0);
        assertThat(issueDataValidation.getNotEstimatedBlocks()).hasSize(1);
        issueDataValidationService.findOpenIssue(issueDataValidation.getId()).get().close(issueService.findStatus(IssueStatus.RESOLVED).get());
        
        message = mockCanntEstimateDataMessage(now.plus(2, ChronoUnit.MINUTES), now.plus(2, ChronoUnit.MINUTES), channel, readingType);
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
        meter.store(reading);
        
        Message message = null;
        
        //create issue
        message = mockCanntEstimateDataMessage(fixedTime, fixedTime.plus(1, ChronoUnit.MINUTES), channel, readingType);
        messageHandler.process(message);
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        
        //update issue by removing one suspect interval of two
        message = mockSuspectDeletedMessage(fixedTime, channel, "3.5.258");
        messageHandler.process(message);
        issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issues.get(0).getNotEstimatedBlocks()).hasSize(1);
        assertThat(issues.get(0).getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(fixedTime.plus(1, ChronoUnit.MINUTES));
        assertThat(issues.get(0).getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(fixedTime.plus(2, ChronoUnit.MINUTES));

        //resolve issue completely
        message = mockSuspectDeletedMessage(fixedTime.plus(1, ChronoUnit.MINUTES), channel, "3.5.258");
        messageHandler.process(message);
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getStatus().getKey()).isEqualTo(IssueStatus.RESOLVED);
        assertThat(issues.get(0).getNotEstimatedBlocks()).isEmpty();
    }
    
    private Message mockCanntEstimateDataMessage(Instant start, Instant end, Channel channel, ReadingType readingType) {
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
    
    private Message mockSuspectDeletedMessage(Instant timeStamp, Channel channel, String readingQuality) {
        Message message = mock(Message.class);
        Map<String, Object> map = new HashMap<>();
        map.put("event.topics", "com/elster/jupiter/metering/readingquality/DELETED");
        map.put("readingTimestamp", timeStamp.toEpochMilli());
        map.put("channelId", channel.getId());
        map.put("readingQualityTypeCode", readingQuality);
        String payload = inMemoryPersistence.getService(JsonService.class).serialize(map);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }
    
    private DeviceType createDeviceType() {
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getService(DeviceConfigurationService.class);
        DeviceType deviceType = deviceConfigurationService.newDeviceType("DeviceType", mock(DeviceProtocolPluggableClass.class, Mockito.RETURNS_DEEP_STUBS));
        deviceType.save();
        return deviceType;
    }
    
    private DeviceConfiguration createDeviceConfiguration(DeviceType deviceType, String name) {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration(name).add();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        return deviceConfiguration;
    }
    
    private Meter createMeter(DeviceConfiguration deviceConfiguration, String name) {
        DeviceService deviceService = inMemoryPersistence.getService(DeviceService.class);
        Device device = deviceService.newDevice(deviceConfiguration, name, name);
        device.save();
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem  = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        return amrSystem.findMeter(String.valueOf(device.getId())).get();
    }
    
    private CreationRule createRuleForDeviceConfiguration(String name, DeviceConfiguration... deviceConfiguration) {
        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        ListValue<HasIdAndName> value = new ListValue<>();
        for(DeviceConfiguration config : deviceConfiguration) {
            HasIdAndName deviceConfig = mock(HasIdAndName.class);
            when(deviceConfig.getId()).thenReturn(config.getId());
            value.addValue(deviceConfig);
        }
        props.put(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, value);
        CreationRule creationRule = ruleBuilder.setTemplate(DataValidationIssueCreationRuleTemplate.NAME)
                   .setName(name)
                   .setReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                   .setDueInTime(DueInType.YEAR, 5)
                   .setProperties(props)
                   .complete();
        creationRule.save();
        return creationRule;
    }
}
