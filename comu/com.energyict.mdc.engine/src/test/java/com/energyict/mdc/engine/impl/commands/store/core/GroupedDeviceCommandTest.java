/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.MessagesCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.DaisyChainedLogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOffCommand;
import com.energyict.mdc.engine.impl.commands.store.access.LogOnCommand;
import com.energyict.mdc.engine.impl.commands.store.common.AddPropertiesCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolTerminateCommand;
import com.energyict.mdc.engine.impl.commands.store.common.DeviceProtocolUpdateCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.BasicCheckCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ForceClockCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadRegistersCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.meterdata.ComTaskExecutionCollectedData;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link GroupedDeviceCommand} component
 *
 * @author gna
 * @since 10/05/12 - 16:48
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupedDeviceCommandTest extends CommonCommandImplTests {

    private final Optional<TimeDuration> MAX_CLOCK_DIFF = Optional.of(new TimeDuration(8));
    private final Optional<TimeDuration> MIN_CLOCK_DIFF = Optional.of(new TimeDuration(2));
    private final Optional<TimeDuration> MAX_CLOCK_SHIFT = Optional.of(new TimeDuration(5));

    @Before
    public void initializeEventPublisher() throws Exception {
        EventPublisherImpl eventPublisher = mock(EventPublisherImpl.class);
        when(executionContextServiceProvider.eventPublisher()).thenReturn(eventPublisher);
    }

    @Test
    public void getEmptyRootCommandsNotNullTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);

        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        // set all the options to false
        when(loadProfilesTask.createMeterEventsFromStatusFlags()).thenReturn(false);
        when(loadProfilesTask.isMarkIntervalsAsBadTime()).thenReturn(false);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.FORCECLOCK);
        when(clockTask.getMaximumClockDifference()).thenReturn(MAX_CLOCK_DIFF);
        when(clockTask.getMaximumClockShift()).thenReturn(MAX_CLOCK_SHIFT);
        when(clockTask.getMinimumClockDifference()).thenReturn(MIN_CLOCK_DIFF);

        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(MAX_CLOCK_DIFF);

        MessagesTask messagesTask = mock(MessagesTask.class);

        RegistersTask registersTask = mock(RegistersTask.class);

        LogBooksTask logBooksTask = mock(LogBooksTask.class);

        LoadProfileCommand loadProfileCommand = groupedDeviceCommand.getLoadProfileCommand(loadProfilesTask, groupedDeviceCommand, comTaskExecution);
        ClockCommand clockCommand = groupedDeviceCommand.getClockCommand(clockTask, groupedDeviceCommand, comTaskExecution);
        MessagesCommand messagesCommand = groupedDeviceCommand.getMessagesCommand(messagesTask, groupedDeviceCommand, comTaskExecution);
        RegisterCommand registerCommand = groupedDeviceCommand.getRegisterCommand(registersTask, groupedDeviceCommand, comTaskExecution);
        LogBooksCommand logBooksCommand = groupedDeviceCommand.getLogBooksCommand(logBooksTask, groupedDeviceCommand, comTaskExecution);
        BasicCheckCommand basicCheckCommand = groupedDeviceCommand.getBasicCheckCommand(basicCheckTask, groupedDeviceCommand, comTaskExecution);

        // Asserts
        assertNotNull(loadProfileCommand);
        assertNotNull(groupedDeviceCommand.getTimeDifferenceCommand(loadProfileCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getCreateMeterEventsFromStatusFlagsCommand(loadProfileCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getMarkIntervalsAsBadTimeCommand(loadProfileCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getVerifyLoadProfileCommand(loadProfileCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution));
        assertNotNull(clockCommand);
        assertNotNull(groupedDeviceCommand.getForceClockCommand(clockCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getSetClockCommand(clockCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getSynchronizeClockCommand(clockCommand, comTaskExecution));
        assertNotNull(registerCommand);
        assertNotNull(messagesCommand);
        assertNotNull(basicCheckCommand);
        assertNotNull(logBooksCommand);
        assertNotNull(groupedDeviceCommand.getVerifySerialNumberCommand(basicCheckCommand, comTaskExecution));
        assertNotNull(groupedDeviceCommand.getStatusInformationCommand(groupedDeviceCommand, comTaskExecution));
    }

    @Test
    public void getGetCollectedDataCallsSubCommands() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);

        LoadProfileCommand loadProfileCommand = getLoadProfileCommand(groupedDeviceCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(loadProfileCommand, comTaskExecution);

        ClockCommand clockCommand = mockClockCommand();
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);

        MessagesCommand messagesCommand = mockMessageCommand();
        groupedDeviceCommand.addCommand(messagesCommand, comTaskExecution);

        RegisterCommand registerCommand = mockRegisterCommand();
        groupedDeviceCommand.addCommand(registerCommand, comTaskExecution);

        LogBooksCommand logBooksCommand = mockLogBooksCommand();
        groupedDeviceCommand.addCommand(logBooksCommand, comTaskExecution);

        // Business method
        groupedDeviceCommand.getCollectedData();

        // Asserts
        verify(loadProfileCommand).getCollectedData();
        verify(clockCommand).getCollectedData();
        verify(messagesCommand).getCollectedData();
        verify(registerCommand).getCollectedData();
        verify(logBooksCommand).getCollectedData();
    }

    private MessagesCommand mockMessageCommand() {
        MessagesCommand messagesCommand = mock(MessagesCommand.class);
        when(messagesCommand.getCommandType()).thenReturn(ComCommandTypes.MESSAGES_COMMAND);
        return messagesCommand;
    }

    @Test
    public void getGetCollectedDataCollectsFromSubCommands() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);

        LoadProfileCommand loadProfileCommand = getLoadProfileCommand(groupedDeviceCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(loadProfileCommand, comTaskExecution);
        ServerCollectedData loadProfileCollectedData = mock(ServerCollectedData.class);
        when(loadProfileCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(loadProfileCollectedData));

        ClockCommand clockCommand = mockClockCommand();
        groupedDeviceCommand.addCommand(clockCommand, comTaskExecution);
        ServerCollectedData clockCollectedData = mock(ServerCollectedData.class);
        when(clockCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(clockCollectedData));

        MessagesCommand messagesCommand = mockMessageCommand();
        groupedDeviceCommand.addCommand(messagesCommand, comTaskExecution);
        ServerCollectedData messagesCollectedData = mock(ServerCollectedData.class);
        when(messagesCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(messagesCollectedData));

        RegisterCommand registerCommand = mockRegisterCommand();
        groupedDeviceCommand.addCommand(registerCommand, comTaskExecution);
        ServerCollectedData registerCollectedData = mock(ServerCollectedData.class);
        when(registerCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(registerCollectedData));

        LogBooksCommand logBooksCommand = mockLogBooksCommand();
        groupedDeviceCommand.addCommand(logBooksCommand, comTaskExecution);
        ServerCollectedData logBooksCollectedData = mock(ServerCollectedData.class);
        when(logBooksCommand.getCollectedData()).thenReturn(Arrays.<CollectedData>asList(logBooksCollectedData));

        // Business method
        List<CollectedData> collectedData = groupedDeviceCommand.getCollectedData();
        List<CollectedData> actualCollectedData = new ArrayList<>();

        // Asserts
        assertThat(collectedData).hasSize(1);   // Since all command are for the same ComTaskExecution
        CollectedData data = collectedData.get(0);
        assertThat(data).isInstanceOf(ComTaskExecutionCollectedData.class);
        ComTaskExecutionCollectedData comTaskExecutionCollectedData = (ComTaskExecutionCollectedData) data;
        actualCollectedData.addAll(comTaskExecutionCollectedData.getElements());
        assertThat(actualCollectedData).containsOnly(loadProfileCollectedData, clockCollectedData, messagesCollectedData, registerCollectedData, logBooksCollectedData);
    }

    private LogBooksCommand mockLogBooksCommand() {
        LogBooksCommand logBooksCommand = mock(LogBooksCommand.class);
        when(logBooksCommand.getCommandType()).thenReturn(ComCommandTypes.LOGBOOKS_COMMAND);
        return logBooksCommand;
    }

    private RegisterCommand mockRegisterCommand() {
        RegisterCommand registerCommand = mock(RegisterCommand.class);
        when(registerCommand.getCommandType()).thenReturn(ComCommandTypes.REGISTERS_COMMAND);
        return registerCommand;
    }

    private ClockCommand mockClockCommand() {
        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getCommandType()).thenReturn(ComCommandTypes.CLOCK_COMMAND);
        return clockCommand;
    }

    @Test
    public void ProtocolRuntimeExceptionWithDeviceProtocolTerminateAndUpdateCacheTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = mockDeviceProtocolTerminateComCommand();
        DeviceProtocolUpdateCacheCommand deviceProtocolUpdateCacheCommand = mockDeviceProtocolUpdateCacheCommand();
        ComCommand addPropertiesCommand = mockNoneConnectionErrorFailureComCommand(groupedDeviceCommand);
        groupedDeviceCommand.addCommand(addPropertiesCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(deviceProtocolTerminateCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(deviceProtocolUpdateCacheCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);
        verify(deviceProtocolTerminateCommand).execute(deviceProtocol, executionContext);
        verify(deviceProtocolUpdateCacheCommand).execute(deviceProtocol, executionContext);
    }

    private DeviceProtocolUpdateCacheCommand mockDeviceProtocolUpdateCacheCommand() {
        DeviceProtocolUpdateCacheCommand deviceProtocolUpdateCacheCommand = mock(DeviceProtocolUpdateCacheCommand.class);
        when(deviceProtocolUpdateCacheCommand.getCommandType()).thenReturn(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
        when(deviceProtocolUpdateCacheCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        return deviceProtocolUpdateCacheCommand;
    }

    @Test
    public void communicationExceptionWithDeviceProtocolTerminateAndUpdateCacheTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        ComCommand addPropertiesCommand = mockConnectionErrorFailureComCommand(groupedDeviceCommand);
        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = mockDeviceProtocolTerminateComCommand();
        DeviceProtocolUpdateCacheCommand deviceProtocolUpdateCacheCommand = mockDeviceProtocolUpdateCacheCommand();
        groupedDeviceCommand.addCommand(addPropertiesCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(deviceProtocolTerminateCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(deviceProtocolUpdateCacheCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);

        verify(deviceProtocolTerminateCommand).execute(deviceProtocol, executionContext);
        verify(deviceProtocolUpdateCacheCommand).execute(deviceProtocol, executionContext);
    }

    private DeviceProtocolTerminateCommand mockDeviceProtocolTerminateComCommand() {
        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = mock(DeviceProtocolTerminateCommand.class);
        when(deviceProtocolTerminateCommand.getCommandType()).thenReturn(ComCommandTypes.DEVICE_PROTOCOL_TERMINATE);
        when(deviceProtocolTerminateCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        return deviceProtocolTerminateCommand;
    }

    @Test
    public void normalCommandCantBeExecutedAfterExceptionTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        ComCommand addPropertiesCommand = mockConnectionErrorFailureComCommand(groupedDeviceCommand);
        ForceClockCommandImpl forceClockCommand = mockForceClockCommand();
        ReadRegistersCommandImpl readRegistersCommand = mockReadRegistersCommand();

        groupedDeviceCommand.addCommand(addPropertiesCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(forceClockCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(readRegistersCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);

        verify(forceClockCommand, never()).execute(deviceProtocol, executionContext);
        verify(readRegistersCommand, never()).execute(deviceProtocol, executionContext);
    }

    private ComCommand mockConnectionErrorFailureComCommand(GroupedDeviceCommand groupedDeviceCommand) {
        AddPropertiesCommand addPropertiesCommand = spy(new AddPropertiesCommand(groupedDeviceCommand, TypedProperties.empty(), TypedProperties.empty(), null));
        doThrow(new ConnectionCommunicationException(1)).when(addPropertiesCommand)
                .doExecute(any(DeviceProtocol.class), any(ExecutionContext.class));
        return addPropertiesCommand;
    }

    private ComCommand mockNoneConnectionErrorFailureComCommand(GroupedDeviceCommand groupedDeviceCommand) {
        AddPropertiesCommand addPropertiesCommand = spy(new AddPropertiesCommand(groupedDeviceCommand, TypedProperties.empty(), TypedProperties.empty(), null));
        doThrow(new DataParseException(new IndexOutOfBoundsException("You did not parse it right ..."), MessageSeeds.COULD_NOT_PARSE_REGISTER_DATA)).when(addPropertiesCommand)
                .doExecute(any(DeviceProtocol.class), any(ExecutionContext.class));
        return addPropertiesCommand;
    }

    private ReadRegistersCommandImpl mockReadRegistersCommand() {
        ReadRegistersCommandImpl readRegistersCommand = mock(ReadRegistersCommandImpl.class);
        when(readRegistersCommand.getCommandType()).thenReturn(ComCommandTypes.READ_REGISTERS_COMMAND);
        when(readRegistersCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        return readRegistersCommand;
    }

    private ForceClockCommandImpl mockForceClockCommand() {
        ForceClockCommandImpl forceClockCommand = mock(ForceClockCommandImpl.class);
        when(forceClockCommand.getCommandType()).thenReturn(ComCommandTypes.FORCE_CLOCK_COMMAND);
        when(forceClockCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        return forceClockCommand;
    }

    private AddPropertiesCommand mockAddPropertiesCommand() {
        AddPropertiesCommand addPropertiesCommand = mock(AddPropertiesCommand.class);
        when(addPropertiesCommand.getCommandType()).thenReturn(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        when(addPropertiesCommand.getCompletionCode()).thenReturn(CompletionCode.Ok);
        return addPropertiesCommand;
    }

    @Test
    public void logOffAndDaisyChainedLogOffCantBeCalledAfterCommunicationExceptionsTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        ComCommand addPropertiesCommand = mockConnectionErrorFailureComCommand(groupedDeviceCommand);
        groupedDeviceCommand.addCommand(addPropertiesCommand, comTaskExecution);
        LogOffCommand logOffCommand = getLogOffCommand(groupedDeviceCommand, comTaskExecution);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = getDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);
        verify(daisyChainedLogOffCommand, never()).execute(deviceProtocol, executionContext);
        verify(logOffCommand, never()).execute(deviceProtocol, executionContext);
    }

    @Test
    public void logOffAndDaisyChainedLogOffMustBeCalledAfterANonCommunicationExceptionsTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        ComCommand addPropertiesCommand = mockNoneConnectionErrorFailureComCommand(groupedDeviceCommand);
        groupedDeviceCommand.addCommand(addPropertiesCommand, comTaskExecution);
        LogOffCommand logOffCommand = getLogOffCommand(groupedDeviceCommand, comTaskExecution);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = getDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);

        verify(daisyChainedLogOffCommand).execute(deviceProtocol, executionContext);
        verify(logOffCommand).execute(deviceProtocol, executionContext);
    }


    @Test
    public void initErrorShouldContinueWithOtherDeviceInCommandRoot() {
        OfflineDevice offlineMaster = mock(OfflineDevice.class);
        Long deviceId1 = 1235L;
        Long deviceId2 = 12541L;
        Device device1 = mockDevice(deviceId1);
        Device device2 = mockDevice(deviceId2);
        OfflineDevice offlineDevice1 = mockOfflineDevice(deviceId1);
        OfflineDevice offlineDevice2 = mockOfflineDevice(deviceId2);
        when(offlineMaster.getAllSlaveDevices()).thenReturn(Arrays.asList(offlineDevice1, offlineDevice2));
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(device1);
        ComTaskExecution comTaskExecution2 = mockComTaskExecution(device2);
        DeviceProtocol deviceProtocol1 = mock(DeviceProtocol.class);
        DeviceProtocol deviceProtocol2 = mock(DeviceProtocol.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand1 = createGroupedDeviceCommand(commandRoot, offlineDevice1, deviceProtocol1, null);
        GroupedDeviceCommand groupedDeviceCommand2 = createGroupedDeviceCommand(commandRoot, offlineDevice2, deviceProtocol2, null);
        final ExecutionContext executionContext = groupedDeviceCommand1.getCommandRoot().getExecutionContext();

        LogOnCommand logOnCommand = getLogOnCommand(groupedDeviceCommand1, comTaskExecution1);
        LoadProfileCommand readLoadProfileCommand = getLoadProfileCommand(groupedDeviceCommand1, comTaskExecution1);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = getDaisyChainedLogOffCommand(groupedDeviceCommand1, comTaskExecution1);
        DaisyChainedLogOnCommand daisyChainedLogOnCommand = getDaisyChainedLogOnCommand(groupedDeviceCommand2, comTaskExecution2);
        ReadRegistersCommand readRegistersCommand = getReadRegistersCommand(groupedDeviceCommand2, comTaskExecution2);
        LogOffCommand logOffCommand = getLogOffCommand(groupedDeviceCommand2, comTaskExecution1);
        doThrow(new CommunicationException(MessageSeeds.CIPHERING_EXCEPTION)).when(deviceProtocol1).logOn();

        commandRoot.execute(true);
        verify(logOnCommand, times(1)).execute(deviceProtocol1, executionContext);
        verify(readLoadProfileCommand, never()).execute(deviceProtocol1, executionContext);
        verify(daisyChainedLogOffCommand, never()).execute(deviceProtocol1, executionContext);
        verify(daisyChainedLogOnCommand, times(1)).execute(deviceProtocol2, executionContext);
        verify(readRegistersCommand, times(1)).execute(deviceProtocol2, executionContext);
        verify(logOffCommand, times(1)).execute(deviceProtocol2, executionContext);
    }

    private ComTaskExecution mockComTaskExecution(Device device) {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getDevice()).thenReturn(device);
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        return comTaskExecution;
    }

    private Device mockDevice(Long deviceId) {
        Device mock = mock(Device.class);
        when(mock.getId()).thenReturn(deviceId);
        return mock;
    }

    private OfflineDevice mockOfflineDevice(Long deviceId) {
        OfflineDevice offlineDevice1 = mock(OfflineDevice.class);
        when(offlineDevice1.getMacException()).thenReturn(Optional.empty());
        when(offlineDevice1.getId()).thenReturn(deviceId);
        return offlineDevice1;
    }

    private ReadRegistersCommand getReadRegistersCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        ReadRegistersCommandImpl readRegistersCommand = spy(new ReadRegistersCommandImpl(groupedDeviceCommand, mock(RegisterCommand.class)));
        groupedDeviceCommand.addCommand(readRegistersCommand, comTaskExecution);
        return readRegistersCommand;
    }

    private DaisyChainedLogOnCommand getDaisyChainedLogOnCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        DaisyChainedLogOnCommand daisyChainedLogOnCommand = spy(new DaisyChainedLogOnCommand(groupedDeviceCommand));
        groupedDeviceCommand.addCommand(daisyChainedLogOnCommand, comTaskExecution);
        return daisyChainedLogOnCommand;
    }

    private DaisyChainedLogOffCommand getDaisyChainedLogOffCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = spy(new DaisyChainedLogOffCommand(groupedDeviceCommand));
        groupedDeviceCommand.addCommand(daisyChainedLogOffCommand, comTaskExecution);
        return daisyChainedLogOffCommand;
    }

    private LogOffCommand getLogOffCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        LogOffCommand logOffCommand = spy(new LogOffCommand(groupedDeviceCommand));
        groupedDeviceCommand.addCommand(logOffCommand, comTaskExecution);
        return logOffCommand;
    }

    private LoadProfileCommand getLoadProfileCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        LoadProfileCommand loadProfileCommand = spy(new LoadProfileCommandImpl(groupedDeviceCommand, mock(LoadProfilesTask.class), comTaskExecution));
        groupedDeviceCommand.addCommand(loadProfileCommand, comTaskExecution);
        return loadProfileCommand;
    }

    private LogOnCommand getLogOnCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        LogOnCommand logOnCommand = spy(new LogOnCommand(groupedDeviceCommand));
        groupedDeviceCommand.addCommand(logOnCommand, comTaskExecution);
        return logOnCommand;
    }

    @Test
    public void basicCheckFailureShouldPerformSomeCleanupCommandsTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        final BasicCheckCommandImpl basicCheckCommand = getBasicCheckCommand(groupedDeviceCommand, comTaskExecution);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                basicCheckCommand.setCompletionCode(CompletionCode.TimeError);
                return null;
            }
        }).when(basicCheckCommand).doExecute(deviceProtocol, executionContext);
        ForceClockCommandImpl forceClockCommand = mockForceClockCommand();
        ReadRegistersCommandImpl readRegistersCommand = mockReadRegistersCommand();

        groupedDeviceCommand.addCommand(basicCheckCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(forceClockCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(readRegistersCommand, comTaskExecution);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = getDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);
        LogOffCommand logOffCommand = getLogOffCommand(groupedDeviceCommand, comTaskExecution);
        DeviceProtocolTerminateCommand deviceProtocolTerminateCommand = mockDeviceProtocolTerminateComCommand();
        groupedDeviceCommand.addCommand(deviceProtocolTerminateCommand, comTaskExecution);
        DeviceProtocolUpdateCacheCommand deviceProtocolUpdateCacheCommand = mockDeviceProtocolUpdateCacheCommand();
        groupedDeviceCommand.addCommand(deviceProtocolUpdateCacheCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);

        verify(forceClockCommand, never()).execute(deviceProtocol, executionContext);
        verify(readRegistersCommand, never()).execute(deviceProtocol, executionContext);
        verify(daisyChainedLogOffCommand, times(1)).execute(deviceProtocol, executionContext);
        verify(logOffCommand, times(1)).execute(deviceProtocol, executionContext);
        verify(deviceProtocolTerminateCommand, times(1)).execute(deviceProtocol, executionContext);
        verify(deviceProtocolUpdateCacheCommand, times(1)).execute(deviceProtocol, executionContext);
    }

    @Test
    public void basicCheckFailureShouldNotExecuteOtherCommandsOfThatGroupedDeviceCommandTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(mockOfflineDevice(1L), deviceProtocol);
        final ExecutionContext executionContext = groupedDeviceCommand.getCommandRoot().getExecutionContext();

        final BasicCheckCommandImpl basicCheckCommand = getBasicCheckCommand(groupedDeviceCommand, comTaskExecution);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                basicCheckCommand.setCompletionCode(CompletionCode.TimeError);
                return null;
            }
        }).when(basicCheckCommand).doExecute(deviceProtocol, executionContext);
        ForceClockCommandImpl forceClockCommand = mockForceClockCommand();
        ReadRegistersCommandImpl readRegistersCommand = mockReadRegistersCommand();

        groupedDeviceCommand.addCommand(basicCheckCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(forceClockCommand, comTaskExecution);
        groupedDeviceCommand.addCommand(readRegistersCommand, comTaskExecution);

        groupedDeviceCommand.execute(executionContext);

        verify(forceClockCommand, never()).execute(deviceProtocol, executionContext);
        verify(readRegistersCommand, never()).execute(deviceProtocol, executionContext);
    }

    @Test
    public void basicCheckFailureDoesNotPreventOtherGroupedDeviceCommandsFromExecutionTest() {
        CommandRoot commandRoot = createCommandRoot();
        ExecutionContext executionContext = commandRoot.getExecutionContext();
        DeviceProtocol deviceProtocol1 = mock(DeviceProtocol.class);
        DeviceProtocol deviceProtocol2 = mock(DeviceProtocol.class);
        long deviceId1 = 10L;
        long deviceId2 = 120L;
        OfflineDevice offlineDevice1 = mockOfflineDevice(deviceId1);
        OfflineDevice offlineDevice2 = mockOfflineDevice(deviceId2);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(mockDevice(deviceId1));
        ComTaskExecution comTaskExecution2 = mockComTaskExecution(mockDevice(deviceId2));
        GroupedDeviceCommand groupedDeviceCommand1 = createGroupedDeviceCommand(commandRoot, offlineDevice1, deviceProtocol1, null);
        GroupedDeviceCommand groupedDeviceCommand2 = createGroupedDeviceCommand(commandRoot, offlineDevice2, deviceProtocol2, null);

        final BasicCheckCommandImpl basicCheckCommand1 = getBasicCheckCommand(groupedDeviceCommand1, comTaskExecution1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                basicCheckCommand1.setCompletionCode(CompletionCode.TimeError);
                return null;
            }
        }).when(basicCheckCommand1).doExecute(deviceProtocol1, executionContext);
        ForceClockCommandImpl forceClockCommand = mockForceClockCommand();
        ReadRegistersCommandImpl readRegistersCommand1 = mockReadRegistersCommand();
        groupedDeviceCommand1.addCommand(basicCheckCommand1, comTaskExecution1);
        groupedDeviceCommand1.addCommand(forceClockCommand, comTaskExecution1);
        groupedDeviceCommand1.addCommand(readRegistersCommand1, comTaskExecution1);

        BasicCheckCommandImpl basicCheckCommand2 = getBasicCheckCommand(groupedDeviceCommand2, comTaskExecution2);
        ReadRegistersCommandImpl readRegistersCommand2 = mockReadRegistersCommand();
        groupedDeviceCommand2.addCommand(basicCheckCommand2, comTaskExecution2);
        groupedDeviceCommand2.addCommand(readRegistersCommand2, comTaskExecution2);

        commandRoot.execute(true);

        verify(basicCheckCommand1, times(1)).execute(deviceProtocol1, executionContext);
        verify(forceClockCommand, never()).execute(deviceProtocol1, executionContext);
        verify(readRegistersCommand1, never()).execute(deviceProtocol1, executionContext);

        verify(basicCheckCommand2, times(1)).execute(deviceProtocol2, executionContext);
        verify(readRegistersCommand2, times(1)).execute(deviceProtocol2, executionContext);
    }

    private BasicCheckCommandImpl getBasicCheckCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        return spy(new BasicCheckCommandImpl(mock(BasicCheckTask.class), groupedDeviceCommand, comTaskExecution));
    }
}