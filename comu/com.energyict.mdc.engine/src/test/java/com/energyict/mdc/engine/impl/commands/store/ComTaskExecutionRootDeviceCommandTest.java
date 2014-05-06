package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTaskExecution;
import java.sql.SQLException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComTaskExecutionRootDeviceCommand} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-18 (11:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionRootDeviceCommandTest {

    private static final int COM_TASK_EXECUTION_ID = 97;

    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private MdwInterface mdwInterface;
    @Mock
    private ServerManager manager;

    @Before
    public void initializeMocks () throws SQLException, BusinessException {
        when(this.comTask.getName()).thenReturn(FailureLoggerImplTest.class.getSimpleName());
        when(this.comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(this.comTaskExecution.getComTask()).thenReturn(this.comTask);
        when(this.manager.getMdwInterface()).thenReturn(this.mdwInterface);
        when(this.mdwInterface.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer (InvocationOnMock invocation) throws Throwable {
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                return transaction.doExecute();
            }
        });
        ManagerFactory.setCurrent(this.manager);
    }

    @Test
    public void testExecuteLogFailures () {
        DeviceCommand deviceCommand = this.mockDeviceCommand();
        String errorMessage = "ComTaskExecutionRootDeviceCommandTest#testExecuteLogFailures";
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        ApplicationException toBeThrown = new ApplicationException(errorMessage);
        Mockito.doThrow(toBeThrown).when(deviceCommand).execute(comServerDAO);
        ComTaskExecutionRootDeviceCommand command = new ComTaskExecutionRootDeviceCommand(this.comTaskExecution, ComServer.LogLevel.INFO, Arrays.asList(deviceCommand));
        DeviceCommand.ExecutionLogger executionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(executionLogger);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(executionLogger).logUnexpected(toBeThrown, this.comTaskExecution);
    }

    @Test
    public void testExecuteDuringShutdownLogFailures () {
        DeviceCommand deviceCommand = this.mockDeviceCommand();
        String errorMessage = "ComTaskExecutionRootDeviceCommandTest#testExecuteDuringShutdownLogFailures";
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        ApplicationException toBeThrown = new ApplicationException(errorMessage);
        Mockito.doThrow(toBeThrown).when(deviceCommand).executeDuringShutdown(comServerDAO);
        ComTaskExecutionRootDeviceCommand command = new ComTaskExecutionRootDeviceCommand(this.comTaskExecution, ComServer.LogLevel.INFO, Arrays.asList(deviceCommand));
        DeviceCommand.ExecutionLogger executionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(executionLogger);

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(executionLogger).logUnexpected(toBeThrown, this.comTaskExecution);
    }

    private DeviceCommand mockDeviceCommand () {
        return mock(DeviceCommand.class);
    }

}