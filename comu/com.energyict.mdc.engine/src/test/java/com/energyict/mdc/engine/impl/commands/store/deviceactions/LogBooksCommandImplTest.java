package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineLogBookSpec;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link LogBooksCommandImpl} component
 *
 * @author sva
 * @since 10/12/12 - 16:36
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBooksCommandImplTest extends CommonCommandImplTests {

    private static final long LOGBOOK_ID_1 = 1;
    private static final long LOGBOOK_ID_2 = 2;
    private static final long LOGBOOK_ID_3 = 3;

    private static final ObisCode LOGBOOK1_OBIS = ObisCode.fromString("1.1.0.0.0.255");
    private static final ObisCode LOGBOOK2_OBIS = ObisCode.fromString("1.2.0.0.0.255");
    private static final ObisCode LOGBOOK3_OBIS = ObisCode.fromString("1.3.0.0.0.255");

    private static final long LOGBOOK_TYPE_1 = 10;
    private static final long LOGBOOK_TYPE_2 = 20;
    private static final long LOGBOOK_TYPE_3 = 30;

    private static final Instant LAST_LOGBOOK_1 = Instant.ofEpochMilli(1354320000L * 1000L); // 01 Dec 2012 00:00:00 GMT
    private static final Instant LAST_LOGBOOK_2 = Instant.ofEpochMilli(1351728000L * 1000L); // 01 Nov 2012 00:00:00 GMT
    private static final Instant LAST_LOGBOOK_3 = Instant.ofEpochMilli(1349049600L * 1000L); // 01 Oct 2012 00:00:00 GMT

    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_1 = ObisCode.fromString("1.1.1.1.1.1");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_2 = ObisCode.fromString("2.2.2.2.2.2");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_3 = ObisCode.fromString("3.3.3.3.3.3");

    private static String SERIAL_NUMBER = "SerialNumber";
    private final TestSerialNumberDeviceIdentifier deviceIdentifier = new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER);

    @Mock
    private OfflineLogBook offlineLogBook_A;
    @Mock
    private OfflineLogBook offlineLogBook_B;
    @Mock
    private OfflineLogBook offlineLogBook_C;

    @Mock
    private LogBookType logBookType_A;
    @Mock
    private LogBookType logBookType_B;
    @Mock
    private LogBookType logBookType_C;

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private ComTaskExecution comTaskExecution_B;
    @Mock
    private LogBookService logBookService;
    @Mock
    private Device device;

    @Mock
    private OfflineDevice offlineDevice;

    private Clock clock = Clock.systemUTC();

    @Before
    public void setUp() throws Exception {
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getmRID()).thenReturn("MyMrid");
        when(offlineLogBook_A.getLogBookId()).thenReturn(LOGBOOK_ID_1);
        when(offlineLogBook_A.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS));
        when(offlineLogBook_A.getLastReading()).thenReturn(Date.from(LAST_LOGBOOK_1));
        when(offlineLogBook_A.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        OfflineLogBookSpec offlineLogBookSpec1 = mock(OfflineLogBookSpec.class);
        when(offlineLogBookSpec1.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_1);
        when(offlineLogBookSpec1.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_1);
        when(offlineLogBook_A.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec1);
        when(offlineLogBook_A.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(offlineLogBook_B.getLogBookId()).thenReturn(LOGBOOK_ID_2);
        when(offlineLogBook_B.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_2, LOGBOOK2_OBIS));
        when(offlineLogBook_B.getLastReading()).thenReturn(Date.from(LAST_LOGBOOK_2));
        when(offlineLogBook_B.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        OfflineLogBookSpec offlineLogBookSpec2 = mock(OfflineLogBookSpec.class);
        when(offlineLogBookSpec2.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_2);
        when(offlineLogBookSpec2.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_2);
        when(offlineLogBook_B.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec2);
        when(offlineLogBook_B.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(offlineLogBook_C.getLogBookId()).thenReturn(LOGBOOK_ID_3);
        when(offlineLogBook_C.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS));
        when(offlineLogBook_C.getLastReading()).thenReturn(Date.from(LAST_LOGBOOK_3));
        when(offlineLogBook_C.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        OfflineLogBookSpec offlineLogBookSpec3 = mock(OfflineLogBookSpec.class);
        when(offlineLogBookSpec3.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_3);
        when(offlineLogBookSpec3.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_3);
        when(offlineLogBook_C.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec3);
        when(offlineLogBook_C.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(logBookType_A.getId()).thenReturn(LOGBOOK_TYPE_1);
        when(logBookType_B.getId()).thenReturn(LOGBOOK_TYPE_2);
        when(logBookType_C.getId()).thenReturn(LOGBOOK_TYPE_3);

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(0L);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution_B.getDevice()).thenReturn(device);
    }

    @Test(expected = CodingException.class)
    public void logBooksTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        new LogBooksCommandImpl(groupedDeviceCommand, null, comTaskExecution);
        // should have gotten a CodingException
    }

    @Test(expected = CodingException.class)
    public void DeviceNullTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(null, deviceProtocol);
        new LogBooksCommandImpl(groupedDeviceCommand, logBooksTask, comTaskExecution);
        // should have gotten a CodingException
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        new LogBooksCommandImpl(null, logBooksTask, comTaskExecution);
        // should have gotten a CodingException
    }


    @Test
    public void commandTypeTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        LogBooksCommand logBooksCommand = getLogBooksCommand(logBooksTask, groupedDeviceCommand);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
    }

    private LogBooksCommandImpl getLogBooksCommand(LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand) {
        LogBooksCommandImpl logBooksCommand = spy(new LogBooksCommandImpl(groupedDeviceCommand, logBooksTask, comTaskExecution));
        groupedDeviceCommand.addCommand(logBooksCommand, comTaskExecution);
        return logBooksCommand;
    }

    @Test
    public void createLogBookReadersForEmptyLogBookTaskLogBookTypesTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(new ArrayList<LogBookType>());  // No LogBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        LogBooksCommandImpl logBooksCommand = getLogBooksCommand(logBooksTask, groupedDeviceCommand);
        List<LogBookReader> logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS), deviceIdentifier, SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_2 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_2, Optional.of(LAST_LOGBOOK_2), new LogBookIdentifierById(LOGBOOK_ID_2, LOGBOOK2_OBIS), deviceIdentifier, SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS), deviceIdentifier, SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertEquals(logBooksForDevice.size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_2);
        assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

    @Test
    public void createLogBookReadersForSpecificLogBookTaskLogBookTypesTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDeviceImpl device = mock(OfflineDeviceImpl.class);
        when(device.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        LogBooksCommand logBooksCommand = getLogBooksCommand(logBooksTask, groupedDeviceCommand);
        List<LogBookReader> logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS), deviceIdentifier, SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS), deviceIdentifier, SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertEquals(logBooksTask.getLogBookTypes().size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

    @Test
    public void testUpdateAccordingTo() {
        LogBooksTask logBooksTask_A = mock(LogBooksTask.class);
        when(logBooksTask_A.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        LogBooksCommand logBooksCommand = getLogBooksCommand(logBooksTask_A, groupedDeviceCommand);
        List<LogBookReader> logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, LOGBOOK1_OBIS), deviceIdentifier, SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, LOGBOOK3_OBIS), deviceIdentifier, SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertEquals(logBooksTask_A.getLogBookTypes().size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);

        LogBooksTask logBooksTask_B = mock(LogBooksTask.class);
        when(logBooksTask_B.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_B));
        logBooksCommand.updateAccordingTo(logBooksTask_B, groupedDeviceCommand, comTaskExecution);
        logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_2 = new LogBookReader(clock, DEVICE_OBISCODE_LOGBOOK_2, Optional.of(LAST_LOGBOOK_2), new LogBookIdentifierById(LOGBOOK_ID_2, LOGBOOK2_OBIS), deviceIdentifier, SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertEquals(3, logBookReaders.size());
        // The types of task A (A and C) should be merged with tasks of B (A and B) - There should be no double entries
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
        assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_2);
    }

}