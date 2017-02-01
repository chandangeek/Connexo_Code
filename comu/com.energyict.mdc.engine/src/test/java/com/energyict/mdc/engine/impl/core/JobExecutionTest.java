/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.GenericDeviceProtocol;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.DeviceProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.impl.HexServiceImpl;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TopologyTask;

import com.google.common.base.Strings;
import org.joda.time.DateTime;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionTest {

    public static final String PROTOCOL_DIALECT = "protocolDialect";
    public static final String VERY_LARGE_STRING = Strings.repeat("0123456789", 10000); // String containing 100_000 characters which >> 4K
    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final String MY_PROPERTY = "myProperty";
    private static final String MY_PROPERTY_VALUE = "myPropertyValue";
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private DeviceCommandExecutionToken token;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private OutboundComPort comPort;
    @Mock
    private ComServer comServer;
    @Mock
    private ScheduledConnectionTask connectionTask;
    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private GenericDeviceProtocol genericDeviceProtocol;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private EngineService engineService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private Clock clock;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @Mock
    private ExecutionContext.ServiceProvider serviceProvider;
    @Mock
    private JobExecution.ServiceProvider jobExecutionServiceProvider;
    @Mock
    private CommandRoot.ServiceProvider commandRootServiceProvider;
    private PropertySpec propertySpec;
    private DeviceProtocolDialect protocolDialect;
    private TypedProperties typedProperties;
    @Mock
    private ValueFactory<String> valueFactory;
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    @Mock
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet1;
    @Mock
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet2;

    private GroupedDeviceCommand groupedDeviceCommand1;
    private GroupedDeviceCommand groupedDeviceCommand2;
    private IssueService issueService = new FakeIssueService();
    private CommandCreator commandCreator = new DeviceProtocolCommandCreator();

    public void setupServiceProviders() {
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        when(this.thesaurus.getString(anyString(), anyString())).thenReturn("Translation not supported in unit testing");
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.serviceProvider.issueService()).thenReturn(this.issueService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.serviceProvider.engineService()).thenReturn(this.engineService);
        when(this.serviceProvider.nlsService()).thenReturn(this.nlsService);
        when(this.serviceProvider.clock()).thenReturn(this.clock);

        when(this.jobExecutionServiceProvider.transactionService()).thenReturn(new FakeTransactionService());
        when(this.jobExecutionServiceProvider.clock()).thenReturn(this.clock);
        when(this.jobExecutionServiceProvider.nlsService()).thenReturn(this.nlsService);
        when(this.jobExecutionServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.jobExecutionServiceProvider.hexService()).thenReturn(new HexServiceImpl());
        when(this.jobExecutionServiceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.jobExecutionServiceProvider.issueService()).thenReturn(this.issueService);
        when(this.jobExecutionServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.jobExecutionServiceProvider.deviceConfigurationService()).thenReturn(this.deviceConfigurationService);
        when(this.jobExecutionServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.jobExecutionServiceProvider.engineService()).thenReturn(this.engineService);
        when(this.jobExecutionServiceProvider.mdcReadingTypeUtilService()).thenReturn(this.mdcReadingTypeUtilService);

        IdentificationService identificationService = mock(IdentificationService.class);
        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(any(Device.class))).thenReturn(mock(DeviceIdentifier.class));
        when(this.jobExecutionServiceProvider.identificationService()).thenReturn(identificationService);

        when(this.commandRootServiceProvider.transactionService()).thenReturn(new FakeTransactionService());
        when(this.commandRootServiceProvider.clock()).thenReturn(this.clock);
        when(this.commandRootServiceProvider.issueService()).thenReturn(this.issueService);
        when(this.commandRootServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.commandRootServiceProvider.mdcReadingTypeUtilService()).thenReturn(this.mdcReadingTypeUtilService);
    }

    @Before
    public void setupEventPublisher() {
        when(clock.instant()).thenReturn(Instant.now());
        this.setupServiceProviders();
    }

    @Before
    public void initMocks() throws ConnectionException {
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(device.getProtocolDialectProperties(anyString())).thenReturn(Optional.<ProtocolDialectProperties>empty());
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(this.deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.of(this.device));
        ConnectionTask ct = connectionTask;
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.of(ct));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(this.comTask);
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.connect(eq(comPort), anyList())).thenReturn(new VoidTestComChannel());
        doNothing().when(comServerDAO).executionCompleted(comTaskExecution);
        when(comPort.getComServer()).thenReturn(this.comServer);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);

        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        when(comSessionBuilder.addComTaskExecutionSession(eq(this.comTaskExecution), any(ComTask.class), any(Instant.class))).
                thenReturn(mock(ComTaskExecutionSessionBuilder.class));
        when(this.connectionTaskService.buildComSession(eq(this.connectionTask), eq(this.comPortPool), eq(this.comPort), any(Instant.class))).
                thenReturn(comSessionBuilder);
        when(this.deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.comTaskEnablement.getSecurityPropertySet()).thenReturn(this.securityPropertySet);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(1);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(1);
        when(this.securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(this.securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);

        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.empty());

        ExecutionContext executionContext = newTestExecutionContext();

        CommandRootImpl commandRoot1 = new CommandRootImpl(executionContext, commandRootServiceProvider);
        CommandRootImpl commandRoot2 = new CommandRootImpl(executionContext, commandRootServiceProvider);
        groupedDeviceCommand1 = spy(new GroupedDeviceCommand(commandRoot1, offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet1));
        groupedDeviceCommand2 = spy(new GroupedDeviceCommand(commandRoot2, offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet2));
    }

    @Test
    public void testGenericDeviceProtocol() throws ConnectionException {
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        ScheduledComTaskExecutionGroup jobExecution = spy(new ScheduledComTaskExecutionGroup(outboundComPort, comServerDAO, this.deviceCommandExecutor, connectionTask, jobExecutionServiceProvider));
        ExecutionContext executionContext = newTestExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        createMockedComTaskWithGivenProtocolTasks();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceProtocolPluggableClass severServerDeviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(severServerDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(genericDeviceProtocol);
        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(severServerDeviceProtocolPluggableClass);
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        jobExecution.prepareAll(Arrays.asList(comTaskExecution));
        verify(genericDeviceProtocol, times(1)).organizeComCommands(any(CommandRoot.class));
    }

    @Test
    public void testNormalDeviceProtocol() throws ConnectionException {
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        ScheduledComTaskExecutionGroup jobExecution = spy(new ScheduledComTaskExecutionGroup(outboundComPort, comServerDAO, this.deviceCommandExecutor, connectionTask, jobExecutionServiceProvider));
        ExecutionContext executionContext = newTestExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        createMockedComTaskWithGivenProtocolTasks();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceProtocolPluggableClass severServerDeviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(severServerDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(severServerDeviceProtocolPluggableClass);
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));

        jobExecution.prepareAll(Arrays.asList(comTaskExecution));
        verify(genericDeviceProtocol, never()).organizeComCommands(any(CommandRoot.class));
    }

    @Test
    public void basicCheckIsInFrontWhenAlreadyInFrontTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask, topologyTask);

        // business method
        jobExecution.prepareComTaskExecution(comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet, groupedDeviceCommand1, commandCreator);

        // asserts
        final Map<ComCommandType, ComCommand> commands = groupedDeviceCommand1.getComTaskRoot(comTaskExecution).getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.BASIC_CHECK_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);
    }

    @Test
    public void basicCheckInFrontWhenLastTaskTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        ProtocolTask loadProfilesTask = mock(LoadProfilesTask.class);
        ProtocolTask logBooksTask = mock(LogBooksTask.class);
        createMockedComTaskWithGivenProtocolTasks(loadProfilesTask, logBooksTask, topologyTask, basicCheckTask);

        // business method
        jobExecution.prepareComTaskExecution(comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet, groupedDeviceCommand1, commandCreator);

        // asserts
        final Map<ComCommandType, ComCommand> commands = groupedDeviceCommand1.getComTaskRoot(comTaskExecution).getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.BASIC_CHECK_COMMAND, ComCommandTypes.LOAD_PROFILE_COMMAND, ComCommandTypes.LOGBOOKS_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);
    }

    @Test
    public void basicCheckInFrontWhenOnlyBasicCheckExistsTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);

        // business method
        jobExecution.prepareComTaskExecution(comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet, groupedDeviceCommand1, commandCreator);

        // asserts
        final Map<ComCommandType, ComCommand> commands = groupedDeviceCommand1.getComTaskRoot(comTaskExecution).getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.BASIC_CHECK_COMMAND);

    }

    @Test
    public void basicCheckInFrontWhenNoBasicCheckExistsTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        ProtocolTask loadProfilesTask = mock(LoadProfilesTask.class);
        ProtocolTask logBooksTask = mock(LogBooksTask.class);
        createMockedComTaskWithGivenProtocolTasks(loadProfilesTask, logBooksTask, topologyTask);

        // business method
        jobExecution.prepareComTaskExecution(comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet, groupedDeviceCommand1, commandCreator);

        // asserts
        final Map<ComCommandType, ComCommand> commands = groupedDeviceCommand1.getComTaskRoot(comTaskExecution).getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.LOAD_PROFILE_COMMAND, ComCommandTypes.LOGBOOKS_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);

    }

    @Test
    public void timeDifferenceExceedsMaxShouldFailCompleteSessionTest() throws ConnectionException {
        Date meterTime = new DateTime(2013, 9, 18, 16, 0, 0, 0).toDate();
        Date systemTime = new DateTime(2013, 9, 18, 15, 0, 0, 0).toDate();
        when(this.clock.instant()).thenReturn(systemTime.toInstant());
        when(this.clock.millis()).thenReturn(systemTime.getTime());
        when(deviceProtocol.getTime()).thenReturn(meterTime);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup comTaskExecutionGroup = getJobExecutionForBasicCheckInFrontTests();
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class, withSettings().extraInterfaces(OfflineDeviceContext.class));
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS)));
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);

        // business method
        comTaskExecutionGroup.createExecutionContext();
        comTaskExecutionGroup.prepareComTaskExecution(comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet, groupedDeviceCommand1, commandCreator);

        comTaskExecutionGroup.establishConnectionFor(comPort);
        groupedDeviceCommand1.execute(comTaskExecutionGroup.getExecutionContext());

        assertThat(comTaskExecutionGroup.getExecutionContext().hasBasicCheckFailed()).isTrue();
    }

    @Test
    public void serialNumberMisMatchShouldFailCompleteSessionTest() {
        String meterSerial = "ThisIsTheMeterSerialNumber";
        String configuredSerial = "ThisIsTheConfiguredSerialNumber";
        when(deviceProtocol.getSerialNumber()).thenReturn(meterSerial);
        when(this.offlineDevice.getSerialNumber()).thenReturn(configuredSerial);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup comTaskExecutionGroup = getJobExecutionForBasicCheckInFrontTests();

        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);

        // business method
        comTaskExecutionGroup.createExecutionContext();
        comTaskExecutionGroup.prepareComTaskExecution(comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet, groupedDeviceCommand1, commandCreator);

        comTaskExecutionGroup.establishConnectionFor(comPort);
        groupedDeviceCommand1.execute(comTaskExecutionGroup.getExecutionContext());

        assertThat(comTaskExecutionGroup.getExecutionContext().hasBasicCheckFailed()).isTrue();
    }

    @Test
    public void testGetProtocolDialectProperties() {
        prepareMocksForProtocolDialectProperties();
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        // make sur no protocoldialect properties set on device
        when(device.getProtocolDialectProperties(anyString())).thenReturn(Optional.<ProtocolDialectProperties>empty());

        TypedProperties typedProperties = JobExecution.getProtocolDialectTypedProperties(comTaskExecution);
        assertThat(typedProperties.getProperty(MY_PROPERTY)).isEqualTo(MY_PROPERTY_VALUE);
    }

    private void createMockedComTaskWithGivenProtocolTasks(ProtocolTask... protocolTasks) {
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
    }

    private ScheduledComTaskExecutionGroup getJobExecutionForBasicCheckInFrontTests() {
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ScheduledComTaskExecutionGroup comTaskExecutionGroup = new ScheduledComTaskExecutionGroup((OutboundComPort) comPort, comServerDAO, this.deviceCommandExecutor, connectionTask, jobExecutionServiceProvider);
        comTaskExecutionGroup.add(comTaskExecution);
        comTaskExecutionGroup.setExecutionContext(new ExecutionContext(comTaskExecutionGroup, connectionTask, comPort, true, jobExecutionServiceProvider));
        return comTaskExecutionGroup;
    }

    private ExecutionContext newTestExecutionContext() {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    private ExecutionContext newTestExecutionContext(Logger logger) {
        return this.newTestExecutionContext(logger, mock(JobExecution.class));
    }

    private ExecutionContext newTestExecutionContext(Logger logger, JobExecution jobExecution) {
        ExecutionContext executionContext =
                new ExecutionContext(
                        jobExecution,
                        this.connectionTask,
                        this.comPort,
                        false,
                        this.serviceProvider);
        executionContext.setLogger(logger);
        return executionContext;
    }

    private void prepareMocksForProtocolDialectProperties() {
        typedProperties = TypedProperties.empty();
        typedProperties.setProperty(MY_PROPERTY, MY_PROPERTY_VALUE);

        protocolDialect = mock(DeviceProtocolDialect.class);
        propertySpec = mock(PropertySpec.class);

        when(protocolDialect.getDisplayName()).thenReturn(PROTOCOL_DIALECT);
        when(protocolDialect.getDeviceProtocolDialectName()).thenReturn(PROTOCOL_DIALECT);
        when(protocolDialect.getPropertySpec(MY_PROPERTY)).thenReturn(Optional.of(propertySpec));

        when(propertySpec.getValueFactory()).thenReturn(valueFactory);

        when(valueFactory.fromStringValue(MY_PROPERTY_VALUE)).thenReturn(MY_PROPERTY_VALUE);
        when(valueFactory.toStringValue(MY_PROPERTY_VALUE)).thenReturn(MY_PROPERTY_VALUE);
        when(valueFactory.fromStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
        when(valueFactory.toStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);

        protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(protocolDialect);
        when(protocolDialectConfigurationProperties.getTypedProperties()).thenReturn(typedProperties);

        when(deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(protocolDialect)).thenReturn(protocolDialectConfigurationProperties);
    }
}