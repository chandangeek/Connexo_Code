package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.tasks.LogBooksTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link LogBooksCommandImpl} component
 *
 * @author sva
 * @since 10/12/12 - 16:36
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBooksCommandImplTest {

    private static final long LOGBOOK_ID_1 = 1;
    private static final long LOGBOOK_ID_2 = 2;
    private static final long LOGBOOK_ID_3 = 3;

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
    private LogBookService logBookService;
    @Mock
    private Device device;

    private Clock clock = Clock.systemUTC();

    @Before
    public void setUp() throws Exception {
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getmRID()).thenReturn("MyMrid");
        when(offlineLogBook_A.getLogBookId()).thenReturn(LOGBOOK_ID_1);
        when(offlineLogBook_A.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_1, logBookService));
        when(offlineLogBook_A.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK_1));
        when(offlineLogBook_A.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_A.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_1);
        when(offlineLogBook_A.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_1);
        when(offlineLogBook_A.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(offlineLogBook_B.getLogBookId()).thenReturn(LOGBOOK_ID_2);
        when(offlineLogBook_B.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_2, logBookService));
        when(offlineLogBook_B.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK_2));
        when(offlineLogBook_B.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_B.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_2);
        when(offlineLogBook_B.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_2);
        when(offlineLogBook_B.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(offlineLogBook_C.getLogBookId()).thenReturn(LOGBOOK_ID_3);
        when(offlineLogBook_C.getLogBookIdentifier()).thenReturn(new LogBookIdentifierById(LOGBOOK_ID_3, logBookService));
        when(offlineLogBook_C.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK_3));
        when(offlineLogBook_C.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_C.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_3);
        when(offlineLogBook_C.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_3);
        when(offlineLogBook_C.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        when(logBookType_A.getId()).thenReturn(LOGBOOK_TYPE_1);
        when(logBookType_B.getId()).thenReturn(LOGBOOK_TYPE_2);
        when(logBookType_C.getId()).thenReturn(LOGBOOK_TYPE_3);
    }

    @Test(expected = CodingException.class)
    public void logBooksTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        new LogBooksCommandImpl(null, device, commandRoot, comTaskExecution);
        // should have gotten a CodingException
    }

    @Test(expected = CodingException.class)
    public void DeviceNullTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        new LogBooksCommandImpl(logBooksTask, null, commandRoot, comTaskExecution);
        // should have gotten a CodingException
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        new LogBooksCommandImpl(logBooksTask, device, null, comTaskExecution);
        // should have gotten a CodingException
    }


    @Test
    public void commandTypeTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);

        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution);

        // asserts
        assertThat(logBooksCommand.getCommandType()).isEqualTo(ComCommandTypes.LOGBOOKS_COMMAND);
        assertThat(logBooksCommand.getLogBooksTask()).isNotNull();
    }

    @Test
    public void createLogBookReadersForEmptyLogBookTaskLogBookTypesTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(new ArrayList<LogBookType>());  // No LogBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooksForMRID(any())).thenReturn(logBooksForDevice);

        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);

        LogBooksCommandImpl logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution);
        List<LogBookReader> logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, logBookService), deviceIdentifier);
        LogBookReader expectedLogBookReader_2 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_2, Optional.of(LAST_LOGBOOK_2), new LogBookIdentifierById(LOGBOOK_ID_2, logBookService), deviceIdentifier);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, logBookService), deviceIdentifier);

        // asserts
        assertThat(logBooksCommand.getCommandType()).isEqualTo(ComCommandTypes.LOGBOOKS_COMMAND);
        assertThat(logBooksCommand.getLogBooksTask()).isNotNull();
        assertThat(logBookReaders.size()).isEqualTo(logBooksForDevice.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_2);
        assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_3);

        assertThat(logBooksCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{logBookObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2, 3.3.3.3.3.3}");
    }

    @Test
    public void createLogBookReadersForSpecificLogBookTaskLogBookTypesTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooksForMRID(any())).thenReturn(logBooksForDevice);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);

        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);

        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution);
        List<LogBookReader> logBookReaders = ((LogBooksCommandImpl) logBooksCommand).getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_1, Optional.of(LAST_LOGBOOK_1), new LogBookIdentifierById(LOGBOOK_ID_1, logBookService), deviceIdentifier);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(this.clock, DEVICE_OBISCODE_LOGBOOK_3, Optional.of(LAST_LOGBOOK_3), new LogBookIdentifierById(LOGBOOK_ID_3, logBookService), deviceIdentifier);

        // asserts
        assertThat(logBooksCommand.getCommandType()).isEqualTo(ComCommandTypes.LOGBOOKS_COMMAND);
        assertThat(logBooksCommand.getLogBooksTask()).isNotNull();
        assertThat(logBookReaders.size()).isEqualTo(logBooksTask.getLogBookTypes().size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

}