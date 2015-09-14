package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.transaction.TransactionService;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link ComTaskExecutionCollectedData} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-18 (08:42)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionCollectedDataTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    @Test
    public void testPostProcessDelegatesToContainedCollectedData () {
        ServerCollectedData cd1 = mock(ServerCollectedData.class);
        ServerCollectedData cd2 = mock(ServerCollectedData.class);
        ServerCollectedData cd3 = mock(ServerCollectedData.class);

        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(mock(ComTaskExecution.class), Arrays.asList(cd1, cd2, cd3));
        ConnectionTask connectionTask = mock(ConnectionTask.class);

        // Business method
        collectedData.postProcess(connectionTask);

        // Asserts
        verify(cd1).postProcess(connectionTask);
        verify(cd2).postProcess(connectionTask);
        verify(cd3).postProcess(connectionTask);
    }

    @Test
    public void testGetResultTypeDelegatesToContainedCollectedData () {
        ServerCollectedData cd1 = mock(ServerCollectedData.class);
        when(cd1.getResultType()).thenReturn(ResultType.Supported);
        ServerCollectedData cd2 = mock(ServerCollectedData.class);
        when(cd2.getResultType()).thenReturn(ResultType.Supported);
        ServerCollectedData cd3 = mock(ServerCollectedData.class);
        when(cd3.getResultType()).thenReturn(ResultType.Supported);

        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(mock(ComTaskExecution.class), Arrays.asList(cd1, cd2, cd3));

        // Business method
        collectedData.getResultType();

        // Asserts
        verify(cd1).getResultType();
        verify(cd2).getResultType();
        verify(cd3).getResultType();
    }

    @Test
    public void testGetResultTypeWithAllSame () {
        ServerCollectedData cd1 = mock(ServerCollectedData.class);
        ResultType expectedResultType = ResultType.Supported;
        when(cd1.getResultType()).thenReturn(expectedResultType);
        ServerCollectedData cd2 = mock(ServerCollectedData.class);
        when(cd2.getResultType()).thenReturn(expectedResultType);
        ServerCollectedData cd3 = mock(ServerCollectedData.class);
        when(cd3.getResultType()).thenReturn(expectedResultType);

        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(mock(ComTaskExecution.class), Arrays.asList(cd1, cd2, cd3));

        // Business method
        ResultType resultType = collectedData.getResultType();

        // Asserts
        assertThat(resultType).isEqualTo(expectedResultType);
    }

    @Test
    public void testGetResultTypeReturnsHighestPriorityResultTypeOfAll () {
        ServerCollectedData cd1 = mock(ServerCollectedData.class);
        when(cd1.getResultType()).thenReturn(ResultType.Supported);
        ServerCollectedData cd2 = mock(ServerCollectedData.class);
        when(cd2.getResultType()).thenReturn(ResultType.NotSupported);
        ServerCollectedData cd3 = mock(ServerCollectedData.class);
        when(cd3.getResultType()).thenReturn(ResultType.DataIncomplete);

        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(mock(ComTaskExecution.class), Arrays.asList(cd1, cd2, cd3));

        // Business method
        ResultType resultType = collectedData.getResultType();

        // Asserts
        assertThat(resultType).isEqualTo(ResultType.DataIncomplete);
    }

    @Test
    public void testGetIssuesDelegatesToContainedCollectedData () {
        ServerCollectedData cd1 = mock(ServerCollectedData.class);
        ServerCollectedData cd2 = mock(ServerCollectedData.class);
        ServerCollectedData cd3 = mock(ServerCollectedData.class);

        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(mock(ComTaskExecution.class), Arrays.asList(cd1, cd2, cd3));

        // Business method
        collectedData.getIssues();

        // Asserts
        verify(cd1).getIssues();
        verify(cd2).getIssues();
        verify(cd3).getIssues();
    }

    @Test
    public void testToDeviceCommandDelegatesToContainedCollectedData () {
        ServerCollectedData cd1 = mock(ServerCollectedData.class);
        ServerCollectedData cd2 = mock(ServerCollectedData.class);
        ServerCollectedData cd3 = mock(ServerCollectedData.class);

        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(mock(ComTaskExecution.class), Arrays.asList(cd1, cd2, cd3));

        // Business method
        MeterDataStoreCommandImpl meterDataStoreCommand = mock(MeterDataStoreCommandImpl.class);
        collectedData.toDeviceCommand(meterDataStoreCommand, serviceProvider);

        // Asserts
        verify(cd1).toDeviceCommand(meterDataStoreCommand, serviceProvider);
        verify(cd2).toDeviceCommand(meterDataStoreCommand, serviceProvider);
        verify(cd3).toDeviceCommand(meterDataStoreCommand, serviceProvider);
    }

    @Test
    public void testIsConfiguredIn () {
        ComTask comTask = mock(ComTask.class, withSettings().extraInterfaces(DataCollectionConfiguration.class));
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(comTaskExecution, new ArrayList<>(0));

        // Business method
        boolean configuredIn = collectedData.isConfiguredIn((DataCollectionConfiguration) comTask);

        // Asserts
        assertThat(configuredIn).isTrue();
    }

    @Test
    public void testIsNotConfigured () {
        ComTask comTask = mock(ComTask.class, withSettings().extraInterfaces(DataCollectionConfiguration.class));
        ComTask otherComTask = mock(ComTask.class, withSettings().extraInterfaces(DataCollectionConfiguration.class));
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        ComTaskExecutionCollectedData collectedData = new ComTaskExecutionCollectedData(comTaskExecution, new ArrayList<>(0));

        // Business method
        boolean configuredIn = collectedData.isConfiguredIn((DataCollectionConfiguration) otherComTask);

        // Asserts
        assertThat(configuredIn).isFalse();
    }

}