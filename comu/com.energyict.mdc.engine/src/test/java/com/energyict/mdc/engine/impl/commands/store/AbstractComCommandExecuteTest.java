package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.communication.tasks.ProtocolDialectConfigurationPropertiesFactory;
import com.energyict.mdc.communication.tasks.ProtocolDialectPropertiesFactory;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.runner.RunWith;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    @Mock
    private ProtocolDialectConfigurationPropertiesFactory protocolDialectConfigurationPropertiesFactory;
    @Mock
    protected ServerManager manager;
    @Mock
    private ProtocolDialectPropertiesFactory protocolDialectPropertiesFactory;

    @Before
    public void setUpManager() throws Exception {
        when(this.protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIG_PROPS_ID);
        when(this.protocolDialectConfigurationPropertiesFactory.find(PROTOCOL_DIALECT_CONFIG_PROPS_ID)).thenReturn(this.protocolDialectConfigurationProperties);
        when(this.manager.getProtocolDialectConfigurationPropertiesFactory()).thenReturn(this.protocolDialectConfigurationPropertiesFactory);
        when(this.manager.getProtocolDialectPropertiesFactory()).thenReturn(this.protocolDialectPropertiesFactory);
        ManagerFactory.setCurrent(this.manager);
    }

    protected static JobExecution.ExecutionContext newTestExecutionContext () {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    protected static JobExecution.ExecutionContext newTestExecutionContext (Logger logger) {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(comPortPool.getId()).thenReturn((long)COMPORT_POOL_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.getDevice()).thenReturn(device);
        JobExecution.ExecutionContext executionContext =
                new JobExecution.ExecutionContext(
                        mock(JobExecution.class),
                        connectionTask,
                        comPort, issueService);
        executionContext.setLogger(logger);
        executionContext.start(comTaskExecution);
        return executionContext;
    }
}