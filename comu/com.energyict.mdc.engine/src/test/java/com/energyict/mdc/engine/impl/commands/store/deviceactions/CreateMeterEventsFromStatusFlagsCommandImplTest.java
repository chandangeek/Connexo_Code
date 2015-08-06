package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.LoadProfilesTask;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(this.executionContextServiceProvider.clock()).thenReturn(clock);
        when(this.executionContextServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.executionContextServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.jobExecutionServiceProvider.clock()).thenReturn(clock);
        when(this.jobExecutionServiceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.jobExecutionServiceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.jobExecutionServiceProvider.identificationService()).thenReturn(this.identificationService);
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
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.getOfflineDevice()).thenReturn(device);
        when(device.getId()).thenReturn(DEVICE_ID);
    }

    private void initializeDeviceLoadProfileWith(int intervalStateBit) {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(Date.from(getFrozenClock().instant()), intervalStateBit));
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
        when(this.meteringService.getEndDeviceEventType(EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID())).thenReturn(Optional.of(otherEndDeviceEventType));
    }

    @Test
    public void testDoExecute() throws Exception {
        initializeEndDeviceEventTypeFactory();
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(loadProfileCommand, commandRoot);
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
        initializeDeviceLoadProfileWith(IntervalStateBits.CONFIGURATIONCHANGE);
        command.doExecute(deviceProtocol, executionContext);

        ArgumentCaptor<DeviceLogBook> argument = ArgumentCaptor.forClass(DeviceLogBook.class);
        verify(loadProfileCommand, atLeastOnce()).addCollectedDataItem(argument.capture());

        DeviceLogBook logBook = argument.getValue();

        verifyIntervalStateBitsWithMeterEvent(logBook, MeterEvent.CONFIGURATIONCHANGE);
    }

    private void verifyPowerDown(CreateMeterEventsFromStatusFlagsCommandImpl command, ExecutionContext executionContext) {
        initializeDeviceLoadProfileWith(IntervalStateBits.POWERDOWN);
        command.doExecute(deviceProtocol, executionContext);

        ArgumentCaptor<DeviceLogBook> argument = ArgumentCaptor.forClass(DeviceLogBook.class);
        verify(loadProfileCommand, atLeastOnce()).addCollectedDataItem(argument.capture());

        DeviceLogBook logBook = argument.getValue();

        verifyIntervalStateBitsWithMeterEvent(logBook, MeterEvent.POWERDOWN);
    }

    private void verifyDeviceError(CreateMeterEventsFromStatusFlagsCommandImpl command, ExecutionContext executionContext) {
        initializeDeviceLoadProfileWith(IntervalStateBits.DEVICE_ERROR);
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
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(meterEvent, this.meteringService).orElseThrow(() -> new RuntimeException("event type for " + meterEvent + " was not setup correctly in this unit test")),
                        null,
                        0,
                        0));

        assertThat(compareMeterEventList(expectedEventList, logBook.getCollectedMeterEvents())).isTrue();
    }

    @Test
    public void testToJournalMessageDescriptionWithErrorLevel() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(loadProfileCommand, commandRoot);

        // Business method
        String description = command.toJournalMessageDescription(LogLevel.INFO);

        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok}");
    }

    @Test
    public void testToJournalMessageDescriptionWithInfoLevel() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(loadProfileCommand, commandRoot);

        // Business method
        String description = command.toJournalMessageDescription(LogLevel.INFO);

        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok}");
    }

    @Test
    public void testToJournalMessageDescriptionWithTraceLevel() throws Exception {
        CreateMeterEventsFromStatusFlagsCommandImpl command = new CreateMeterEventsFromStatusFlagsCommandImpl(loadProfileCommand, commandRoot);

        // Business method
        String description = command.toJournalMessageDescription(LogLevel.TRACE);

        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; Load profile obisCodes: 1.1.1.1.1.1; meterEvents: }");
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
        protected boolean isConnected() {
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
        public void outsideComWindow() {
        }

        @Override
        public void rescheduleToNextComWindow(ComServerDAO comServerDAO) {
        }

        @Override
        public void execute() {
            // No actual execution in mock mode
        }
    }

}