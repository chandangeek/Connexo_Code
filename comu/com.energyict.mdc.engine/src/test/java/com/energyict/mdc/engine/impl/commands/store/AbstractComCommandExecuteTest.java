package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootServiceProviderAdapter;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.Clock;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

/**
 * Provides code reuse opportunities for ComCommand execute tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-16 (11:44)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractComCommandExecuteTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = DEVICE_ID + 1;
    private static final long PROTOCOL_DIALECT_CONFIG_PROPS_ID = 6516;

    protected static final FakeServiceProvider serviceProvider = new FakeServiceProvider();
    protected static CommandRoot.ServiceProvider commandRootServiceProvider = new CommandRootServiceProviderAdapter(serviceProvider);

    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    protected EventPublisherImpl eventPublisher;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    
    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void setupServiceProvider() {
        serviceProvider.setDeviceConfigurationService(deviceConfigurationService);
        serviceProvider.setClock(this.clock);
        serviceProvider.setIssueService(new IssueServiceImpl(this.clock));
        serviceProvider.setConnectionTaskService(mock(ConnectionTaskService.class, RETURNS_DEEP_STUBS));
        serviceProvider.setDeviceService(mock(DeviceService.class, RETURNS_DEEP_STUBS));
        serviceProvider.setNlsService(nlsService);
        ServiceProvider.instance.set(serviceProvider);
        when(nlsService.getThesaurus(any(), any())).thenReturn(thesaurus);
        when(thesaurus.getStringBeyondComponent(any(), any())).thenAnswer(invocationOnMock -> (String) invocationOnMock.getArguments()[0]);

    }

    @After
    public void resetServiceProvider() {
        serviceProvider.setClock(Clock.systemDefaultZone());
        ServiceProvider.instance.set(null);
    }

    @Before
    public void setUpManager() throws Exception {
        when(this.protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIG_PROPS_ID);
        when(deviceConfigurationService.getProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_CONFIG_PROPS_ID)).thenReturn(Optional.of(protocolDialectConfigurationProperties));
    }

    protected static ExecutionContext newTestExecutionContext() {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    protected static ExecutionContext newTestExecutionContext(Logger logger) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTask comTask = mock(ComTask.class);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.getDevice()).thenReturn(device);
        ExecutionContext executionContext =
                new ExecutionContext(
                        mock(JobExecution.class),
                        connectionTask,
                        comPort,
                        serviceProvider);
        executionContext.setLogger(logger);
        executionContext.start(comTaskExecution, comTask);
        return executionContext;
    }

}