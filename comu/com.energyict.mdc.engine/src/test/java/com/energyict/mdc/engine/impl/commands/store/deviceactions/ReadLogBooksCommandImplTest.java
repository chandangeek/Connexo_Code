package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.core.JobExecution;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.LogBooksCommand;
import com.energyict.mdc.commands.ReadLogBooksCommand;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.LogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.tasks.LogBooksTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.LogBooksTask;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
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
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.ReadLogBooksCommandImpl} component
 *
 * @author sva
 * @since 12/12/12 - 13:48
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadLogBooksCommandImplTest extends AbstractComCommandExecuteTest {

    @Mock
    private ComTaskExecution comTaskExecution;

    @Test
    public void commandTypeTest() {
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadLogBooksCommand readLogBooksCommand = new ReadLogBooksCommandImpl(mock(LogBooksCommand.class), commandRoot);

        // asserts
        Assert.assertEquals(ComCommandTypes.READ_LOGBOOKS_COMMAND, readLogBooksCommand.getCommandType());
    }

    @Test
    public void testExecuteCommand() throws Exception {
        OfflineDevice device = mock(OfflineDevice.class);
        LogBooksTask logBooksTask = mock(LogBooksTask.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(device, executionContext, issueService);
        LogBooksCommand logBooksCommand = commandRoot.getLogBooksCommand(logBooksTask, commandRoot, comTaskExecution);
        ReadLogBooksCommand readLogBooksCommand = commandRoot.getReadLogBooksCommand(logBooksCommand, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class);
        when(deviceProtocol.getLogBookData(Matchers.<List<LogBookReader>>any())).thenReturn(Arrays.asList(collectedLogBook));

        readLogBooksCommand.execute(deviceProtocol, executionContext);

        // asserts
        assertNotNull(logBooksCommand.getCollectedData());
        Assert.assertEquals("Should have one collectedLogBook", 1, logBooksCommand.getCollectedData().size());
        assertNotNull(readLogBooksCommand.getIssues());
        Assert.assertEquals("Should have no issues", 0, logBooksCommand.getIssues().size());
        Assert.assertEquals("Should have no problems", 0, logBooksCommand.getProblems().size());
        Assert.assertEquals("Should have no warnings", 0, logBooksCommand.getWarnings().size());
    }

    @Test
    public void testAddOnlyUniqueLogBooks() {
        final ObisCode logBookObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        final ObisCode logBookObisCode2 = ObisCode.fromString("1.0.1.8.2.255");
        final ObisCode logBookObisCode3 = ObisCode.fromString("1.0.1.8.3.255");

        final String SERIAL_NUMBER = "SerialNumber";

        final Date lastLogBookDate1 = new Date(1354320000L * 1000L); // 01 Dec 2012 00:00:00 GMT
        final Date lastLogBookDate2 = new Date(1351728000L * 1000L); // 01 Nov 2012 00:00:00 GMT
        final Date lastLogBookDate3 = new Date(1349049600L * 1000L); // 01 Oct 2012 00:00:00 GMT

        LogBookIdentifier logBookIdentifier1 = mock(LogBookIdentifier.class);
        BaseLogBook logBook1 = mock(BaseLogBook.class);
        when((logBookIdentifier1).getLogBook()).thenReturn(logBook1);
        when(logBook1.getId()).thenReturn(10);
        LogBookIdentifier logBookIdentifier2 = mock(LogBookIdentifier.class);
        BaseLogBook logBook2 = mock(BaseLogBook.class);
        when((logBookIdentifier2).getLogBook()).thenReturn(logBook2);
        when(logBook2.getId()).thenReturn(20);
        LogBookIdentifier logBookIdentifier3 = mock(LogBookIdentifier.class);
        BaseLogBook logBook3 = mock(BaseLogBook.class);
        when((logBookIdentifier3).getLogBook()).thenReturn(logBook3);
        when(logBook3.getId()).thenReturn(30);

        LogBookReader logBookReader1 = new LogBookReader(logBookObisCode1, lastLogBookDate1, logBookIdentifier1, SERIAL_NUMBER);
        LogBookReader logBookReader2 = new LogBookReader(logBookObisCode2, lastLogBookDate2, logBookIdentifier2, SERIAL_NUMBER);
        LogBookReader logBookReader3 = new LogBookReader(logBookObisCode3, lastLogBookDate3, logBookIdentifier3, SERIAL_NUMBER);

        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(device, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
        ReadLogBooksCommand readLogBooksCommand = commandRoot.getReadLogBooksCommand(mock(LogBooksCommand.class), comTaskExecution);
        readLogBooksCommand.addLogBooks(Arrays.asList(logBookReader1, logBookReader2, logBookReader3, logBookReader1, logBookReader2));

        assertEquals("Expected only the three unique LogBookReaders", 3, ((ReadLogBooksCommandImpl) readLogBooksCommand).getLogBooksToCollect().size());
        Assert.assertEquals("ReadLogBooksCommandImpl {logbookObisCodes: 1.0.1.8.1.255, 1.0.1.8.2.255, 1.0.1.8.3.255}", readLogBooksCommand.toJournalMessageDescription(LogLevel.ERROR));
    }
}
