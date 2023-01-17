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
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.DefaultState;
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
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.pki.KeyPurpose;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceMessageEnablement;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ClockTask;
import com.energyict.mdc.common.tasks.ClockTaskType;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarmClearStatus;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.impl.MdcPropertyUtilsImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXORequestSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOResponseSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOSecuritySuiteAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLRequestSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLResponseSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLSecuritySuiteLevelAdapter;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.UPLConnectionFunction;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;

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
import java.util.function.Function;
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
import static org.mockito.Mockito.withSettings;

public class MultisensePublicApiJerseyTest extends FelixRestApplicationJerseyTest {
    private static final String MANUFACTURER = "The Manufacturer";
    private static final String MODELNBR = "The ModelNumber";
    private static final String MODELVERSION = "The modelVersion";
    private static final Pattern MRID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final String MRID_REPLACEMENT = "$1-$2-$3-$4-$5";
    private static final int UNUSED_SECURITY_ACCESS_LEVEL = -1;
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
    DeviceAlarmService deviceAlarmService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    SecurityAccessorInfoFactory securityAccessorInfoFactory;
    @Mock
    HsmEnergyService hsmEnergyService;

    static ProtocolPluggableService protocolPluggableService;
    static PropertyValueInfoService propertyValueInfoService;

    @BeforeClass
    public static void before() {
        protocolPluggableService = mock(ProtocolPluggableService.class);
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLAuthenticationLevelAdapter.adaptTo((com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLEncryptionLevelAdapter.adaptTo((com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.SecuritySuite.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLSecuritySuiteLevelAdapter.adaptTo((com.energyict.mdc.upl.security.SecuritySuite) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.RequestSecurityLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLRequestSecurityLevelAdapter.adaptTo((com.energyict.mdc.upl.security.RequestSecurityLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.ResponseSecurityLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLResponseSecurityLevelAdapter.adaptTo((com.energyict.mdc.upl.security.ResponseSecurityLevel) args[0], null);
        });
    }

    @BeforeClass
    public static void initializePropertyValueInfoServiceMock() throws Exception {
        propertyValueInfoService = mock(PropertyValueInfoService.class);
        when(propertyValueInfoService.getPropertyInfo(any(PropertySpec.class), any(Function.class))).thenAnswer(invocation -> {
            String propertyName = invocation.getArguments()[0] != null ? ((PropertySpec) invocation.getArguments()[0]).getName() : "property";
            Object value = invocation.getArguments()[1] != null ? ((Function) invocation.getArguments()[1]).apply(propertyName) : "";
            if (value instanceof BigDecimal) {
                Integer propertyValue = invocation.getArguments()[1] != null ? ((BigDecimal) value).intValue() : null;
                return new PropertyInfo("Property", "Property", new PropertyValueInfo<>(propertyValue, null), new PropertyTypeInfo(), false);
            } else {
                String propertyValue = invocation.getArguments()[1] != null ? value.toString() : null;
                return new PropertyInfo("Property", "Property", new PropertyValueInfo<>(propertyValue, null), new PropertyTypeInfo(), false);
            }
        });
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
        application.setMdcPropertyUtils(new MdcPropertyUtilsImpl(propertyValueInfoService, meteringGroupsService));
        application.setSecurityManagementService(securityManagementService);
        application.setSecurityAccessorInfoFactory(securityAccessorInfoFactory);
        application.setHsmEnergyService(hsmEnergyService);
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
        DeviceType deviceType = deviceConfiguration.getDeviceType() != null
                ? deviceConfiguration.getDeviceType()
                : mockDeviceType(99L, "Device type for " + serial, 1L);
        when(mock.getDeviceType()).thenReturn(deviceType);
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

    PropertySpec mockStringPropertySpec(String key, String value) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn(key);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(value);
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

    PartialScheduledConnectionTask mockPartialScheduledConnectionTask(long id, String name, long version, UPLConnectionFunction connectionFunction, PropertySpec... propertySpecs) {
        PartialScheduledConnectionTask mock = mock(PartialScheduledConnectionTask.class);
        when(mock.getConnectionFunction()).thenReturn(Optional.ofNullable(adaptToConnexoConnectionFunction(connectionFunction)));
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
        when(mock.getConnectionFunction()).thenReturn(Optional.empty());
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
        return mockPartialOutboundConnectionTask(id, name, deviceConfig, version, dialectProperties, false, null);
    }

    PartialScheduledConnectionTask mockPartialOutboundConnectionTask(long id, String name, DeviceConfiguration deviceConfig, long version, ProtocolDialectConfigurationProperties dialectProperties, boolean isDefault, UPLConnectionFunction connectionFunction) {
        PartialScheduledConnectionTask mock = mock(PartialScheduledConnectionTask.class);
        when(mock.getName()).thenReturn(name);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(mock.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(mock.getId()).thenReturn(id);
        when(mock.getConfiguration()).thenReturn(deviceConfig);
        when(mock.isDefault()).thenReturn(isDefault);
        when(mock.getConnectionFunction()).thenReturn(Optional.ofNullable(adaptToConnexoConnectionFunction(connectionFunction)));
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

    protected ConnectionFunction adaptToConnexoConnectionFunction(UPLConnectionFunction connectionFunction) {
        if (connectionFunction != null) {
            return new ConnectionFunction() {
                @Override
                public String getConnectionFunctionDisplayName() {
                    return connectionFunction.getConnectionFunctionName();
                }

                @Override
                public String getConnectionFunctionName() {
                    return connectionFunction.getConnectionFunctionName();
                }

                @Override
                public long getId() {
                    return connectionFunction.getId();
                }
            };
        }
        return null;
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
        return mockPluggableClass(id, name, version, authAccessLvls, encAccessLvls, Collections.<SecuritySuite>emptyList(), Collections.<RequestSecurityLevel>emptyList(), Collections.<ResponseSecurityLevel>emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    DeviceProtocolPluggableClass mockPluggableClass(long id, String name, String version,
                                                    List<AuthenticationDeviceAccessLevel> authAccessLvls,
                                                    List<EncryptionDeviceAccessLevel> encAccessLvls,
                                                    List<SecuritySuite> securitySuites,
                                                    List<RequestSecurityLevel> requestSecurityLvls,
                                                    List<ResponseSecurityLevel> responseSecurityLevels) {
        return mockPluggableClass(id, name, version, authAccessLvls, encAccessLvls, securitySuites, requestSecurityLvls, responseSecurityLevels, Collections.emptyList(), Collections.emptyList());
    }

    DeviceProtocolPluggableClass mockPluggableClass(long id, String name, String version,
                                                    List<AuthenticationDeviceAccessLevel> authAccessLvls,
                                                    List<EncryptionDeviceAccessLevel> encAccessLvls,
                                                    List<SecuritySuite> securitySuites,
                                                    List<RequestSecurityLevel> requestSecurityLvls,
                                                    List<ResponseSecurityLevel> responseSecurityLevels,
                                                    List<UPLConnectionFunction> providedConnectionFunctions,
                                                    List<UPLConnectionFunction> consumableConnectionFunctions) {
        DeviceProtocolPluggableClass mock = mock(DeviceProtocolPluggableClass.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getJavaClassName()).thenReturn("com.energyict.prot." + name + ".class");
        when(mock.getVersion()).thenReturn(version);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(id)).thenReturn(Optional.of(mock));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> adaptedAuthLevels = authAccessLvls.stream().map(CXOAuthenticationLevelAdapter::adaptTo).collect(Collectors.toList());
        List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> adaptedEncrLevels = encAccessLvls.stream().map(CXOEncryptionLevelAdapter::adaptTo).collect(Collectors.toList());
        List<com.energyict.mdc.upl.security.SecuritySuite> adaptedSecuritySuites = securitySuites.stream().map(CXOSecuritySuiteAdapter::adaptTo).collect(Collectors.toList());
        List<com.energyict.mdc.upl.security.RequestSecurityLevel> adaptedRequestSecurityLevels = requestSecurityLvls.stream().map(CXORequestSecurityLevelAdapter::adaptTo).collect(Collectors.toList());
        List<com.energyict.mdc.upl.security.ResponseSecurityLevel> adaptedResponseSecurityLevels = responseSecurityLevels.stream().map(CXOResponseSecurityLevelAdapter::adaptTo).collect(Collectors.toList());

        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(adaptedAuthLevels);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(adaptedEncrLevels);
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(adaptedSecuritySuites);
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getRequestSecurityLevels()).thenReturn(adaptedRequestSecurityLevels);
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getResponseSecurityLevels()).thenReturn(adaptedResponseSecurityLevels);

        when(deviceProtocol.getProvidedConnectionFunctions()).thenReturn(providedConnectionFunctions);
        when(mock.getProvidedConnectionFunctions()).thenReturn(providedConnectionFunctions.stream().map(this::adaptToConnexoConnectionFunction).collect(Collectors.toList()));
        when(deviceProtocol.getConsumableConnectionFunctions()).thenReturn(consumableConnectionFunctions);
        when(mock.getConsumableConnectionFunctions()).thenReturn(consumableConnectionFunctions.stream().map(this::adaptToConnexoConnectionFunction).collect(Collectors.toList()));
        when(mock.getDeviceProtocol()).thenReturn(deviceProtocol);
        return mock;
    }

    SecuritySuite mockSecuritySuite(int id) {
        SecuritySuite mock = mock(SecuritySuite.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTranslation()).thenReturn("Proper name for " + id);
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

    RequestSecurityLevel mockRequestSecurityDeviceAccessLevel(int id) {
        RequestSecurityLevel mock = mock(RequestSecurityLevel.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTranslation()).thenReturn("Proper name for " + id);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(mock.getSecurityProperties()).thenReturn(Collections.singletonList(propertySpec));
        return mock;
    }

    ResponseSecurityLevel mockResponseSecurityDeviceAccessLevel(int id) {
        ResponseSecurityLevel mock = mock(ResponseSecurityLevel.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTranslation()).thenReturn("Proper name for " + id);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(mock.getSecurityProperties()).thenReturn(Collections.singletonList(propertySpec));
        return mock;
    }

    UPLConnectionFunction mockUPLConnectionFunction(long id, String name) {
        UPLConnectionFunction connectionFunction = mock(UPLConnectionFunction.class);
        when(connectionFunction.getId()).thenReturn(id);
        when(connectionFunction.getConnectionFunctionName()).thenReturn(name);
        return connectionFunction;
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

    protected SecurityPropertySet mockSecurityPropertySet(long id, DeviceConfiguration deviceConfiguration, String name, int client, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel, AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, String configurationSecurityPropertyName, long keyAccessorTypeId, long version) {
        SecuritySuite securitySuite = mock(SecuritySuite.class);
        RequestSecurityLevel requestSecurityLevel = mock(RequestSecurityLevel.class);
        ResponseSecurityLevel responseSecurityLevel = mock(ResponseSecurityLevel.class);
        when(securitySuite.getId()).thenReturn(UNUSED_SECURITY_ACCESS_LEVEL);
        when(requestSecurityLevel.getId()).thenReturn(UNUSED_SECURITY_ACCESS_LEVEL);
        when(responseSecurityLevel.getId()).thenReturn(UNUSED_SECURITY_ACCESS_LEVEL);
        return mockSecurityPropertySet(id, deviceConfiguration, name, client, securitySuite, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, requestSecurityLevel, responseSecurityLevel, configurationSecurityPropertyName, keyAccessorTypeId, version);
    }

    protected SecurityPropertySet mockSecurityPropertySet(long id, DeviceConfiguration deviceConfiguration, String name, int client,
                                                          SecuritySuite securitySuite,
                                                          EncryptionDeviceAccessLevel encryptionDeviceAccessLevel,
                                                          AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel,
                                                          RequestSecurityLevel requestSecurityLevel,
                                                          ResponseSecurityLevel responseSecurityLevel,
                                                          String configurationSecurityPropertyName, long keyAccessorTypeId, long version) {
        SecurityPropertySet mock = mock(SecurityPropertySet.class);
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        PropertySpec bigDecimalPropertySpec = mockBigDecimalPropertySpec();
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec));
        when(mock.getSecuritySuite()).thenReturn(securitySuite);
        when(mock.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(mock.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(mock.getRequestSecurityLevel()).thenReturn(requestSecurityLevel);
        when(mock.getResponseSecurityLevel()).thenReturn(responseSecurityLevel);
        when(mock.getVersion()).thenReturn(version);
        when(mock.getClient()).thenReturn(BigDecimal.valueOf(client));
        when(mock.getClientSecurityPropertySpec()).thenReturn(Optional.of(bigDecimalPropertySpec));
        ConfigurationSecurityProperty configurationSecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(configurationSecurityProperty.getName()).thenReturn(configurationSecurityPropertyName);
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(configurationSecurityPropertyName);
        when(securityAccessorType.getId()).thenReturn(keyAccessorTypeId);
        when(securityAccessorType.getKeyPurpose()).thenReturn(new KeyPurpose() {
            @Override
            public String getId() {
                return "KEY";
            }

            @Override
            public String getName() {
                return "name";
            }
        });
        when(configurationSecurityProperty.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(mock.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(configurationSecurityProperty));

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
        DeviceAlarmClearStatus clearedStatus = new DeviceAlarmClearStatus();
        clearedStatus.init();
        when(alarm.getId()).thenReturn(id);
        when(alarm.getReason()).thenReturn(reason);
        when(alarm.getStatus()).thenReturn(status);
        when(alarm.getClearStatus()).thenReturn(clearedStatus);
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

    protected SecurityAccessor mockSecurityAccessor(String type, PropertySpec... propertySpecs) {
        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        SecurityAccessorType securityAccessorType = mockSecuritySecurityAccessorType(type);
        when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(securityAccessor.getPropertySpecs()).thenReturn(Arrays.asList(propertySpecs));
        return securityAccessor;
    }

    protected SecurityAccessorType mockSecuritySecurityAccessorType(String type) {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(type);
        return securityAccessorType;
    }

    private static Location mockLocation(String location) {
        Location mock = mock(Location.class);
        when(mock.toString()).thenReturn(location);
        return mock;
    }
}
