package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootServiceProviderAdapter;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import com.google.common.base.Optional;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.logging.Logger;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Before
    public void setUpManager() throws Exception {
        setupServiceProvider();
        when(this.protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIG_PROPS_ID);
        when(deviceConfigurationService.getProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_CONFIG_PROPS_ID)).thenReturn(Optional.of(protocolDialectConfigurationProperties));
    }

    @Before
    public void setupServiceProvider() {
        serviceProvider.setDeviceConfigurationService(deviceConfigurationService);
        ProgrammableClock clock = new ProgrammableClock();
        serviceProvider.setClock(clock);
        serviceProvider.setIssueService(new IssueServiceImpl(clock));
        serviceProvider.setTaskHistoryService(mock(TaskHistoryService.class, RETURNS_DEEP_STUBS));
        ServiceProvider.instance.set(serviceProvider);
    }

    @After
    public void resetServiceProvider () {
        ServiceProvider.instance.set(null);
    }

    protected static ExecutionContext newTestExecutionContext () {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    protected static ExecutionContext newTestExecutionContext (Logger logger) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(comTaskExecution.getDevice()).thenReturn(device);
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
                        comPort, commandRootServiceProvider);
        executionContext.setLogger(logger);
        executionContext.start(comTaskExecution);
        return executionContext;
    }
}