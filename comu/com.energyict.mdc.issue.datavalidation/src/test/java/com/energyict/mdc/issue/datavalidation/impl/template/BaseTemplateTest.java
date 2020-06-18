package com.energyict.mdc.issue.datavalidation.impl.template;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.HasIdAndName;
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
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.BareMinimumDeviceProtocol;
import com.energyict.mdc.issue.datavalidation.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseTemplateTest {

    protected static final InMemoryIntegrationPersistence inMemoryPersistence = new InMemoryIntegrationPersistence();

    protected static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    protected static final Instant TIME = LocalDateTime.of(2015, 6, 16, 0, 0).toInstant(ZoneOffset.UTC);

    protected IssueService issueService;
    protected IssueCreationService issueCreationService;
    protected IssueDataValidationService issueDataValidationService;
    protected MessageHandler messageHandler;
    protected MeteringService meteringService;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    protected Message mockCannotEstimateDataMessage(Instant start, Instant end, Channel channel, ReadingType readingType) {
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

    protected Message mockSuspectDeletedMessage(Instant timeStamp, Channel channel, String readingQuality, ReadingType readingType) {
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

    protected DeviceType createDeviceType() {
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getService(DeviceConfigurationService.class);
        ProtocolPluggableService protocolPluggableService = inMemoryPersistence.getService(ProtocolPluggableService.class);
        protocolPluggableService.addDeviceProtocolService(new BareMinimumDeviceProtocolService());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("DVALIDATION", BareMinimumDeviceProtocol.class.getName());
        deviceProtocolPluggableClass.save();
        return deviceConfigurationService.newDeviceType("DeviceType", deviceProtocolPluggableClass);
    }

    protected DeviceConfiguration createDeviceConfiguration(DeviceType deviceType, String name) {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration(name).add();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        return deviceConfiguration;
    }

    protected Meter createMeter(DeviceConfiguration deviceConfiguration, String name, Instant creationTime) {
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

    protected CreationRule createRuleForDeviceConfiguration(String name, DeviceType deviceType, DeviceConfiguration... deviceConfigurations) {
        IssueCreationService.CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
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

    protected String createReadingTypeCode() {
        return ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .flow(FlowDirection.FORWARD)
                .accumulate(Accumulation.BULKQUANTITY)
                .period(TimeAttribute.MINUTE1)
                .code();
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
