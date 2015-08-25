package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.common.AddPropertiesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolTerminateCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ForceClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadRegistersCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.meterdata.ComTaskExecutionCollectedData;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the CommandRootImpl component
 *
 * @author gna
 * @since 10/05/12 - 16:48
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandRootImplTest extends CommonCommandImplTests {

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private Device device;

    private final TimeDuration MAX_CLOCK_DIFF = new TimeDuration(8);
    private final TimeDuration MIN_CLOCK_DIFF = new TimeDuration(2);
    private final TimeDuration MAX_CLOCK_SHIFT = new TimeDuration(5);

    @Before
    public void initializeMocks() {
        when(this.comTaskExecution.getDevice()).thenReturn(this.device);
        when(this.device.getmRID()).thenReturn(CommandRootImplTest.class.getSimpleName());
    }

    @Test
    public void getEmptyRootCommandsNotNullTest() {
        CommandRoot commandRoot = createCommandRoot();

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        // set all the options to false
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(false);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(false);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.FORCECLOCK);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(MAX_CLOCK_DIFF));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(MAX_CLOCK_SHIFT));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(MIN_CLOCK_DIFF));

        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(MAX_CLOCK_DIFF));

        RegistersTask registersTask = mock(RegistersTask.class);

        LogBooksTask logBooksTask = mock(LogBooksTask.class);

        LoadProfileCommand loadProfileCommand = commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution);
        ClockCommand clockCommand = commandRoot.getClockCommand(clockTask, commandRoot, comTaskExecution);
        //MessagesCommand messagesCommand = commandRoot.getMessagesCommand(messagesTask, commandRoot, comTaskExecution);
        RegisterCommand registerCommand = commandRoot.getRegisterCommand(registersTask, commandRoot, comTaskExecution);
        LogBooksCommand logBooksCommand = commandRoot.getLogBooksCommand(logBooksTask, commandRoot, comTaskExecution);
        BasicCheckCommand basicCheckCommand = commandRoot.getBasicCheckCommand(basicCheckTask, commandRoot, comTaskExecution);

        // Asserts
        assertThat(loadProfileCommand).isNotNull();
        assertThat(commandRoot.getTimeDifferenceCommand(commandRoot, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getCreateMeterEventsFromStatusFlagsCommand(loadProfileCommand, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getVerifyLoadProfileCommand(loadProfileCommand, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution)).isNotNull();
        assertThat(clockCommand).isNotNull();
        assertThat(commandRoot.getForceClockCommand(clockCommand, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getSetClockCommand(clockCommand, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getSynchronizeClockCommand(clockCommand, comTaskExecution)).isNotNull();
        assertThat(registerCommand).isNotNull();
        //assertThat(messagesCommand).isNotNull();
        assertThat(basicCheckCommand).isNotNull();
        assertThat(logBooksCommand).isNotNull();
        assertThat(commandRoot.getVerifySerialNumberCommand(basicCheckCommand, comTaskExecution)).isNotNull();
        assertThat(commandRoot.getStatusInformationCommand(commandRoot, comTaskExecution)).isNotNull();
    }

    @Test
    public void getGetCollectedDataCallsSubCommands () {
        CommandRoot commandRoot = createCommandRoot();

        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        when(loadProfileCommand.getCommandType()).thenReturn(ComCommandTypes.LOAD_PROFILE_COMMAND);
        commandRoot.addUniqueCommand(loadProfileCommand, comTaskExecution);

        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        commandRoot.addUniqueCommand(clockCommand, comTaskExecution);

        //MessagesCommand messagesCommand = mock(MessagesCommand.class);
        //when(messagesCommand.getCommandType()).thenReturn(ComCommandTypes.MESSAGES_COMMAND);
        //commandRoot.addCommand(messagesCommand, comTaskExecution);

        RegisterCommand registerCommand = mock(RegisterCommand.class);
        when(registerCommand.getCommandType()).thenReturn(ComCommandTypes.REGISTERS_COMMAND);
        commandRoot.addUniqueCommand(registerCommand, comTaskExecution);

        LogBooksCommand logBooksCommand = mock(LogBooksCommand.class);
        when(logBooksCommand.getCommandType()).thenReturn(ComCommandTypes.LOGBOOKS_COMMAND);
        commandRoot.addUniqueCommand(logBooksCommand, comTaskExecution);

        // Business method
        commandRoot.getCollectedData();

        // Asserts
        verify(loadProfileCommand).getCollectedData();
        verify(clockCommand).getCollectedData();
        //verify(messagesCommand).getCollectedData();
        verify(registerCommand).getCollectedData();
        verify(logBooksCommand).getCollectedData();
    }

    @Test
    public void getGetCollectedDataCollectsFromSubCommands () {
        CommandRoot commandRoot = createCommandRoot();

        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        when(loadProfileCommand.getCommandType()).thenReturn(ComCommandTypes.LOAD_PROFILE_COMMAND);
        commandRoot.addUniqueCommand(loadProfileCommand, comTaskExecution);
        ServerCollectedData loadProfileCollectedData = mock(ServerCollectedData.class);
        when(loadProfileCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(loadProfileCollectedData));

        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        commandRoot.addUniqueCommand(clockCommand, comTaskExecution);
        ServerCollectedData clockCollectedData = mock(ServerCollectedData.class);
        when(clockCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(clockCollectedData));

//        MessagesCommand messagesCommand = mock(MessagesCommand.class);
//        when(messagesCommand.getCommandType()).thenReturn(ComCommandTypes.MESSAGES_COMMAND);
//        commandRoot.addCommand(messagesCommand, comTaskExecution);
//        ServerCollectedData messagesCollectedData = mock(ServerCollectedData.class);
//        when(messagesCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(messagesCollectedData));

        RegisterCommand registerCommand = mock(RegisterCommand.class);
        when(registerCommand.getCommandType()).thenReturn(ComCommandTypes.REGISTERS_COMMAND);
        commandRoot.addUniqueCommand(registerCommand, comTaskExecution);
        ServerCollectedData registerCollectedData = mock(ServerCollectedData.class);
        when(registerCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(registerCollectedData));

        LogBooksCommand logBooksCommand = mock(LogBooksCommand.class);
        when(logBooksCommand.getCommandType()).thenReturn(ComCommandTypes.LOGBOOKS_COMMAND);
        commandRoot.addUniqueCommand(logBooksCommand, comTaskExecution);
        ServerCollectedData logBooksCollectedData = mock(ServerCollectedData.class);
        when(logBooksCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(logBooksCollectedData));

        // Business method
        List<CollectedData> collectedData = commandRoot.getCollectedData();
        List<CollectedData> actualCollectedData = new ArrayList<>();

        // Asserts
        assertThat(collectedData).hasSize(1);   // Since all command are for the same ComTaskExecution
        CollectedData data = collectedData.get(0);
        assertThat(data).isInstanceOf(ComTaskExecutionCollectedData.class);
        ComTaskExecutionCollectedData comTaskExecutionCollectedData = (ComTaskExecutionCollectedData) data;
        actualCollectedData.addAll(comTaskExecutionCollectedData.getElements());
        //assertThat(actualCollectedData).containsOnly(loadProfileCollectedData, clockCollectedData, messagesCollectedData, registerCollectedData, logBooksCollectedData);
        assertThat(actualCollectedData).containsOnly(loadProfileCollectedData, clockCollectedData, registerCollectedData, logBooksCollectedData);
    }

    @Test
    public void comServerRuntimeExceptionWithDeviceProtocolTerminateAndUpdateCacheTest() {
        CommandRoot commandRoot = createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();

        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = mock(DeviceProtocolTerminateCommand.class);
        when(deviceProtocolTerminateCommand.getCommandType()).thenReturn(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE);
        DeviceProtocolUpdateCacheCommand deviceProtocolUpdateCacheCommand = mock(DeviceProtocolUpdateCacheCommand.class);
        when(deviceProtocolUpdateCacheCommand.getCommandType()).thenReturn(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
        AddPropertiesCommand addPropertiesCommand = mock(AddPropertiesCommand.class);
        when(addPropertiesCommand.getCommandType()).thenReturn(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        Mockito.doThrow(new DataParseException(new IndexOutOfBoundsException("Some message"), mock(MessageSeed.class))).when(addPropertiesCommand).doExecute(deviceProtocol, executionContext);
        commandRoot.addUniqueCommand(addPropertiesCommand, comTaskExecution);
        commandRoot.addUniqueCommand(deviceProtocolTerminateCommand, comTaskExecution);
        commandRoot.addUniqueCommand(deviceProtocolUpdateCacheCommand, comTaskExecution);

        try {
            commandRoot.execute(deviceProtocol, executionContext);
        } catch (DataParseException e) {
            // if we get the exception we need to verify if the terminate command and updateCache command is called first
            verify(deviceProtocolTerminateCommand).execute(deviceProtocol, executionContext);
            verify(deviceProtocolUpdateCacheCommand).execute(deviceProtocol, executionContext);
        }
    }

    @Test
    public void communicationExceptionWithDeviceProtocolTerminateAndUpdateCacheTest() {
        CommandRoot commandRoot = createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();

        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = mock(DeviceProtocolTerminateCommand.class);
        when(deviceProtocolTerminateCommand.getCommandType()).thenReturn(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE);
        DeviceProtocolUpdateCacheCommand deviceProtocolUpdateCacheCommand = mock(DeviceProtocolUpdateCacheCommand.class);
        when(deviceProtocolUpdateCacheCommand.getCommandType()).thenReturn(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
        AddPropertiesCommand addPropertiesCommand = mock(AddPropertiesCommand.class);
        when(addPropertiesCommand.getCommandType()).thenReturn(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        Mockito.doThrow(new ConnectionCommunicationException(5)).when(addPropertiesCommand).doExecute(deviceProtocol, executionContext);
        commandRoot.addUniqueCommand(addPropertiesCommand, comTaskExecution);
        commandRoot.addUniqueCommand(deviceProtocolTerminateCommand, comTaskExecution);
        commandRoot.addUniqueCommand(deviceProtocolUpdateCacheCommand, comTaskExecution);

        try {
            commandRoot.execute(deviceProtocol, executionContext);
        } catch (ConnectionCommunicationException e) {
            // if we get the exception we need to verify if the terminate command and updateCache command is called first
            verify(deviceProtocolTerminateCommand).execute(deviceProtocol, executionContext);
            verify(deviceProtocolUpdateCacheCommand).execute(deviceProtocol, executionContext);
        }
    }

    @Test
    public void normalCommandCantBeExecutedAfterExceptionTest() {
        CommandRoot commandRoot = createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();

        AddPropertiesCommand addPropertiesCommand = mock(AddPropertiesCommand.class);
        when(addPropertiesCommand.getCommandType()).thenReturn(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        ForceClockCommandImpl forceClockCommand = mock(ForceClockCommandImpl.class);
        when(forceClockCommand.getCommandType()).thenReturn(ComCommandTypes.FORCE_CLOCK_COMMAND);
        ReadRegistersCommandImpl readRegistersCommand = mock(ReadRegistersCommandImpl.class);
        when(readRegistersCommand.getCommandType()).thenReturn( ComCommandTypes.READ_REGISTERS_COMMAND);
        Mockito.doThrow(new ConnectionCommunicationException(5)).when(addPropertiesCommand).doExecute(deviceProtocol, executionContext);
        commandRoot.addUniqueCommand(addPropertiesCommand, comTaskExecution);
        commandRoot.addUniqueCommand(forceClockCommand, comTaskExecution);
        commandRoot.addUniqueCommand(readRegistersCommand, comTaskExecution);

        try {
            commandRoot.execute(deviceProtocol, executionContext);
        } catch (ConnectionCommunicationException e) {
            verify(forceClockCommand, never()).execute(deviceProtocol, executionContext);
            verify(readRegistersCommand, never()).execute(deviceProtocol, executionContext);
        }
    }

    @Test
    public void logOffAndDaisyChainedLogOffCantBeCalledAfterCommunicationExceptionsTest() {
        CommandRoot commandRoot = createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();

        AddPropertiesCommand addPropertiesCommand = mock(AddPropertiesCommand.class);
        when(addPropertiesCommand.getCommandType()).thenReturn(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        LogOffCommand logOffCommand = mock(LogOffCommand.class);
        when(logOffCommand.getCommandType()).thenReturn(ComCommandTypes.LOGOFF);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = mock(DaisyChainedLogOffCommand.class);
        when(daisyChainedLogOffCommand.getCommandType()).thenReturn(ComCommandTypes.DAISY_CHAINED_LOGOFF);
        Mockito.doThrow(new ConnectionCommunicationException(5)).when(addPropertiesCommand).doExecute(deviceProtocol, executionContext);
        commandRoot.addUniqueCommand(addPropertiesCommand, comTaskExecution);
        commandRoot.addUniqueCommand(daisyChainedLogOffCommand, comTaskExecution);
        commandRoot.addUniqueCommand(logOffCommand, comTaskExecution);

        try {
            commandRoot.execute(deviceProtocol, executionContext);
        } catch (ConnectionCommunicationException e) {
            verify(daisyChainedLogOffCommand, never()).execute(deviceProtocol, executionContext);
            verify(logOffCommand, never()).execute(deviceProtocol, executionContext);
        }
    }
    @Test
    public void logOffAndDaisyChainedLogOffMustBeCalledAfterANonCommunicationExceptionsTest() {
        CommandRoot commandRoot = createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();

        AddPropertiesCommand addPropertiesCommand = mock(AddPropertiesCommand.class);
        when(addPropertiesCommand.getCommandType()).thenReturn(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        LogOffCommand logOffCommand = mock(LogOffCommand.class);
        when(logOffCommand.getCommandType()).thenReturn(ComCommandTypes.LOGOFF);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = mock(DaisyChainedLogOffCommand.class);
        when(daisyChainedLogOffCommand.getCommandType()).thenReturn(ComCommandTypes.DAISY_CHAINED_LOGOFF);
        Mockito.doThrow(new DataParseException(new IndexOutOfBoundsException("Some message"), mock(MessageSeed.class))).when(addPropertiesCommand).doExecute(deviceProtocol, executionContext);
        commandRoot.addUniqueCommand(addPropertiesCommand, comTaskExecution);
        commandRoot.addUniqueCommand(daisyChainedLogOffCommand, comTaskExecution);
        commandRoot.addUniqueCommand(logOffCommand, comTaskExecution);

        try {
            commandRoot.execute(deviceProtocol, executionContext);
        } catch (ConnectionCommunicationException e) {
            verify(daisyChainedLogOffCommand).execute(deviceProtocol, executionContext);
            verify(logOffCommand).execute(deviceProtocol, executionContext);
        }
    }

    @Test
    public void commandRootHasExecutedTest() {
        CommandRootImpl commandRoot = (CommandRootImpl) createCommandRoot();
        assertThat(commandRoot.hasExecuted()).isFalse();
    }

    @Test
    public void executeComCommandWhenNoComTaskExecutionWasProvidedToTheRootTest() {
        CommandRootImpl commandRoot = (CommandRootImpl) createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();
        ComCommand comCommand = mock(ComCommand.class);
        commandRoot.performTheComCommandIfAllowed(deviceProtocol, executionContext, comCommand);

        verify(comCommand, times(1)).execute(deviceProtocol, executionContext);
    }

    @Test
    public void performTheComCommandIfNOTAllowedTest() {
        CommandRootImpl commandRoot = (CommandRootImpl) createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComCommand comCommand = mock(ComCommand.class);
        commandRoot.addUniqueCommand(comCommand, comTaskExecution);

        JobExecution.PreparedComTaskExecution preparedComTaskExecution = mock(JobExecution.PreparedComTaskExecution.class);
        when(preparedComTaskExecution.getDeviceProtocol()).thenReturn(deviceProtocol);
        commandRoot.executeFor(preparedComTaskExecution, executionContext);

        verify(comCommand, never()).execute(any(DeviceProtocol.class), any(ExecutionContext.class));
    }


    @Test
    public void performTheComCommandIfAllowedTest() {
        CommandRootImpl commandRoot = (CommandRootImpl) createCommandRoot();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        final ExecutionContext executionContext = commandRoot.getExecutionContext();
        ComCommand comCommand = mock(ComCommand.class);
        ComTaskExecution comTaskExecution = executionContext.getComTaskExecution();
        commandRoot.addUniqueCommand(comCommand, comTaskExecution);

        JobExecution.PreparedComTaskExecution preparedComTaskExecution = mock(JobExecution.PreparedComTaskExecution.class);
        when(preparedComTaskExecution.getComTaskExecution()).thenReturn(comTaskExecution);
        when(preparedComTaskExecution.getDeviceProtocol()).thenReturn(deviceProtocol);
        commandRoot.executeFor(preparedComTaskExecution, executionContext);

        verify(comCommand, times(1)).execute(any(DeviceProtocol.class), any(ExecutionContext.class));
    }



}