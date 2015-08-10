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
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TaskService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.*;
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

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return new MessageSeed[0];
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
        application.setDeviceLifeCycleService(deviceLifeCycleService);
        application.setFiniteStateMachineService(finiteStateMachineService);
        application.setConnectionTaskService(connectionTaskService);
        application.setEngineConfigurationService(engineConfigurationService);
        application.setClock(clock);
        application.setTaskService(taskService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setProtocolPluggableService(protocolPluggableService);
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
        return readingType;
    }

    Device mockDevice(String mrid, String serial, DeviceConfiguration deviceConfiguration) {
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
        when(batchService.findBatch(mock)).thenReturn(Optional.of(batch));
        when(topologyService.getPhysicalGateway(mock)).thenReturn(Optional.empty());
        when(this.deviceService.findByUniqueMrid(mrid)).thenReturn(Optional.of(mock));
        when(this.deviceService.findAndLockDeviceByIdAndVersion(deviceId, 333L)).thenReturn(Optional.of(mock));
        return mock;
    }

    DeviceType mockDeviceType(long id, String name) {
        DeviceType mock = mock(DeviceType.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1000 + id, "Default");
        when(mock.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getId()).thenReturn(id * id);
        when(mock.getDeviceProtocolPluggableClass()).thenReturn(pluggableClass);
        when(deviceConfigurationService.findDeviceType(id)).thenReturn(Optional.of(mock));
        return mock;
    }

    DeviceConfiguration mockDeviceConfiguration(long id, String name) {
        DeviceConfiguration mock = mock(DeviceConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);

        return mock;
    }

    DeviceConfiguration mockDeviceConfiguration(long id, String name, DeviceType deviceType) {
        DeviceConfiguration mock = mockDeviceConfiguration(id, name);
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

    ScheduledConnectionTask mockScheduledConnectionTask(long id, String name, Device deviceXas, OutboundComPortPool comPortPool, PartialScheduledConnectionTask partial) {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(id);
        when(connectionTask.getName()).thenReturn(name);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(connectionTask.getDevice()).thenReturn(deviceXas);
        when(connectionTask.isSimultaneousConnectionsAllowed()).thenReturn(true);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partial);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(connectionTask.getRescheduleDelay()).thenReturn(TimeDuration.minutes(60));
        when(connectionTask.getCommunicationWindow()).thenReturn(new ComWindow(PartialTime.fromHours(2), PartialTime.fromHours(4)));
        when(connectionTaskService.findConnectionTask(id)).thenReturn(Optional.of(connectionTask));
        return connectionTask;
    }

    InboundConnectionTask mockInboundConnectionTask(long id, String name, Device deviceXas, InboundComPortPool comPortPool, PartialInboundConnectionTask partial) {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(id);
        when(connectionTask.getName()).thenReturn(name);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(connectionTask.getDevice()).thenReturn(deviceXas);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partial);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(connectionTaskService.findConnectionTask(id)).thenReturn(Optional.of(connectionTask));
        return connectionTask;
    }

    PartialScheduledConnectionTask mockPartialScheduledConnectionTask(long id, String name, PropertySpec... propertySpecs) {
        PartialScheduledConnectionTask partial = mock(PartialScheduledConnectionTask.class);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(partial.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(partial.getName()).thenReturn(name);
        when(partial.getId()).thenReturn(id);
        when(connectionTaskPluggeableClass.getName()).thenReturn("outbound pluggeable class");
        when(connectionTaskPluggeableClass.getPropertySpecs()).thenReturn(Arrays.asList(propertySpecs));
        return partial;
    }

    PartialInboundConnectionTask mockPartialInboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig) {
        PartialInboundConnectionTask mock = mock(PartialInboundConnectionTask.class);
        when(mock.getName()).thenReturn(name);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(mock.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(mock.getId()).thenReturn(id);
        when(mock.getConfiguration()).thenReturn(deviceConfig);
        when(connectionTaskPluggeableClass.getName()).thenReturn("inbound pluggeable class");
        InboundComPortPool comPortPool = mockInboundComPortPool(65L);
        when(mock.getComPortPool()).thenReturn(comPortPool);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(mock.getConnectionType()).thenReturn(connectionType);
        when(mock.getTypedProperties()).thenReturn(TypedProperties.empty());

        return mock;
    }

    PartialScheduledConnectionTask mockPartialOutboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig) {
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
        when(mock.isSimultaneousConnectionsAllowed()).thenReturn(true);
        when(mock.getRescheduleDelay()).thenReturn(TimeDuration.minutes(60));
        when(mock.getCommunicationWindow()).thenReturn(new ComWindow(PartialTime.fromHours(2), PartialTime.fromHours(4)));

        return mock;
    }

    private InboundComPortPool mockInboundComPortPool(long id) {
        InboundComPortPool mock = mock(InboundComPortPool.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    ComTask mockComTask(long id, String name, ProtocolTask... protocolTasks) {
        ComTask mock = mock(ComTask.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
        when(taskService.findComTask(id)).thenReturn(Optional.of(mock));
        return mock;
    }

    DeviceMessageCategory mockDeviceMessageCategory(int id, String name) {
        DeviceMessageCategory mock = mock(DeviceMessageCategory.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getDescription()).thenReturn("Description of " + name);
        when(mock.getMessageSpecifications()).thenReturn(Collections.emptyList());
        when(deviceMessageSpecificationService.findCategoryById(id)).thenReturn(Optional.of(mock));
        return mock;
    }

    ClockTask mockClockTask(long id) {
        ClockTask protocolTask = mock(ClockTask.class);
        when(protocolTask.getId()).thenReturn(id);
        when(protocolTask.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);
        when(taskService.findProtocolTask(id)).thenReturn(Optional.of(protocolTask));
        return protocolTask;
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
        when(mock.getTranslationKey()).thenReturn("aal" + id);
        when(thesaurus.getStringBeyondComponent("aal" + id, "aal" + id)).thenReturn("Proper name for " + id);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(mock.getSecurityProperties()).thenReturn(Collections.singletonList(propertySpec));
        return mock;
    }

    EncryptionDeviceAccessLevel mockEncryptionAccessLevel(int id) {
        EncryptionDeviceAccessLevel mock = mock(EncryptionDeviceAccessLevel.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTranslationKey()).thenReturn("eal" + id);
        when(thesaurus.getStringBeyondComponent("eal" + id, "eal" + id)).thenReturn("Proper name for " + id);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(mock.getSecurityProperties()).thenReturn(Collections.singletonList(propertySpec));
        return mock;
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


}