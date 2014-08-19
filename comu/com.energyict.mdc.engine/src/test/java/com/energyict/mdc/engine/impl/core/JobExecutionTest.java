package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.GenericDeviceProtocol;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.DeviceProtocolCommandCreator;
import com.energyict.mdc.engine.impl.core.aspects.ComServerEventServiceProviderAdapter;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TopologyTask;

import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.channels.VoidComChannel;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test to check if the organizeComCommands() method on the interface GenericDeviceProtocol is triggered correctly
 * <p/>
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
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private GenericDeviceProtocol genericDeviceProtocol;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private DeviceDataService deviceDataService;
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

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private IssueService issueService = new FakeIssueService();
    private CommandRootImpl root;
    private CommandRootImpl root2;

    public void setupServiceProvider () {
        this.serviceProvider.setIssueService(this.issueService);
        this.serviceProvider.setTransactionService(new FakeTransactionService());
        this.serviceProvider.setDeviceDataService(this.deviceDataService);
        this.serviceProvider.setEngineService(this.engineService);
        this.serviceProvider.setDeviceConfigurationService(this.deviceConfigurationService);
        this.serviceProvider.setClock(this.clock);
        ServiceProvider.instance.set(this.serviceProvider);
    }

    @After
    public void restServiceProvider () {
        ServiceProvider.instance.set(null);
    }

    @Before
    public void setupEventPublisher () {
        this.setupServiceProvider();
        EventPublisherImpl.setInstance(this.eventPublisher);
        when(this.eventPublisher.serviceProvider()).thenReturn(new ComServerEventServiceProviderAdapter());
    }

    @After
    public void resetEventPublisher () {
        EventPublisherImpl.setInstance(null);
    }

    @Before
    public void initMocks() throws ConnectionException {
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(device.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(this.deviceDataService.findDeviceById(DEVICE_ID)).thenReturn(this.device);
        ConnectionTask ct = connectionTask;
        when(comTaskExecution.getConnectionTask()).thenReturn(ct);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.connect(comPort)).thenReturn(new VoidComChannel());
        doNothing().when(comServerDAO).executionCompleted(comTaskExecution);
        when(comPort.getComServer()).thenReturn(this.comServer);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);

        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        when(comSessionBuilder.addComTaskExecutionSession(eq(this.comTaskExecution), eq(this.device), any(Date.class))).
            thenReturn(mock(ComTaskExecutionSessionBuilder.class));
        when(this.deviceDataService.buildComSession(eq(this.connectionTask), eq(this.comPortPool), eq(this.comPort), any(Date.class))).
            thenReturn(comSessionBuilder);
        when(this.deviceConfigurationService.findComTaskEnablement(any(ComTask.class), eq(this.deviceConfiguration))).
            thenReturn(Optional.of(this.comTaskEnablement));
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.comTaskEnablement.getSecurityPropertySet()).thenReturn(this.securityPropertySet);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(1);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(1);
        when(this.securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(this.securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);

        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.<DeviceCache>absent());

        ExecutionContext executionContext = newTestExecutionContext();
        root = spy(new CommandRootImpl(offlineDevice, executionContext, this.serviceProvider));
        root2 = spy(new CommandRootImpl(offlineDevice, executionContext, this.serviceProvider));
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
        ScheduledComTaskExecutionGroup jobExecution = spy(new MockScheduledComTaskExecutionGroup(outboundComPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, connectionTask));
        ExecutionContext executionContext = newTestExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(preparedComTaskExecution.getDeviceProtocol()).thenReturn(genericDeviceProtocol);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(genericDeviceProtocol);
        createMockedComTaskWithGivenProtocolTasks();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
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
        ScheduledComTaskExecutionGroup jobExecution = spy(new MockScheduledComTaskExecutionGroup(outboundComPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, connectionTask));
        ExecutionContext executionContext = newTestExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(preparedComTaskExecution.getDeviceProtocol()).thenReturn(deviceProtocol);
        createMockedComTaskWithGivenProtocolTasks();
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
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
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, device, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandTypes, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
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
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, device, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandTypes, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
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
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, device, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandTypes, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
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
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, device, deviceProtocolSecurityPropertySet);

        // asserts
        assertThat(preparedComTaskExecution).isNotNull();
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        final Map<ComCommandTypes, ComCommand> commands = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commands).isNotEmpty();
        assertThat(commands.keySet()).containsSequence(ComCommandTypes.LOAD_PROFILE_COMMAND, ComCommandTypes.LOGBOOKS_COMMAND, ComCommandTypes.TOPOLOGY_COMMAND);

    }

    @Test
    public void timeDifferenceExceedsMaxShouldFailCompleteSessionTest() throws ConnectionException {
        Date meterTime = new DateTime(2013, 9, 18, 16, 0, 0, 0).toDate();
        Date systemTime = new DateTime(2013, 9, 18, 15, 0, 0, 0).toDate();
        when(this.clock.now()).thenReturn(systemTime);
        when(deviceProtocol.getTime()).thenReturn(meterTime);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();

        jobExecution.setExecutionContext(jobExecution.newExecutionContext(this.connectionTask, this.comPort));

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        when(comTaskPreparationContext.getDeviceProtocol()).thenReturn(deviceProtocol);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(new TimeDuration(1, TimeDuration.SECONDS));
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);

        // Business method
        JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, device, deviceProtocolSecurityPropertySet);

        ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup = getJobExecutionForBasicCheckInFrontTests();
        scheduledComTaskExecutionGroup.setExecutionContext(scheduledComTaskExecutionGroup.newExecutionContext(this.connectionTask, this.comPort));

        scheduledComTaskExecutionGroup.establishConnectionFor();
        scheduledComTaskExecutionGroup.performPreparedComTaskExecution(preparedComTaskExecution);

        assertThat(scheduledComTaskExecutionGroup.getExecutionContext().hasBasicCheckFailed()).isTrue();
    }

    @Test
    public void serialNumberMisMatchShouldFailCompleteSessionTest() throws ConnectionException {
        String meterSerial = "ThisIsTheMeterSerialNumber";
        String configuredSerial = "ThisIsTheConfiguredSerialNumber";
        when(deviceProtocol.getSerialNumber()).thenReturn(meterSerial);
        when(this.offlineDevice.getSerialNumber()).thenReturn(configuredSerial);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        ScheduledComTaskExecutionGroup jobExecution = getJobExecutionForBasicCheckInFrontTests();
        jobExecution.setExecutionContext(jobExecution.newExecutionContext(this.connectionTask, this.comPort));

        JobExecution.ComTaskPreparationContext comTaskPreparationContext = mock(JobExecution.ComTaskPreparationContext.class);
        when(comTaskPreparationContext.getCommandCreator()).thenReturn(new DeviceProtocolCommandCreator());
        when(comTaskPreparationContext.getRoot()).thenReturn(root);
        when(comTaskPreparationContext.getDeviceProtocol()).thenReturn(deviceProtocol);
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = mock(ComTaskExecutionConnectionSteps.class);
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        createMockedComTaskWithGivenProtocolTasks(basicCheckTask);
        jobExecution.setExecutionContext(jobExecution.newExecutionContext(this.connectionTask, this.comPort));

        // Business method
        JobExecution.PreparedComTaskExecution preparedComTaskExecution =
                jobExecution.getPreparedComTaskExecution(comTaskPreparationContext, comTaskExecution, comTaskExecutionConnectionSteps, device, deviceProtocolSecurityPropertySet);

        ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup = getJobExecutionForBasicCheckInFrontTests();
        scheduledComTaskExecutionGroup.setExecutionContext(scheduledComTaskExecutionGroup.newExecutionContext(this.connectionTask, this.comPort));

        scheduledComTaskExecutionGroup.establishConnectionFor();
        scheduledComTaskExecutionGroup.performPreparedComTaskExecution(preparedComTaskExecution);

        assertThat(scheduledComTaskExecutionGroup.getExecutionContext().hasBasicCheckFailed()).isTrue();
    }

    private void createMockedComTaskWithGivenProtocolTasks(ProtocolTask... protocolTasks) {
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(protocolTasks));
    }

    private ScheduledComTaskExecutionGroup getJobExecutionForBasicCheckInFrontTests() {
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        return new ScheduledComTaskExecutionGroup(outboundComPort, this.comServerDAO, this.deviceCommandExecutor, connectionTask, this.serviceProvider);
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
        PreparedComTaskExecution getPreparedComTaskExecution(ComTaskPreparationContext comTaskPreparationContext, ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, BaseDevice masterDevice, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
            JobExecution.PreparedComTaskExecution preparedComTaskExecution = mock(JobExecution.PreparedComTaskExecution.class);
            when(preparedComTaskExecution.getComTaskExecution()).thenReturn(comTaskExecution);
            when(preparedComTaskExecution.getCommandRoot()).thenReturn(root);
            return preparedComTaskExecution;
        }
    }

}