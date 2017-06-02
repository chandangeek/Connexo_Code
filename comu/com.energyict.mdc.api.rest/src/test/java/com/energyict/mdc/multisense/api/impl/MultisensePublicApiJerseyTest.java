/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.devtools.tests.Matcher;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.ws.rs.core.Application;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultisensePublicApiJerseyTest extends FelixRestApplicationJerseyTest {
    private static final String MANUFACTURER = "The Manufacturer";
    private static final String MODELNBR = "The ModelNumber";
    private static final String MODELVERSION = "The modelVersion";
    private static final Pattern MRID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final String MRID_REPLACEMENT = "$1-$2-$3-$4-$5";
    @Mock
    DeviceService deviceService;
    @Mock
    TopologyService topologyService;
    @Mock
    BatchService batchService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    IssueService issueService;
    @Mock
    DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    FiniteStateMachineService finiteStateMachineService;
    @Mock
    ConnectionTaskService connectionTaskService;
    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    TaskService taskService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    Clock clock;

    static ProtocolPluggableService protocolPluggableService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    DeviceMessageService deviceMessageService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    MeteringService meteringService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    DeviceAlarmService deviceAlarmService;
    @Mock
    ThreadPrincipalService threadPrincipalService;


    @BeforeClass
    public static void before() {
        protocolPluggableService = mock(ProtocolPluggableService.class);
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return new UPLAuthenticationLevelAdapter((com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel) args[0]);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return new UPLEncryptionLevelAdapter((com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel) args[0]);
        })        ;
    }

    @Override
    protected Application getApplication() {
        PublicRestApplication application = new PublicRestApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setDeviceService(deviceService);
        application.setTopologyService(topologyService);
        application.setBatchService(batchService);
        application.setIssueService(issueService);
        application.setDeviceAlarmService(deviceAlarmService);
        application.setDeviceLifeCycleService(deviceLifeCycleService);
        application.setFiniteStateMachineService(finiteStateMachineService);
        application.setConnectionTaskService(connectionTaskService);
        application.setEngineConfigurationService(engineConfigurationService);
        application.setClock(clock);
        application.setTaskService(taskService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setSchedulingService(schedulingService);
        application.setDeviceMessageService(deviceMessageService);
        application.setCommunicationTaskService(communicationTaskService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setMeteringService(meteringService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setThreadPrincipalService(threadPrincipalService);
        return application;
    }

    public ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.<ReadingType>empty());
        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getVersion()).thenReturn(3333L);
        return readingType;
    }

    Device mockDevice(String mRID, String serial, DeviceConfiguration deviceConfiguration, long version) {
        Device mock = mock(Device.class);
        when(mock.getmRID()).thenReturn(mRID);
        when(mock.getName()).thenReturn("Device" + serial);
        long deviceId = (long) mRID.hashCode();
        when(mock.getId()).thenReturn(deviceId);
        when(mock.getSerialNumber()).thenReturn(serial);
        when(mock.getManufacturer()).thenReturn(MANUFACTURER);
        when(mock.getModelNumber()).thenReturn(MODELNBR);
        when(mock.getModelVersion()).thenReturn(MODELVERSION);
        when(mock.getVersion()).thenReturn(333L);
        State state = mock(State.class);
        when(state.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(mock.getState()).thenReturn(state);
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Register register = mock(Register.class);
        when(register.getRegisterSpecId()).thenReturn(666L);
        when(mock.getRegisters()).thenReturn(Collections.singletonList(register));
        Batch batch = mock(Batch.class);
        when(batch.getName()).thenReturn("BATCH A");
        when(mock.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(mock.getBatch()).thenReturn(Optional.of(batch));
        when(topologyService.getPhysicalGateway(mock)).thenReturn(Optional.empty());
        when(deviceService.findDeviceByMrid(mRID)).thenReturn(Optional.of(mock));
        when(deviceService.findAndLockDeviceByIdAndVersion(deviceId, version)).thenReturn(Optional.of(mock));
        when(deviceService.findAndLockDeviceByIdAndVersion(eq(deviceId), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceService.findAndLockDeviceBymRIDAndVersion(eq(mRID), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceService.findAndLockDeviceBymRIDAndVersion(eq(mRID), eq(version))).thenReturn(Optional.of(mock));
        when(mock.getVersion()).thenReturn(version);
        return mock;
    }

    DeviceType mockDeviceType(long id, String name, long version) {
        DeviceProtocolDialect dialect1 = mock(DeviceProtocolDialect.class);
        when(dialect1.getDeviceProtocolDialectName()).thenReturn("ProtocolDialect1");
        DeviceProtocolDialect dialect2 = mock(DeviceProtocolDialect.class);
        when(dialect1.getDeviceProtocolDialectName()).thenReturn("ProtocolDialect2");
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(dialect1, dialect2));
        DeviceType mock = mock(DeviceType.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1000 + id, "Default", mock, 3333L);
        when(mock.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getId()).thenReturn(id * id);
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(mock.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findDeviceType(id)).thenReturn(Optional.of(mock));
        when(deviceConfigurationService.findAndLockDeviceType(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockDeviceType(id, version)).thenReturn(Optional.of(mock));
        when(mock.getVersion()).thenReturn(version);
        return mock;
    }

    DeviceConfiguration mockDeviceConfiguration(long id, String name, long version) {
        DeviceConfiguration mock = mock(DeviceConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getVersion()).thenReturn(version);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, version)).thenReturn(Optional.of(mock));
        when(deviceConfigurationService.findDeviceConfiguration(id)).thenReturn(Optional.of(mock));
        return mock;
    }

    DeviceConfiguration mockDeviceConfiguration(long id, String name, DeviceType deviceType, long version) {
        DeviceConfiguration mock = mockDeviceConfiguration(id, name, version);
        when(mock.getDeviceType()).thenReturn(deviceType);
        return mock;
    }

    PropertySpec mockStringPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("string.property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn("default");
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);

        return propertySpec;
    }


    PropertySpec mockBigDecimalPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("decimal.property");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(BigDecimal.ONE);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    PropertySpec mockExhaustiveListPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("list.property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.isExhaustive()).thenReturn(true);
        when(possibleValues.getAllValues()).thenReturn(Arrays.asList("Value1", "Value2", "Value3"));
        when(possibleValues.getDefault()).thenReturn("Value1");
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    PropertySpec mockDatePropertySpec(Date defaultValue) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("date.property");
        when(propertySpec.getValueFactory()).thenReturn(new DateFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(defaultValue);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    ScheduledConnectionTask mockScheduledConnectionTask(long id, String name, long version) {
        ScheduledConnectionTask mock = mock(ScheduledConnectionTask.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(connectionTaskService.findConnectionTask(id)).thenReturn(Optional.of(mock));
        when(mock.getVersion()).thenReturn(version);
        return mock;
    }

    ScheduledConnectionTask mockScheduledConnectionTask(long id, String name, Device deviceXas, OutboundComPortPool comPortPool, PartialScheduledConnectionTask partial, long version) {
        ScheduledConnectionTask mock = mock(ScheduledConnectionTask.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.isDefault()).thenReturn(true);
        when(mock.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(mock.getDevice()).thenReturn(deviceXas);
        when(mock.getNumberOfSimultaneousConnections()).thenReturn(2);
        when(mock.getComPortPool()).thenReturn(comPortPool);
        when(mock.getPartialConnectionTask()).thenReturn(partial);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(mock.getConnectionType()).thenReturn(connectionType);
        when(mock.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(mock.getRescheduleDelay()).thenReturn(TimeDuration.minutes(60));
        when(mock.getCommunicationWindow()).thenReturn(new ComWindow(PartialTime.fromHours(2), PartialTime.fromHours(4)));
        when(mock.getVersion()).thenReturn(version);
        when(connectionTaskService.findConnectionTask(id)).thenReturn(Optional.of(mock));
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(id, version)).thenReturn(Optional.of(mock));
        return mock;
    }

    InboundConnectionTask mockInboundConnectionTask(long id, String name, Device deviceXas, InboundComPortPool comPortPool, PartialInboundConnectionTask partial, long version) {
        InboundConnectionTask mock = mock(InboundConnectionTask.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.isDefault()).thenReturn(true);
        when(mock.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(mock.getDevice()).thenReturn(deviceXas);
        when(mock.getComPortPool()).thenReturn(comPortPool);
        when(mock.getPartialConnectionTask()).thenReturn(partial);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(mock.getConnectionType()).thenReturn(connectionType);
        when(mock.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(mock.getVersion()).thenReturn(version);

        when(connectionTaskService.findConnectionTask(id)).thenReturn(Optional.of(mock));
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(id, version)).thenReturn(Optional.of(mock));
        return mock;
    }

    PartialScheduledConnectionTask mockPartialScheduledConnectionTask(long id, String name, long version, PropertySpec... propertySpecs) {
        PartialScheduledConnectionTask mock = mock(PartialScheduledConnectionTask.class);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(mock.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        when(connectionTaskPluggeableClass.getName()).thenReturn("outbound pluggeable class");
        when(connectionTaskPluggeableClass.getPropertySpecs()).thenReturn(Arrays.asList(propertySpecs));
        when(mock.getVersion()).thenReturn(version);

        return mock;
    }

    PartialInboundConnectionTask mockPartialInboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig, long version, ProtocolDialectConfigurationProperties dialectProperties) {
        PartialInboundConnectionTask mock = mock(PartialInboundConnectionTask.class);
        when(mock.getName()).thenReturn(name);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(mock.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(mock.getId()).thenReturn(id);
        when(mock.getConfiguration()).thenReturn(deviceConfig);
        when(connectionTaskPluggeableClass.getName()).thenReturn("inbound pluggeable class");
        InboundComPortPool comPortPool = mockInboundComPortPool(65L, 3333L);
        when(mock.getComPortPool()).thenReturn(comPortPool);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(mock.getConnectionType()).thenReturn(connectionType);
        when(mock.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(mock.getVersion()).thenReturn(version);
        when(mock.getProtocolDialectConfigurationProperties()).thenReturn(dialectProperties);
        return mock;
    }

    PartialScheduledConnectionTask mockPartialOutboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig, long version, ProtocolDialectConfigurationProperties dialectProperties) {
        PartialScheduledConnectionTask mock = mock(PartialScheduledConnectionTask.class);
        when(mock.getName()).thenReturn(name);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(mock.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(mock.getId()).thenReturn(id);
        when(mock.getConfiguration()).thenReturn(deviceConfig);
        when(connectionTaskPluggeableClass.getName()).thenReturn("outbound pluggeable class");
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(165L);
        when(mock.getComPortPool()).thenReturn(comPortPool);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(mock.getConnectionType()).thenReturn(connectionType);
        when(mock.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(mock.getNumberOfSimultaneousConnections()).thenReturn(2);
        when(mock.getRescheduleDelay()).thenReturn(TimeDuration.minutes(60));
        when(mock.getCommunicationWindow()).thenReturn(new ComWindow(PartialTime.fromHours(2), PartialTime.fromHours(4)));
        when(mock.getVersion()).thenReturn(version);
        when(mock.getProtocolDialectConfigurationProperties()).thenReturn(dialectProperties);
        return mock;
    }

    private InboundComPortPool mockInboundComPortPool(long id, long version) {
        InboundComPortPool mock = mock(InboundComPortPool.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getVersion()).thenReturn(version);

        return mock;
    }

    ComTask mockComTask(long id, String name, long version, ProtocolTask... protocolTasks) {
        ComTask mock = mock(ComTask.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
        when(taskService.findComTask(id)).thenReturn(Optional.of(mock));
        when(mock.getVersion()).thenReturn(version);

        return mock;
    }

    DeviceMessageCategory mockDeviceMessageCategory(int id, String name, DeviceMessageSpec... specs) {
        DeviceMessageCategory mock = mock(DeviceMessageCategory.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getDescription()).thenReturn("Description of " + name);
        if (specs != null && specs.length > 0) {
            doReturn(Arrays.asList(specs)).when(mock).getMessageSpecifications();
        }
        when(deviceMessageSpecificationService.findCategoryById(id)).thenReturn(Optional.of(mock));

        return mock;
    }

    DeviceMessageSpec mockDeviceMessageSpec(DeviceMessageId deviceMessageId, String name) {
        DeviceMessageSpec mock = mock(DeviceMessageSpec.class);
        when(mock.getId()).thenReturn(deviceMessageId);
        when(mock.getName()).thenReturn(name);

        when(deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue())).thenReturn(Optional.of(mock));
        return mock;
    }

    ClockTask mockClockTask(long id) {
        ClockTask mock = mock(ClockTask.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);

        when(taskService.findProtocolTask(id)).thenReturn(Optional.of(mock));
        return mock;
    }

    DeviceProtocolPluggableClass mockPluggableClass(long id, String name, String version) {
        DeviceProtocolPluggableClass mock = mock(DeviceProtocolPluggableClass.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getJavaClassName()).thenReturn("com.energyict.prot." + name + ".class");
        when(mock.getVersion()).thenReturn(version);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(id)).thenReturn(Optional.of(mock));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.emptyList());
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.emptyList());
        when(mock.getDeviceProtocol()).thenReturn(deviceProtocol);
        return mock;
    }

    DeviceProtocolPluggableClass mockPluggableClass(long id, String name, String version,
                                                    List<AuthenticationDeviceAccessLevel> authAccessLvls,
                                                    List<EncryptionDeviceAccessLevel> encAccessLvls) {
        DeviceProtocolPluggableClass mock = mock(DeviceProtocolPluggableClass.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getJavaClassName()).thenReturn("com.energyict.prot." + name + ".class");
        when(mock.getVersion()).thenReturn(version);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(id)).thenReturn(Optional.of(mock));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> adaptedAuthLevels = authAccessLvls.stream().map(CXOAuthenticationLevelAdapter::new).collect(Collectors.toList());
        List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> adaptedEncrLevels = encAccessLvls.stream().map(CXOEncryptionLevelAdapter::new).collect(Collectors.toList());

        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(adaptedAuthLevels);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(adaptedEncrLevels);
        when(mock.getDeviceProtocol()).thenReturn(deviceProtocol);

        return mock;
    }

    AuthenticationDeviceAccessLevel mockAuthenticationAccessLevel(int id) {
        AuthenticationDeviceAccessLevel mock = mock(AuthenticationDeviceAccessLevel.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTranslation()).thenReturn("Proper name for " + id);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(mock.getSecurityProperties()).thenReturn(Collections.singletonList(propertySpec));
        return mock;
    }

    EncryptionDeviceAccessLevel mockEncryptionAccessLevel(int id) {
        EncryptionDeviceAccessLevel mock = mock(EncryptionDeviceAccessLevel.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTranslation()).thenReturn("Proper name for " + id);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(mock.getSecurityProperties()).thenReturn(Collections.singletonList(propertySpec));
        return mock;
    }

    ComTaskEnablement mockComTaskEnablement(ComTask comTask, DeviceConfiguration deviceConfiguration, long version) {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getPriority()).thenReturn(-19);
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(comTaskEnablement.getVersion()).thenReturn(version);
        return comTaskEnablement;
    }

    ComSchedule mockComSchedule(long scheduleId, String name, long version) {
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getId()).thenReturn(scheduleId);
        when(comSchedule.getName()).thenReturn(name);
        when(comSchedule.getmRID()).thenReturn(Optional.empty());
        when(comSchedule.getPlannedDate()).thenReturn(Optional.empty());
        when(comSchedule.getVersion()).thenReturn(version);

        when(schedulingService.findSchedule(scheduleId)).thenReturn(Optional.of(comSchedule));
        when(schedulingService.findAndLockComScheduleByIdAndVersion(eq(scheduleId), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(schedulingService.findAndLockComScheduleByIdAndVersion(scheduleId, version)).thenReturn(Optional.of(comSchedule));
        return comSchedule;
    }

    ComSchedule mockComSchedule(long scheduleId, String name, Optional<String> mRID, Optional<Instant> plannedDate, long version) {
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getId()).thenReturn(scheduleId);
        when(comSchedule.getName()).thenReturn(name);
        when(comSchedule.getmRID()).thenReturn(mRID);
        when(comSchedule.getPlannedDate()).thenReturn(plannedDate);
        when(comSchedule.getPlannedDate()).thenReturn(plannedDate);
        when(comSchedule.getVersion()).thenReturn(version);

        when(schedulingService.findSchedule(scheduleId)).thenReturn(Optional.of(comSchedule));
        return comSchedule;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }


    protected DeviceMessageEnablement mockDeviceMessageEnablement(long id, DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId) {
        DeviceMessageEnablement mock = mock(DeviceMessageEnablement.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(mock.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(mock.getUserActions()).thenReturn(Collections.singleton(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1));

        return mock;
    }

    protected MessagesTask mockMessagesTask(long id, DeviceMessageCategory... deviceMessageCategories) {
        MessagesTask messagesTask = mock(MessagesTask.class);
        when(messagesTask.getId()).thenReturn(id);
        if (deviceMessageCategories != null && deviceMessageCategories.length > 0) {
            when(messagesTask.getDeviceMessageCategories()).thenReturn(Arrays.asList(deviceMessageCategories));
        }
        return messagesTask;
    }

    protected DeviceMessage mockDeviceMessage(long id, Device mockDevice, DeviceMessageSpec specification, Optional<Instant> now, long version) {
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getId()).thenReturn(id);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(deviceMessage.getSentDate()).thenReturn(now);
        when(deviceMessage.getDevice()).thenReturn(mockDevice);
        when(deviceMessage.getSpecification()).thenReturn(specification);
        when(deviceMessage.getVersion()).thenReturn(version);

        return deviceMessage;
    }

    protected ComTaskExecution mockScheduledComTaskExecution(long id, ComSchedule comSchedule, Device device, long version) {
        ComTaskExecution scheduledComTaskExecution = mock(ComTaskExecution.class);
        when(scheduledComTaskExecution.getComSchedule()).thenReturn(Optional.of(comSchedule));
        when(scheduledComTaskExecution.getId()).thenReturn(id);
        when(scheduledComTaskExecution.getDevice()).thenReturn(device);
        when(scheduledComTaskExecution.getVersion()).thenReturn(version);
        when(communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(id, version)).thenReturn(Optional.of(scheduledComTaskExecution));
        return scheduledComTaskExecution;
    }

    protected SecurityPropertySet mockSecurityPropertySet(long id, DeviceConfiguration deviceConfiguration, String name, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel, AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, long version) {
        SecurityPropertySet mock = mock(SecurityPropertySet.class);
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec));
        when(mock.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(mock.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(mock.getVersion()).thenReturn(version);
        when(deviceConfigurationService.findSecurityPropertySet(id)).thenReturn(Optional.of(mock));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(id, version)).thenReturn(Optional.of(mock));
        return mock;
    }

    protected UsagePoint mockUsagePoint(long id, String name, long version, ServiceKind serviceKind) {
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        UsagePointDetail detail;
        switch (serviceKind) {
            case ELECTRICITY:
                detail = mock(ElectricityDetail.class);
                break;
            case GAS:
                detail = mock(GasDetail.class);
                break;
            case WATER:
                detail = mock(WaterDetail.class);
                break;
            case HEAT:
                detail = mock(HeatDetail.class);
                break;
            default:
                detail = null;
                break;
        }
        return mockUsagePoint(id, name, version, extension, serviceKind, detail);
    }

    protected UsagePoint mockUsagePoint(long id, String name, long version, ServiceKind serviceKind, UsagePointDetail detail) {
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        return mockUsagePoint(id, name, version, extension, serviceKind, detail);
    }

    private UsagePoint mockUsagePoint(long id, String name, long version, UsagePointCustomPropertySetExtension extension, ServiceKind serviceKind, UsagePointDetail detail) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(id);
        when(usagePoint.getVersion()).thenReturn(version);
        when(usagePoint.getName()).thenReturn(name);
        when(usagePoint.getAliasName()).thenReturn("alias " + name);
        when(usagePoint.getDescription()).thenReturn("usage point desc");
        when(usagePoint.getOutageRegion()).thenReturn("outage region");
        when(usagePoint.getReadRoute()).thenReturn("read route");
        when(usagePoint.getServiceLocationString()).thenReturn("location");
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        doReturn(Optional.ofNullable(detail)).when(usagePoint).getDetail(any(Instant.class));
        when(usagePoint.getMRID()).thenReturn(mockMRID(id));
        when(usagePoint.getInstallationTime()).thenReturn(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC));
        when(usagePoint.getServiceDeliveryRemark()).thenReturn("remark");
        when(usagePoint.getServicePriority()).thenReturn("service priority");
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        when(usagePoint.forCustomProperties()).thenReturn(extension);
        when(meteringService.findUsagePointById(id)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(eq(id), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional
                .empty());
        when(meteringService.findAndLockUsagePointByIdAndVersion(id, version)).thenReturn(Optional.of(usagePoint));
        return usagePoint;
    }

    protected UsagePointPropertySet mockUsagePointPropertySet(long id, CustomPropertySet cps, UsagePoint usagePoint, UsagePointCustomPropertySetExtension extension) {
        UsagePointPropertySet mock = mock(UsagePointPropertySet.class);
        when(mock.getUsagePoint()).thenReturn(usagePoint);
        when(mock.getCustomPropertySet()).thenReturn(cps);
        when(mock.getId()).thenReturn(id);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty("name", "Valerie");
        values.setProperty("age", BigDecimal.valueOf(21));
        when(mock.getValues()).thenReturn(values);
        when(extension.getPropertySet(id)).thenReturn(mock);
        return mock;
    }

    protected PropertySpec mockDateTimePropertySpec(Date date) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("datetime.property");
        when(propertySpec.getValueFactory()).thenReturn(new DateAndTimeFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(date);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    protected UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name, long version) {
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(id);
        when(metrologyConfiguration.getName()).thenReturn(name);
        when(metrologyConfiguration.getVersion()).thenReturn(version);
        when(metrologyConfigurationService.findMetrologyConfiguration(id)).thenReturn(Optional.of(metrologyConfiguration));
        return metrologyConfiguration;
    }

    protected static String mockMRID(long id) {
        return MRID_PATTERN.matcher(String.format("%032x", id)).replaceAll(MRID_REPLACEMENT);
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal) {
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        return status;
    }

    protected IssueReason mockReason(String key, String name, IssueType issueType) {
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        when(reason.getIssueType()).thenReturn(issueType);
        return reason;
    }


    protected IssueStatus getDefaultOpenStatus() {
        return mockStatus("status.open", "Open", false);
    }

    protected IssueStatus getDefaultClosedStatus() {
        return mockStatus("status.resolved", "Resolved", true);
    }

    protected IssueType mockIssueType(String key, String name) {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn(key);
        when(issueType.getName()).thenReturn(name);
        when(issueType.getPrefix()).thenReturn(name + key);
        return issueType;
    }

    protected IssueType getDefaultIssueType() {
        return mockIssueType("datacollection", "Data collection");
    }


    protected IssueReason getDefaultReason() {
        return mockReason("1", "Reason", getDefaultIssueType());
    }

    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName) {
        IssueAssignee assignee = mock(IssueAssignee.class);
        User user = mock(User.class);
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(workGroupId);
        when(workGroup.getName()).thenReturn(workGroupName);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(userName);
        when(assignee.getUser()).thenReturn(user);
        when(assignee.getWorkGroup()).thenReturn(workGroup);
        return assignee;
    }

    protected OpenDeviceAlarm getDefaultOpenDeviceAlarm() {
        return mockOpenDeviceAlarm(1L, getDefaultReason(), getDefaultOpenStatus(), getDefaultAssignee(), getDefaultDevice());
    }

    protected HistoricalDeviceAlarm getDefaultClosedDeviceAlarm() {
        return mockClosedDeviceAlarm(1L, getDefaultReason(), getDefaultClosedStatus(), getDefaultAssignee(), getDefaultDevice());
    }

    protected User getDefaultUser() {
        return mockUser(1, "Admin");
    }

    protected Meter getDefaultDevice() {
        return mockMeter(1, "DefaultDevice");
    }

    protected Meter mockMeter(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getName()).thenReturn(name);
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        doReturn(Optional.empty()).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        Location location = mockLocation("Ohio,Massachusetts,Tennessee,California,Maryland,Florida,Florida,California,California,California,Texas,Texas,Pennsylvania,Washington,Texas,South Dakota,California,Indiana,Louisiana,North Carolina,Washington,California,Hawaii,Oklahoma,Tennessee,Georgia,Florida,West Virginia,Nevada,California,New York,Colorado,Pennsylvania,Ohio,Texas,Texas,Iowa,Florida,Georgia,Texas,Missouri,Pennsylvania,Michigan,Utah,Minnesota,California,Hawaii,Georgia,Tennessee,Nevada,Florida,Georgia,California,Nevada,Indiana,Wisconsin,California,Alabama,Georgia,Colorado,Pennsylvania,Utah,New York,Florida,Texas,Florida,New York,Missouri,Georgia,Indiana,Minnesota,Florida,Ohio,Colorado,District of Columbia,Kentucky,Virginia,Virginia,New York,District of Columbia,Texas,Minnesota,Louisiana,Nevada,Arizona,Nevada,New York,Louisiana,North Carolina,California,Colorado,California,South Carolina,Alabama,Florida,Virginia,Alabama,California,Hawaii");
        when(meter.getLocation()).thenReturn(Optional.of(location));
        MeterActivation meterActivation = mock(MeterActivation.class);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        UsagePoint usagePoint = mockUsagePoint(1, "UP0", 1, ServiceKind.ELECTRICITY);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        return meter;
    }

    protected OpenDeviceAlarm mockOpenDeviceAlarm(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter) {
        OpenDeviceAlarm alarm = mock(OpenDeviceAlarm.class);
        when(alarm.getId()).thenReturn(id);
        when(alarm.getReason()).thenReturn(reason);
        when(alarm.getStatus()).thenReturn(status);
        when(alarm.getDueDate()).thenReturn(null);
        when(alarm.getAssignee()).thenReturn(assingee);
        when(alarm.getDevice()).thenReturn(meter);
        when(alarm.getCreateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getModTime()).thenReturn(Instant.EPOCH);
        when(alarm.getVersion()).thenReturn(1L);
        Priority priority = Priority.DEFAULT;
        when(alarm.getPriority()).thenReturn(priority);
        return alarm;
    }

    protected HistoricalDeviceAlarm mockClosedDeviceAlarm(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter) {
        HistoricalDeviceAlarm alarm = mock(HistoricalDeviceAlarm.class);
        when(alarm.getId()).thenReturn(id);
        when(alarm.getReason()).thenReturn(reason);
        when(alarm.getStatus()).thenReturn(status);
        when(alarm.getDueDate()).thenReturn(null);
        when(alarm.getAssignee()).thenReturn(assingee);
        when(alarm.getDevice()).thenReturn(meter);
        when(alarm.getCreateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getModTime()).thenReturn(Instant.EPOCH);
        when(alarm.getVersion()).thenReturn(1L);
        com.elster.jupiter.issue.share.Priority priority = com.elster.jupiter.issue.share.Priority.DEFAULT;
        when(alarm.getPriority()).thenReturn(priority);
        return alarm;
    }

    protected IssueComment mockComment(long id, String text, User user) {
        IssueComment comment = mock(IssueComment.class);
        when(comment.getId()).thenReturn(id);
        when(comment.getComment()).thenReturn(text);
        when(comment.getCreateTime()).thenReturn(Instant.EPOCH);
        when(comment.getVersion()).thenReturn(1L);
        when(comment.getUser()).thenReturn(user);
        return comment;
    }


    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        return user;
    }


    protected IssueAssignee getDefaultAssignee() {
        return mockAssignee(1L, "Admin", 1L, "WorkGroup");
    }

    private static Location mockLocation(String location) {
        Location mock = mock(Location.class);
        when(mock.toString()).thenReturn(location);
        return mock;
    }

}
