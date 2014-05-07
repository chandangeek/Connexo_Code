package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.LogBooksCommand;
import com.energyict.mdc.commands.ReadLogBooksCommand;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifierByIdImpl;
import com.energyict.mdc.protocol.tasks.LogBooksTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBookSpec;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.test.MockEnvironmentTranslations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
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

    private static final int LOGBOOK_ID_1 = 1;
    private static final int LOGBOOK_ID_2 = 2;
    private static final int LOGBOOK_ID_3 = 3;

    private static final int LOGBOOK_TYPE_1 = 10;
    private static final int LOGBOOK_TYPE_2 = 20;
    private static final int LOGBOOK_TYPE_3 = 30;

    private static final Date LAST_LOGBOOK_1 = new Date(1354320000L * 1000L); // 01 Dec 2012 00:00:00 GMT
    private static final Date LAST_LOGBOOK_2 = new Date(1351728000L * 1000L); // 01 Nov 2012 00:00:00 GMT
    private static final Date LAST_LOGBOOK_3 = new Date(1349049600L * 1000L); // 01 Oct 2012 00:00:00 GMT

    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_1 = ObisCode.fromString("1.1.1.1.1.1");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_2 = ObisCode.fromString("2.2.2.2.2.2");
    private static final ObisCode DEVICE_OBISCODE_LOGBOOK_3 = ObisCode.fromString("3.3.3.3.3.3");

    private static String SERIAL_NUMBER = "SerialNumber";

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private OfflineLogBook offlineLogBook_A;
    @Mock
    private OfflineLogBook offlineLogBook_B;
    @Mock
    private OfflineLogBook offlineLogBook_C;

    @Mock
    private OfflineLogBookSpec offlineLogBookSpec_A;
    @Mock
    private OfflineLogBookSpec offlineLogBookSpec_B;
    @Mock
    private OfflineLogBookSpec offlineLogBookSpec_C;

    @Mock
    private LogBookType logBookType_A;
    @Mock
    private LogBookType logBookType_B;
    @Mock
    private LogBookType logBookType_C;

    @Mock
    private ComTaskExecution comTaskExecution;

    @Before
    public void setUp() throws Exception {
        when(offlineLogBookSpec_A.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_1);
        when(offlineLogBookSpec_A.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_1);
        when(offlineLogBook_A.getLogBookId()).thenReturn(LOGBOOK_ID_1);
        when(offlineLogBook_A.getLastLogBook()).thenReturn(LAST_LOGBOOK_1);
        when(offlineLogBook_A.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_A.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec_A);

        when(offlineLogBookSpec_B.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_2);
        when(offlineLogBookSpec_B.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_2);
        when(offlineLogBook_B.getLogBookId()).thenReturn(LOGBOOK_ID_2);
        when(offlineLogBook_B.getLastLogBook()).thenReturn(LAST_LOGBOOK_2);
        when(offlineLogBook_B.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_B.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec_B);

        when(offlineLogBookSpec_C.getLogBookTypeId()).thenReturn(LOGBOOK_TYPE_3);
        when(offlineLogBookSpec_C.getDeviceObisCode()).thenReturn(DEVICE_OBISCODE_LOGBOOK_3);
        when(offlineLogBook_C.getLogBookId()).thenReturn(LOGBOOK_ID_3);
        when(offlineLogBook_C.getLastLogBook()).thenReturn(LAST_LOGBOOK_3);
        when(offlineLogBook_C.getMasterSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineLogBook_C.getOfflineLogBookSpec()).thenReturn(offlineLogBookSpec_C);

        when(logBookType_A.getId()).thenReturn(LOGBOOK_TYPE_1);
        when(logBookType_B.getId()).thenReturn(LOGBOOK_TYPE_2);
        when(logBookType_C.getId()).thenReturn(LOGBOOK_TYPE_3);
    }

    @Test(expected = CodingException.class)
    public void logBooksTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        new LogBooksCommandImpl(null, device, commandRoot, comTaskExecution, deviceDataService);
        // should have gotten a CodingException
    }

    @Test(expected = CodingException.class)
    public void DeviceNullTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        new LogBooksCommandImpl(logBooksTask, null, commandRoot, comTaskExecution, deviceDataService);
        // should have gotten a CodingException
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        new LogBooksCommandImpl(logBooksTask, device, null, comTaskExecution, deviceDataService);
        // should have gotten a CodingException
    }


    @Test
    public void commandTypeTest() {
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);

        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution, deviceDataService);

        // asserts
        Assert.assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
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
        ReadLogBooksCommand readLogBooksCommand = mock(ReadLogBooksCommand.class);
        when(commandRoot.getReadLogBooksCommand(any(LogBooksCommand.class), any(ComTaskExecution.class))).thenReturn(readLogBooksCommand);

        LogBooksCommandImpl logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution, deviceDataService);
        List<LogBookReader> logBookReaders = logBooksCommand.getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_1, LAST_LOGBOOK_1, new LogBookIdentifierByIdImpl(LOGBOOK_ID_1), SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_2 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_2, LAST_LOGBOOK_2, new LogBookIdentifierByIdImpl(LOGBOOK_ID_2), SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_3, LAST_LOGBOOK_3, new LogBookIdentifierByIdImpl(LOGBOOK_ID_3), SERIAL_NUMBER);

        // asserts
        assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertNotNull(logBooksCommand.getLogBooksTask());
        assertEquals(logBooksForDevice.size(), logBookReaders.size());
        Assertions.assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        Assertions.assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_2);
        Assertions.assertThat(logBookReaders.get(2)).isEqualsToByComparingFields(expectedLogBookReader_3);

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

        LogBooksCommand logBooksCommand = new LogBooksCommandImpl(logBooksTask, device, commandRoot, comTaskExecution, deviceDataService);
        List<LogBookReader> logBookReaders = ((LogBooksCommandImpl) logBooksCommand).getLogBookReaders();

        LogBookReader expectedLogBookReader_1 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_1, LAST_LOGBOOK_1, new LogBookIdentifierByIdImpl(LOGBOOK_ID_1), SERIAL_NUMBER);
        LogBookReader expectedLogBookReader_3 = new LogBookReader(DEVICE_OBISCODE_LOGBOOK_3, LAST_LOGBOOK_3, new LogBookIdentifierByIdImpl(LOGBOOK_ID_3), SERIAL_NUMBER);

        // asserts
        Assert.assertEquals(ComCommandTypes.LOGBOOKS_COMMAND, logBooksCommand.getCommandType());
        assertNotNull(logBooksCommand.getLogBooksTask());
        Assert.assertEquals(logBooksTask.getLogBookTypes().size(), logBookReaders.size());
        Assertions.assertThat(logBookReaders.get(0)).isEqualsToByComparingFields(expectedLogBookReader_1);
        Assertions.assertThat(logBookReaders.get(1)).isEqualsToByComparingFields(expectedLogBookReader_3);
    }

}