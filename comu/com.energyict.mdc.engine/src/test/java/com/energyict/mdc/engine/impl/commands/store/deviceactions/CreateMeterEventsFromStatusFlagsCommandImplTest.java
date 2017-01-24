package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfilesTaskOptions;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolReadingQualities;
import com.energyict.protocol.exceptions.ConnectionException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 17/01/13 - 16:23
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateMeterEventsFromStatusFlagsCommandImplTest {

    private static final Long DEVICE_ID = 1L;

    @Mock
    private LoadProfileCommand loadProfileCommand;
    @Mock
    private CommandRoot commandRoot;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private Logger logger;
    @Mock
    private OfflineDevice device;
    @Mock
    private DeviceLoadProfile deviceLoadProfile;
    @Mock
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

    private GroupedDeviceCommand groupedDeviceCommand;
    @Mock
    private IssueService issueService;
    @Mock
    private DeviceService deviceService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionTaskService connectionTaskService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IdentificationService identificationService;
    @Mock
    private ExecutionContext.ServiceProvider executionContextServiceProvider;
    @Mock
    private JobExecution.ServiceProvider jobExecutionServiceProvider;
    @Mock
    private CommandRoot.ServiceProvider commandRootServiceProvider;
    @Mock
    private MeteringService meteringService;

    private Clock clock = Clock.systemUTC();
    private Clock frozenClock;

    @Before
    public void setup() {
        when(deviceLoadProfile.getLoadProfileIdentifier()).thenReturn(new LoadProfileIdentifierByObisCodeAndDevice(ObisCode.fromString("0.0.99.98.0.255"), null));
        when(this.executionContextServiceProvider.clock()).thenReturn(clock);
        when(this.executionContextServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.executionContextServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.jobExecutionServiceProvider.clock()).thenReturn(clock);
        when(this.jobExecutionServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.jobExecutionServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.jobExecutionServiceProvider.identificationService()).thenReturn(this.identificationService);
        when(this.jobExecutionServiceProvider.eventPublisher()).thenReturn(mock(EventPublisher.class));
        when(this.commandRootServiceProvider.clock()).thenReturn(clock);
        when(this.commandRootServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.commandRootServiceProvider.identificationService()).thenReturn(this.identificationService);
        when(this.commandRootServiceProvider.meteringService()).thenReturn(this.meteringService);
        when(this.commandRoot.getServiceProvider()).thenReturn(this.commandRootServiceProvider);
        List<CollectedData> collectedDataList = new ArrayList<>();
        collectedDataList.add(deviceLoadProfile);
        when(loadProfileCommand.getCollectedData()).thenReturn(collectedDataList);
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.getOfflineDevice()).thenReturn(device);
        when(device.getId()).thenReturn(DEVICE_ID);
        groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, device, deviceProtocol, deviceProtocolSecurityPropertySet);
    }

    private void initializeDeviceLoadProfileWith(ProtocolReadingQualities readingQualityType) {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(Date.from(getFrozenClock().instant()), new HashSet<String>(Arrays.asList(readingQualityType.getCimCode()))));
        when(deviceLoadProfile.getCollectedIntervalData()).thenReturn(intervalDatas);
    }

    private void initializeEndDeviceEventTypeFactory() {
        EndDeviceEventType hardwareError = mock(EndDeviceEventType.class);
        String hardwareErrorMRID = "0.0.0.79";
        when(hardwareError.getMRID()).thenReturn(hardwareErrorMRID);
        EndDeviceEventType powerDown = mock(EndDeviceEventType.class);
        String powerDownEventMRID = "0.26.38.47";
        when(powerDown.getMRID()).thenReturn(powerDownEventMRID);
        EndDeviceEventType configurationChange = mock(EndDeviceEventType.class);
        String configurationChangeEventMRID = "0.7.31.13";
        when(configurationChange.getMRID()).thenReturn(configurationChangeEventMRID);
        Optional<EndDeviceEventType> hardwareErrorOptional = Optional.of(hardwareError);
        when(this.meteringService.getEndDeviceEventType(hardwareErrorMRID)).thenReturn(hardwareErrorOptional);
        Optional<EndDeviceEventType> powerDownEventOptional = Optional.of(powerDown);
        when(this.meteringService.getEndDeviceEventType(powerDownEventMRID)).thenReturn(powerDownEventOptional);
        Optional<EndDeviceEventType> configurationChangeEventOptional = Optional.of(configurationChange);
        when(this.meteringService.getEndDeviceEventType(configurationChangeEventMRID)).thenReturn(configurationChangeEventOptional);
        EndDeviceEventType otherEndDeviceEventType = mock(EndDeviceEventType.class, Mockito.RETURNS_DEEP_STUBS);
        when(this.meteringService.getEndDeviceEventType(EndDeviceEventTypeMapping.OTHER.getEventType().getCode())).thenReturn(Optional.of(otherEndDeviceEventType));
    }

    @Test
    public void testDoExecute() throws Exception {
        initializeEndDeviceEventTypeFactory();
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(groupedDeviceCommand, loadProfileCommand);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(mock(ComPortPool.class));
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        ExecutionContext executionContext = new ExecutionContext(new MockJobExecution(), connectionTask, comPort, true, this.executionContextServiceProvider);

        verifyDeviceError(command, executionContext);
        verifyPowerDown(command, executionContext);
        verifyConfigurationChange(command, executionContext);
    }

    private void verifyConfigurationChange(CreateMeterEventsFromStatusFlagsCommandImpl command, ExecutionContext executionContext) {
        initializeDeviceLoadProfileWith(ProtocolReadingQualities.CONFIGURATIONCHANGE);
        command.doExecute(deviceProtocol, executionContext);

        ArgumentCaptor<DeviceLogBook> argument = ArgumentCaptor.forClass(DeviceLogBook.class);
        verify(loadProfileCommand, atLeastOnce()).addCollectedDataItem(argument.capture());

        DeviceLogBook logBook = argument.getValue();

        verifyIntervalStateBitsWithMeterEvent(logBook, MeterEvent.CONFIGURATIONCHANGE);
    }

    private void verifyPowerDown(CreateMeterEventsFromStatusFlagsCommandImpl command, ExecutionContext executionContext) {
        initializeDeviceLoadProfileWith(ProtocolReadingQualities.POWERDOWN);
        command.doExecute(deviceProtocol, executionContext);

        ArgumentCaptor<DeviceLogBook> argument = ArgumentCaptor.forClass(DeviceLogBook.class);
        verify(loadProfileCommand, atLeastOnce()).addCollectedDataItem(argument.capture());

        DeviceLogBook logBook = argument.getValue();

        verifyIntervalStateBitsWithMeterEvent(logBook, MeterEvent.POWERDOWN);
    }

    private void verifyDeviceError(CreateMeterEventsFromStatusFlagsCommandImpl command, ExecutionContext executionContext) {
        initializeDeviceLoadProfileWith(ProtocolReadingQualities.DEVICE_ERROR);
        command.doExecute(deviceProtocol, executionContext);

        ArgumentCaptor<DeviceLogBook> argument = ArgumentCaptor.forClass(DeviceLogBook.class);
        verify(loadProfileCommand, atLeastOnce()).addCollectedDataItem(argument.capture());

        DeviceLogBook logBook = argument.getValue();

        verifyIntervalStateBitsWithMeterEvent(logBook, MeterEvent.HARDWARE_ERROR);
    }

    private void verifyIntervalStateBitsWithMeterEvent(DeviceLogBook logBook, int meterEvent) {
        List<MeterProtocolEvent> expectedEventList = new ArrayList<>();
        Date time = new Date(getFrozenClock().millis() - DateTimeConstants.MILLIS_PER_SECOND * 30);    // 30 seconds before the end of the interval
        expectedEventList.add(
                new MeterProtocolEvent(
                        time,
                        meterEvent,
                        0,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(meterEvent),
                        null,
                        0,
                        0));

        assertThat(compareMeterEventList(expectedEventList, logBook.getCollectedMeterEvents())).isTrue();
    }

    @Test
    public void testToJournalMessageDescriptionWithErrorLevel() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(groupedDeviceCommand, loadProfileCommand);

        // Business method
        String description = command.toJournalMessageDescription(LogLevel.INFO);

        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok}");
    }

    @Test
    public void testToJournalMessageDescriptionWhenNoEventsCreated() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(groupedDeviceCommand, loadProfileCommand);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(mock(ComPortPool.class));
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        ExecutionContext executionContext = new ExecutionContext(new MockJobExecution(), connectionTask, comPort, false, jobExecutionServiceProvider);

        command.execute(deviceProtocol, executionContext);
        String journalMessage = command.toJournalMessageDescription(LogLevel.INFO);
        assertEquals(ComCommandDescriptionTitle.CreateMeterEventsFromStatusFlagsCommandImpl.getDescription() + " {No events created from profile load profile having OBIS code 0.0.99.98.0.255 on device with deviceIdentifier 'null'}", journalMessage);
    }

    @Test
    public void testToJournalMessageDescriptionWhenEventsCreated() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(groupedDeviceCommand, loadProfileCommand);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(mock(ComPortPool.class));
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        ExecutionContext executionContext = new ExecutionContext(new MockJobExecution(), connectionTask, comPort, false, jobExecutionServiceProvider);

        initializeDeviceLoadProfileWith(ProtocolReadingQualities.DEVICE_ERROR);
        command.execute(deviceProtocol, executionContext);
        String journalMessage = command.toJournalMessageDescription(LogLevel.INFO);
        assertEquals(ComCommandDescriptionTitle.CreateMeterEventsFromStatusFlagsCommandImpl.getDescription() + " {Created 1 event(s) from profile load profile having OBIS code 0.0.99.98.0.255 on device with deviceIdentifier 'null'}", journalMessage);
    }

    @Test
    public void testToJournalMessageDescriptionWithInfoLevel() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(groupedDeviceCommand, loadProfileCommand);

        // Business method
        String description = command.toJournalMessageDescription(LogLevel.INFO);

        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok}");
    }

    @Test
    public void testToJournalMessageDescriptionWithTraceLevel() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(groupedDeviceCommand, loadProfileCommand);

        // Business method
        String description = command.toJournalMessageDescription(LogLevel.TRACE);

        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok}");
    }

    private Clock getFrozenClock() {
        if (frozenClock == null) {
            frozenClock = Clock.fixed(new DateTime(2012, 1, 1, 12, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        }
        return frozenClock;
    }

    private boolean compareMeterEventList(List<MeterProtocolEvent> expectedEvents, List<MeterProtocolEvent> collectedMeterEvents) {
        if (expectedEvents.size() != collectedMeterEvents.size()) {
            return false;
        }

        for (int i = 0; i < expectedEvents.size(); i++) {
            MeterProtocolEvent expected = expectedEvents.get(i);
            MeterProtocolEvent actual = collectedMeterEvents.get(i);

            if (!expected.getTime().equals(actual.getTime())) {
                return false;
            }
            if (expected.getEiCode() != actual.getEiCode()) {
                return false;
            }
            if (expected.getProtocolCode() != actual.getProtocolCode()) {
                return false;
            }
            if (!expected.getEventType().equals(actual.getEventType())) {
                return false;
            }
            if (expected.getMessage() == null && actual.getMessage() != null) {
                return false;
            } else if (actual.getMessage() == null && expected.getMessage() != null) {
                return false;
            } else if ((expected.getMessage() != null) && (!expected.getMessage().equals(actual.getMessage()))) {
                return false;
            }
            if (expected.getEventLogId() != actual.getEventLogId()) {
                return false;
            }
            if (expected.getDeviceEventId() != actual.getDeviceEventId()) {
                return false;
            }
        }
        return true;
    }

    private class MockJobExecution extends JobExecution {

        private MockJobExecution() {
            super(mock(ComPort.class), mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), jobExecutionServiceProvider);
        }

        @Override
        protected ComPortRelatedComChannel findOrCreateComChannel(ConnectionTaskPropertyProvider connectionTaskPropertyProvider) throws ConnectionException {
            return null;
        }

        @Override
        public List<ComTaskExecution> getComTaskExecutions() {
            return new ArrayList<>(0);
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public List<ComTaskExecution> getNotExecutedComTaskExecutions() {
            return new ArrayList<>(0);
        }

        @Override
        public List<ComTaskExecution> getFailedComTaskExecutions() {
            return new ArrayList<>(0);
        }

        @Override
        public List<ComTaskExecution> getSuccessfulComTaskExecutions() {
            return new ArrayList<>(0);
        }

        @Override
        public ConnectionTask getConnectionTask() {
            return null;
        }

        @Override
        public boolean attemptLock() {
            return true;
        }

        @Override
        public void unlock() {
            // No unlocking in mock mode as we haven't done any locking either
        }

        @Override
        public boolean isStillPending() {
            return true;
        }

        @Override
        public boolean isWithinComWindow() {
            return true;
        }

        @Override
        public void execute() {
            // No actual execution in mock mode
        }

        @Override
        public void rescheduleToNextComWindow() {

        }
    }
}