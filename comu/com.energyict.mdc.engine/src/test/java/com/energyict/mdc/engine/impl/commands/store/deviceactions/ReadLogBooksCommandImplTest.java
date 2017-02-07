/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LogBooksTask;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ReadLogBooksCommandImpl component
 *
 * @author sva
 * @since 12/12/12 - 13:48
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadLogBooksCommandImplTest extends AbstractComCommandExecuteTest {

    @Mock
    private ComTaskExecution comTaskExecution;

    private Clock clock = Clock.systemUTC();

    @Test
    public void commandTypeTest() {
        ReadLogBooksCommand readLogBooksCommand = new ReadLogBooksCommandImpl(getGroupedDeviceCommand(), mock(LogBooksCommand.class));

        // asserts
        assertEquals(ComCommandTypes.READ_LOGBOOKS_COMMAND, readLogBooksCommand.getCommandType());
    }

    @Test
    public void testExecuteCommand() throws Exception {
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("MyMrid");
        when(comTaskExecution.getDevice()).thenReturn(device);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        ExecutionContext executionContext = newTestExecutionContext();
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        LogBooksCommand logBooksCommand = groupedDeviceCommand.getLogBooksCommand(logBooksTask, groupedDeviceCommand, comTaskExecution);
        ReadLogBooksCommand readLogBooksCommand = groupedDeviceCommand.getReadLogBooksCommand(logBooksCommand, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class);
        when(deviceProtocol.getLogBookData(Matchers.<List<LogBookReader>>any())).thenReturn(Arrays.asList(collectedLogBook));

        readLogBooksCommand.execute(deviceProtocol, executionContext);
        String journalMessage = readLogBooksCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // asserts
        assertNotNull(logBooksCommand.getCollectedData());
        assertEquals("Should have one collectedLogBook", 1, logBooksCommand.getCollectedData().size());
        assertNotNull(readLogBooksCommand.getIssues());
        assertEquals("Should have no issues", 0, logBooksCommand.getIssues().size());
        assertEquals("Should have no problems", 0, logBooksCommand.getProblems().size());
        assertEquals("Should have no warnings", 0, logBooksCommand.getWarnings().size());
        assertEquals(ComCommandDescriptionTitle.ReadLogBooksCommandImpl.getDescription() + " {No log books to read}", journalMessage);
    }

    @Test
    public void testAddOnlyUniqueLogBooks() {
        final ObisCode logBookObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        final ObisCode logBookObisCode2 = ObisCode.fromString("1.0.1.8.2.255");
        final ObisCode logBookObisCode3 = ObisCode.fromString("1.0.1.8.3.255");

        final String SERIAL_NUMBER = "SerialNumber";

        final Instant lastLogBookDate1 = Instant.ofEpochMilli(1354320000L * 1000L); // 01 Dec 2012 00:00:00 GMT
        final Instant lastLogBookDate2 = Instant.ofEpochMilli(1351728000L * 1000L); // 01 Nov 2012 00:00:00 GMT
        final Instant lastLogBookDate3 = Instant.ofEpochMilli(1349049600L * 1000L); // 01 Oct 2012 00:00:00 GMT

        LogBookIdentifier logBookIdentifier1 = mock(LogBookIdentifier.class);
        BaseLogBook logBook1 = mock(BaseLogBook.class);
        when((logBookIdentifier1).getLogBook()).thenReturn(logBook1);
        when((logBookIdentifier1).getLogBookObisCode()).thenReturn(logBookObisCode1);
        when(logBook1.getId()).thenReturn(10L);
        LogBookIdentifier logBookIdentifier2 = mock(LogBookIdentifier.class);
        BaseLogBook logBook2 = mock(BaseLogBook.class);
        when((logBookIdentifier2).getLogBook()).thenReturn(logBook2);
        when((logBookIdentifier2).getLogBookObisCode()).thenReturn(logBookObisCode2);
        when(logBook2.getId()).thenReturn(20L);
        LogBookIdentifier logBookIdentifier3 = mock(LogBookIdentifier.class);
        BaseLogBook logBook3 = mock(BaseLogBook.class);
        when((logBookIdentifier3).getLogBook()).thenReturn(logBook3);
        when((logBookIdentifier3).getLogBookObisCode()).thenReturn(logBookObisCode3);
        when(logBook3.getId()).thenReturn(30L);

        LogBookReader logBookReader1 = new LogBookReader(this.clock, logBookObisCode1, Optional.of(lastLogBookDate1), logBookIdentifier1, new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER), SERIAL_NUMBER);
        LogBookReader logBookReader2 = new LogBookReader(this.clock, logBookObisCode2, Optional.of(lastLogBookDate2), logBookIdentifier2, new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER), SERIAL_NUMBER);
        LogBookReader logBookReader3 = new LogBookReader(this.clock, logBookObisCode3, Optional.of(lastLogBookDate3), logBookIdentifier3, new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER), SERIAL_NUMBER);

        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, device, deviceProtocol, null);
        LogBooksCommand logBooksCommand = mock(LogBooksCommand.class);
        when(logBooksCommand.getCommandRoot()).thenReturn(commandRoot);
        when(logBooksCommand.getLogBookReaders()).thenReturn(Arrays.asList(logBookReader1, logBookReader2, logBookReader3));
        ReadLogBooksCommand readLogBooksCommand = groupedDeviceCommand.getReadLogBooksCommand(logBooksCommand, comTaskExecution);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLogBook collectedLogBook1 = new DeviceLogBook(logBookIdentifier1);
        collectedLogBook1.setMeterEvents(Arrays.asList(mock(MeterProtocolEvent.class)));
        CollectedLogBook collectedLogBook2 = new DeviceLogBook(logBookIdentifier2);
        CollectedLogBook collectedLogBook3 = new DeviceLogBook(logBookIdentifier3);
        when(deviceProtocol.getLogBookData(Matchers.<List<LogBookReader>>any())).thenReturn(Arrays.asList(collectedLogBook1, collectedLogBook2, collectedLogBook3));

        readLogBooksCommand.execute(deviceProtocol, newTestExecutionContext());
        String infoJournalMessage = readLogBooksCommand.toJournalMessageDescription(LogLevel.INFO);
        String debugJournalMessage = readLogBooksCommand.toJournalMessageDescription(LogLevel.DEBUG);

        assertEquals("Expected only the three unique LogBookReaders", 3, ((ReadLogBooksCommandImpl) readLogBooksCommand).getLogBooksToCollect().size());
        assertEquals(ComCommandDescriptionTitle.ReadLogBooksCommandImpl.getDescription() + " {nrOfLogbooksToRead: 3}", infoJournalMessage);
        assertEquals(ComCommandDescriptionTitle.ReadLogBooksCommandImpl.getDescription() + " {logbooks: (1.0.1.8.1.255 - Supported - nrOfEvents: 1), (1.0.1.8.2.255 - Supported - nrOfEvents: 0), (1.0.1.8.3.255 - Supported - nrOfEvents: 0)}", debugJournalMessage);
    }
}