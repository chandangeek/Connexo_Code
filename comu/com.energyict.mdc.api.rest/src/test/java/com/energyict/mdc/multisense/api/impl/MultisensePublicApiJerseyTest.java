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
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
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
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
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
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import org.mockito.Mock;

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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/19/14.
 */
public class MultisensePublicApiJerseyTest extends FelixRestApplicationJerseyTest {
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
    @Mock
    ProtocolPluggableService protocolPluggableService;
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

    Device mockDevice(String mrid, String serial, DeviceConfiguration deviceConfiguration, long version) {
        Device mock = mock(Device.class);
        when(mock.getmRID()).thenReturn(mrid);
        when(mock.getName()).thenReturn(mrid);
        long deviceId = (long) mrid.hashCode();
        when(mock.getId()).thenReturn(deviceId);
        when(mock.getSerialNumber()).thenReturn(serial);
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
        when(deviceService.findByUniqueMrid(mrid)).thenReturn(Optional.of(mock));
        when(deviceService.findAndLockDeviceByIdAndVersion(deviceId, version)).thenReturn(Optional.of(mock));
        when(deviceService.findAndLockDeviceByIdAndVersion(eq(deviceId), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceService.findAndLockDeviceBymRIDAndVersion(eq(mrid), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(deviceService.findAndLockDeviceBymRIDAndVersion(eq(mrid), eq(version))).thenReturn(Optional.of(mock));
        when(mock.getVersion()).thenReturn(version);
        return mock;
    }

    DeviceType mockDeviceType(long id, String name, long version) {
        DeviceType mock = mock(DeviceType.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1000 + id, "Default", mock, 3333L);
        when(mock.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getId()).thenReturn(id * id);
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

    PartialInboundConnectionTask mockPartialInboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig, long version) {
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

        return mock;
    }

    PartialScheduledConnectionTask mockPartialOutboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig, long version) {
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
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(authAccessLvls);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(encAccessLvls);
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
        when(comSchedule.getmRID()).thenReturn(Optional.<String>empty());
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

    protected ScheduledComTaskExecution mockScheduledComTaskExecution(long id, ComSchedule comSchedule, Device device, long version) {
        ScheduledComTaskExecution scheduledComTaskExecution = mock(ScheduledComTaskExecution.class);
        when(scheduledComTaskExecution.getComSchedule()).thenReturn(comSchedule);
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
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getInstallationTime()).thenReturn(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC));
        when(usagePoint.getServiceDeliveryRemark()).thenReturn("remark");
        when(usagePoint.getServicePriority()).thenReturn("service priority");
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        when(usagePoint.forCustomProperties()).thenReturn(extension);
        when(meteringService.findUsagePoint(id)).thenReturn(Optional.of(usagePoint));
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
}