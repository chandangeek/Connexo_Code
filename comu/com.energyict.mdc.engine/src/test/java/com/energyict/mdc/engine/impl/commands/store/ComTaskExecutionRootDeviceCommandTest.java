package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.SQLException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link ComTaskExecutionRootDeviceCommand} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-18 (11:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionRootDeviceCommandTest {

    private static final long COM_TASK_EXECUTION_ID = 97;

    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    private ComServerDAOImpl mockComServerDAOButPerformTransactions() {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        when(comServerDAO.executeTransaction(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>) invocation.getArguments()[0]).perform();
            }
        });
        return comServerDAO;
    }

    @Before
    public void initializeMocks () throws SQLException, BusinessException {
        when(this.comTask.getName()).thenReturn(FailureLoggerImplTest.class.getSimpleName());
        when(this.comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask));
    }

    @Test
    public void testExecuteLogFailures () {
        DeviceCommand deviceCommand = this.mockDeviceCommand();
        String errorMessage = "ComTaskExecutionRootDeviceCommandTest#testExecuteLogFailures";
        ComServerDAO comServerDAO = mockComServerDAOButPerformTransactions();
        ApplicationException toBeThrown = new ApplicationException(errorMessage);
        Mockito.doThrow(toBeThrown).when(deviceCommand).execute(comServerDAO);
        ComTaskExecutionRootDeviceCommand command = new ComTaskExecutionRootDeviceCommand(this.comTaskExecution, ComServer.LogLevel.INFO, Arrays.asList(deviceCommand), false, serviceProvider);
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
        ComTaskExecutionRootDeviceCommand command = new ComTaskExecutionRootDeviceCommand(this.comTaskExecution, ComServer.LogLevel.INFO, Arrays.asList(deviceCommand), false, serviceProvider);
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