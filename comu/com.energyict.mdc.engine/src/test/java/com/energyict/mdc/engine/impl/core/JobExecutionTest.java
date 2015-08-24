package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.GenericDeviceProtocol;
import com.energyict.mdc.engine.config.ComPort;
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
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.impl.HexServiceImpl;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TopologyTask;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import org.joda.time.DateTime;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
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

/**
 * Test to check if the organizeComCommands() method on the interface GenericDeviceProtocol is triggered correctly.
 * <p>
 * Copyrights EnergyICT
 * Date: 30/01/13
 * Time: 9:11
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class JobExecutionTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private DeviceCommandExecutionToken token;
    @Mock
    private ComTask comTask;
    @Mock
    private ManuallyScheduledComTaskExecution comTaskExecution;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private ComPort comPort;
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

    private IssueService issueService = new FakeIssueService();
    private CommandRootImpl root;

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
        when(device.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(device.getProtocolDialectProperties(anyString())).thenReturn(Optional.<ProtocolDialectProperties>empty());
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(this.deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.of(this.device));
        ConnectionTask ct = connectionTask;
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.of(ct));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(this.comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask));
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.connect(eq(comPort), anyList())).thenReturn(new VoidTestComChannel());
        doNothing().when(comServerDAO).executionCompleted(comTaskExecution);
        when(comPort.getComServer()).thenReturn(this.comServer);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);

        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        when(comSessionBuilder.addComTaskExecutionSession(eq(this.comTaskExecution), any(ComTask.class), eq(this.device), any(Instant.class))).
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
        root = spy(new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider));
        CommandRootImpl root2 = spy(new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider));
        doNothing().when(root).execute(any(DeviceProtocol.class), any(ExecutionContext.class));
        doNothing().when(root2).execute(any(DeviceProtocol.class), any(ExecutionContext.class));
        when(genericDeviceProtocol.organizeComCommands(root)).thenReturn(root2);
    }

    @Test
    public void testGenericDeviceProtocol() throws ConnectionException {
        JobExecution.PreparedComTaskExecution preparedComTaskExecution = mock(JobExecution.PreparedComTaskExecution.class);
        when(preparedComTaskExecution.getComTaskExecution()).thenReturn(comTaskExecution);
        when(preparedComTaskExecution.getCommandRoot()).thenReturn(root);
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        ScheduledComTaskExecutionGroup jobExecution = spy(new MockScheduledComTaskExecutionGroup(outboundComPort, comServerDAO, this.deviceCommandExecutor, this.jobExecutionServiceProvider, connectionTask));
        ExecutionContext executionContext = newTestExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(preparedComTaskExecution.getDeviceProtocol()).thenReturn(genericDeviceProtocol);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(genericDeviceProtocol);
        createMockedComTaskWithGivenProtocolTasks();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceProtocolPluggableClass serverDeviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(serverDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(serverDeviceProtocolPluggableClass);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(serverDeviceProtocolPluggableClass);
        when(serverDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(genericDeviceProtocol);
        jobExecution.prepareAll(Arrays.asList(comTaskExecution));
        verify(genericDeviceProtocol, times(1)).organizeComCommands(any(CommandRoot.class));
    }

    @Test
    public void testNormalDeviceProtocol() throws ConnectionException {
        JobExecution.PreparedComTaskExecution preparedComTaskExecution = mock(JobExecution.PreparedComTaskExecution.class);
        when(preparedComTaskExecution.getComTaskExecution()).thenReturn(comTaskExecution);
        when(preparedComTaskExecution.getCommandRoot()).thenReturn(root);
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        ScheduledComTaskExecutionGroup jobExecution = spy(new MockScheduledComTaskExecutionGroup(outboundComPort, comServerDAO, this.deviceCommandExecutor, this.jobExecutionServiceProvider, connectionTask));
        ExecutionContext executionContext = newTestExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(preparedComTaskExecution.getDeviceProtocol()).thenReturn(deviceProtocol);
        createMockedComTaskWithGivenProtocolTasks();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceProtocolPluggableClass serverDeviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(serverDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(serverDeviceProtocolPluggableClass);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(serverDeviceProtocolPluggableClass);
        when(serverDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        jobExecution.prepareAll(Arrays.asList(comTaskExecution));
        verify(genericDeviceProtocol, never()).organizeComCommands(any(CommandRoot.class));
    }

    @Test
    public void basicCheckIsInFrontWhenAlreadyInFrontTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask, topologyTask);

        // business method
        final JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandType, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.BASIC_CHECK_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);
    }

    @Test
    public void basicCheckInFrontWhenLastTaskTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        ProtocolTask loadProfilesTask = mock(LoadProfilesTask.class);
        ProtocolTask logBooksTask = mock(LogBooksTask.class);
        createMockedComTaskWithGivenProtocolTasks(loadProfilesTask, logBooksTask, topologyTask, basicCheckTask);

        // business method
        final JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandType, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.BASIC_CHECK_COMMAND, ComCommandTypes.LOAD_PROFILE_COMMAND, ComCommandTypes.LOGBOOKS_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);
    }

    @Test
    public void basicCheckInFrontWhenOnlyBasicCheckExistsTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);

        // business method
        final JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandType, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.BASIC_CHECK_COMMAND);

    }

    @Test
    public void basicCheckInFrontWhenNoBasicCheckExistsTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        ProtocolTask loadProfilesTask = mock(LoadProfilesTask.class);
        ProtocolTask logBooksTask = mock(LogBooksTask.class);
        createMockedComTaskWithGivenProtocolTasks(loadProfilesTask, logBooksTask, topologyTask);

        // business method
        final JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandType, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.LOAD_PROFILE_COMMAND, ComCommandTypes.LOGBOOKS_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);

    }

    @Test
    public void timeDifferenceExceedsMaxShouldFailCompleteSessionTest() throws ConnectionException {
        Date meterTime = new DateTime(2013, 9, 18, 16, 0, 0, 0).toDate();
        Date systemTime = new DateTime(2013, 9, 18, 15, 0, 0, 0).toDate();
        when(this.clock.instant()).thenReturn(systemTime.toInstant());
        when(deviceProtocol.getTime()).thenReturn(meterTime);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        ExecutionContext executionContext = jobExecution.newExecutionContext(this.connectionTask, this.comPort, true);
        jobExecution.setExecutionContext(executionContext);

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        when(comTaskPreparationContext.getDeviceProtocol()).thenReturn(deviceProtocol);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(TimeDuration.seconds(1)));
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);

        // Business method
        JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);

        ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup = getJobExecutionForBasicCheckInFrontTests();
        scheduledComTaskExecutionGroup.setExecutionContext(scheduledComTaskExecutionGroup.newExecutionContext(this.connectionTask, this.comPort, true));

        scheduledComTaskExecutionGroup.establishConnection();
        scheduledComTaskExecutionGroup.performPreparedComTaskExecution(preparedComTaskExecution);

        assertThat(scheduledComTaskExecutionGroup.getExecutionContext().basickCheckHasFailed()).isTrue();
    }

    @Test
    public void serialNumberMisMatchShouldFailCompleteSessionTest() throws ConnectionException {
        String meterSerial = "ThisIsTheMeterSerialNumber";
        String configuredSerial = "ThisIsTheConfiguredSerialNumber";
        when(deviceProtocol.getSerialNumber()).thenReturn(meterSerial);
        when(this.offlineDevice.getSerialNumber()).thenReturn(configuredSerial);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();
        jobExecution.setExecutionContext(jobExecution.newExecutionContext(this.connectionTask, this.comPort, true));

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        when(comTaskPreparationContext.getDeviceProtocol()).thenReturn(deviceProtocol);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.empty());
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);
        jobExecution.setExecutionContext(jobExecution.newExecutionContext(this.connectionTask, this.comPort, true));

        // Business method
        JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);

        ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup = getJobExecutionForBasicCheckInFrontTests();
        scheduledComTaskExecutionGroup.setExecutionContext(scheduledComTaskExecutionGroup.newExecutionContext(this.connectionTask, this.comPort, true));

        scheduledComTaskExecutionGroup.establishConnection();
        scheduledComTaskExecutionGroup.performPreparedComTaskExecution(preparedComTaskExecution);

        assertThat(scheduledComTaskExecutionGroup.getExecutionContext().basickCheckHasFailed()).isTrue();
    }

    private void createMockedComTaskWithGivenProtocolTasks(ProtocolTask... protocolTasks) {
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
    }

    private ScheduledComTaskExecutionGroup getJobExecutionForBasicCheckInFrontTests() {
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        when(outboundComPort.getComServer()).thenReturn(this.comServer);
        return new ScheduledComTaskExecutionGroup(outboundComPort, this.comServerDAO, this.deviceCommandExecutor, connectionTask, this.jobExecutionServiceProvider);
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

    /**
     * Extending class, used to skip the prepare method
     */
    public class MockScheduledComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

        public MockScheduledComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
            super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
        }

        @Override
        PreparedComTaskExecution getPreparedComTaskExecution(ComTaskPreparationContext comTaskPreparationContext, ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
            JobExecution.PreparedComTaskExecution preparedComTaskExecution = mock(JobExecution.PreparedComTaskExecution.class);
            when(preparedComTaskExecution.getComTaskExecution()).thenReturn(comTaskExecution);
            when(preparedComTaskExecution.getCommandRoot()).thenReturn(root);
            return preparedComTaskExecution;
        }
    }

}