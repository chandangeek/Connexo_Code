package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.identifiers.LogBookIdentifierByIdImpl;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    private static final Date LAST_LOGBOOK_1 = new Date(1354320000L * 1000L); // 01 Dec 2012 00:00:00 GMT
    private static final Date LAST_LOGBOOK_2 = new Date(1351728000L * 1000L); // 01 Nov 2012 00:00:00 GMT
    private static final Date LAST_LOGBOOK_3 = new Date(1349049600L * 1000L); // 01 Oct 2012 00:00:00 GMT

    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_1 = ObisCode.fromString("1.1.1.1.1.1");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_2 = ObisCode.fromString("2.2.2.2.2.2");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_3 = ObisCode.fromString("3.3.3.3.3.3");

    private static String SERIAL_NUMBER = "SerialNumber";

//    @ClassRule
//    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

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
    private DeviceDataService deviceDataService;

    @Before
    public void setUp() throws Exception {
        when(offlineLogBook_A.getLogBookId()).thenReturn(LOGBOOK_ID_1);
        when(offlineLogBook_A.getLastLogBook()).thenReturn(LAST_LOGBOOK_1);
        when(offlineLogBook_A.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_A.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_1);
        when(offlineLogBook_A.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_1);

        when(offlineLogBook_B.getLogBookId()).thenReturn(LOGBOOK_ID_2);
        when(offlineLogBook_B.getLastLogBook()).thenReturn(LAST_LOGBOOK_2);
        when(offlineLogBook_B.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_B.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_2);
        when(offlineLogBook_B.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_2);

        when(offlineLogBook_C.getLogBookId()).thenReturn(LOGBOOK_ID_3);
        when(offlineLogBook_C.getLastLogBook()).thenReturn(LAST_LOGBOOK_3);
        when(offlineLogBook_C.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_C.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_3);
        when(offlineLogBook_C.getObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_3);

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
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertNotNull(logBooksCommand.getLogBooksTask());
    }

    @Test
    public void createLogBookReadersForEmptyLogBookTaskLogBookTypesTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(new ArrayList<LogBookType>());  // No LogBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);

        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);

        LogBooksCommandImpl logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution);
        List<LogBookReader> logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_1, LAST_LOGBOOK_1, new LogBookIdentifierByIdImpl(LOGBOOK_ID_1, deviceDataService), SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_2 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_2, LAST_LOGBOOK_2, new LogBookIdentifierByIdImpl(LOGBOOK_ID_2, deviceDataService), SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_3, LAST_LOGBOOK_3, new LogBookIdentifierByIdImpl(LOGBOOK_ID_3, deviceDataService), SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertNotNull(logBooksCommand.getLogBooksTask());
        assertEquals(logBooksForDevice.size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_2);
        assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_3);

        assertEquals("LogBooksCommandImpl {logBookObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2, 3.3.3.3.3.3}", logBooksCommand.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void createLogBookReadersForSpecificLogBookTaskLogBookTypesTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        when(logBooksTask.getLogBookTypes()).thenReturn(Arrays.asList(logBookType_A, logBookType_C));    // The logBookTypes are specified in the LogBooksTask

        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        List<OfflineLogBook> logBooksForDevice = Arrays.asList(offlineLogBook_A, offlineLogBook_B, offlineLogBook_C);
        when(device.getAllOfflineLogBooks()).thenReturn(logBooksForDevice);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);

        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);
        CommandRoot.ServiceProvider serviceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRoot.getServiceProvider()).thenReturn(serviceProvider);

        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution);
        List<LogBookReader> logBookReaders = ((LogBooksCommandImpl) logBooksCommand).getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_1, LAST_LOGBOOK_1, new LogBookIdentifierByIdImpl(LOGBOOK_ID_1, deviceDataService), SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_3, LAST_LOGBOOK_3, new LogBookIdentifierByIdImpl(LOGBOOK_ID_3, deviceDataService), SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertNotNull(logBooksCommand.getLogBooksTask());
        assertEquals(logBooksTask.getLogBookTypes().size(), logBookReaders.size());
        assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

}