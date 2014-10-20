package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionResourceTest extends DeviceDataRestApplicationJerseyTest {
    private final DateTime start = new DateTime(2014, 10,10,13,10,10);
    private final DateTime end = new DateTime(2014, 10,10,13,10,20);
    private ComSession comSession1;

    @Test
    public void testGetComTaskExecutions() throws Exception {
        Device device = mock(Device.class);
        ConnectionTask<?, ?> connectionTask = mock(ConnectionTask.class);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(deviceService.findByUniqueMrid("XAW1")).thenReturn(device);
        when(connectionTask.getId()).thenReturn(3L);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getName()).thenReturn("GPRS");
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.INBOUND);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggeableClass.getName()).thenReturn("IPDIALER");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggeableClass);
        doReturn(partialConnectionTask).when(connectionTask).getPartialConnectionTask();
        ComSession comSession1 = mockComSession(connectionTask, 61L, 0);
        when(connectionTaskService.findAllSessionsFor(connectionTask)).thenReturn(Arrays.asList(comSession1));
        String response = target("/devices/XAW1/connectionmethods/3/comsessions").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<String>get("$.connectionMethod")).isEqualTo("GPRS");
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.comSessions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.comSessions[0].connectionMethod")).isEqualTo("GPRS");
        assertThat(jsonModel.<Long>get("$.comSessions[0].startedOn")).isEqualTo(start.getMillis());
        assertThat(jsonModel.<Long>get("$.comSessions[0].finishedOn")).isEqualTo(end.getMillis());
        assertThat(jsonModel.<Integer>get("$.comSessions[0].durationInSeconds")).isEqualTo(10);
        assertThat(jsonModel.<String>get("$.comSessions[0].direction")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("$.comSessions[0].connectionType")).isEqualTo("IPDIALER");
        assertThat(jsonModel.<Integer>get("$.comSessions[0].comServer.id")).isEqualTo(1234654);
        assertThat(jsonModel.<String>get("$.comSessions[0].comServer.name")).isEqualTo("communication server alfa");
        assertThat(jsonModel.<String>get("$.comSessions[0].comPort")).isEqualTo("comPort 199812981212");
        assertThat(jsonModel.<String>get("$.comSessions[0].result.id")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.comSessions[0].result.displayValue")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.comSessions[0].status")).isEqualTo("Success");
        assertThat(jsonModel.<Integer>get("$.comSessions[0].comTaskCount.numberOfSuccessfulTasks")).isEqualTo(1001);
        assertThat(jsonModel.<Integer>get("$.comSessions[0].comTaskCount.numberOfFailedTasks")).isEqualTo(1002);
        assertThat(jsonModel.<Integer>get("$.comSessions[0].comTaskCount.numberOfIncompleteTasks")).isEqualTo(1003);
        assertThat(jsonModel.<Boolean>get("$.comSessions[0].isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comSessions[0].id")).isEqualTo(61);

    }

    @Test
    public void testGetComSessionById() throws Exception {
        Device device = mock(Device.class);
        ConnectionTask<?, ?> connectionTask = mock(ConnectionTask.class);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(deviceService.findByUniqueMrid("XAW1")).thenReturn(device);
        when(connectionTask.getId()).thenReturn(3L);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getName()).thenReturn("GPRS");
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.INBOUND);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggeableClass.getName()).thenReturn("IPDIALER");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggeableClass);
        doReturn(partialConnectionTask).when(connectionTask).getPartialConnectionTask();
        ComSession comSession1 = mockComSession(connectionTask, 777L, 0);
        when(connectionTaskService.findAllSessionsFor(connectionTask)).thenReturn(Arrays.asList(comSession1));
        String response = target("/devices/XAW1/connectionmethods/3/comsessions/777").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<String>get("$.connectionMethod")).isEqualTo("GPRS");
        assertThat(jsonModel.<Long>get("$.startedOn")).isEqualTo(start.getMillis());
        assertThat(jsonModel.<Long>get("$.finishedOn")).isEqualTo(end.getMillis());
        assertThat(jsonModel.<Integer>get("$.durationInSeconds")).isEqualTo(10);
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("IPDIALER");
        assertThat(jsonModel.<Integer>get("$.comServer.id")).isEqualTo(1234654);
        assertThat(jsonModel.<String>get("$.comServer.name")).isEqualTo("communication server alfa");
        assertThat(jsonModel.<String>get("$.comPort")).isEqualTo("comPort 199812981212");
        assertThat(jsonModel.<String>get("$.result.id")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.result.displayValue")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.status")).isEqualTo("Success");
        assertThat(jsonModel.<Integer>get("$.comTaskCount.numberOfSuccessfulTasks")).isEqualTo(1001);
        assertThat(jsonModel.<Integer>get("$.comTaskCount.numberOfFailedTasks")).isEqualTo(1002);
        assertThat(jsonModel.<Integer>get("$.comTaskCount.numberOfIncompleteTasks")).isEqualTo(1003);
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(777);

    }

    @Test
    public void testGetComTaskExecutionsSorted() throws Exception {
        Device device = mock(Device.class);
        ConnectionTask<?, ?> connectionTask = mock(ConnectionTask.class);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(deviceService.findByUniqueMrid("XAW1")).thenReturn(device);
        when(connectionTask.getId()).thenReturn(3L);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getName()).thenReturn("GPRS");
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.INBOUND);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggeableClass.getName()).thenReturn("IPDIALER");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggeableClass);
        doReturn(partialConnectionTask).when(connectionTask).getPartialConnectionTask();
        ComSession comSession1 = mockComSession(connectionTask, 61L, 1);
        ComSession comSession2 = mockComSession(connectionTask, 62L, 3);
        ComSession comSession3 = mockComSession(connectionTask, 63L, 2);
        when(connectionTaskService.findAllSessionsFor(connectionTask)).thenReturn(Arrays.asList(comSession1, comSession2, comSession3));
        String response = target("/devices/XAW1/connectionmethods/3/comsessions").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List<Integer>>get("$.comSessions[*].startedOn")).isSortedAccordingTo(Comparator.<Integer>reverseOrder());
    }

    @Test
    public void testConnectionTaskJournalEntries() throws Exception {
        setupJournalMocking();
        String response = target("/devices/XAW1/connectionmethods/3/comsessions/888/journals")
                .queryParam("filter", ExtjsFilter.filter().property("logLevels", Arrays.asList("Debug", "Information")).property("logTypes", Arrays.asList("connections")).create())
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(4);
    }

    @Test
    public void testConnectionTaskJournalEntriesByLogLevel() throws Exception {
        setupJournalMocking();
        String response = target("/devices/XAW1/connectionmethods/3/comsessions/888/journals")
                .queryParam("filter", ExtjsFilter.filter().property("logLevels", Arrays.asList("Debug", "Information")).property("logTypes", Arrays.asList("connections")).create())
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(String.class);

        ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
        verify(comSession1).getJournalEntries(captor.capture());
        assertThat(captor.getAllValues().get(0)).containsExactly(ComServer.LogLevel.INFO,ComServer.LogLevel.DEBUG);
    }

    private void setupJournalMocking() {
        Device device = mock(Device.class);
        ConnectionTask<?, ?> connectionTask = mock(ConnectionTask.class);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(deviceService.findByUniqueMrid("XAW1")).thenReturn(device);
        when(connectionTask.getId()).thenReturn(3L);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getName()).thenReturn("GPRS");
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.INBOUND);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggeableClass.getName()).thenReturn("IPDIALER");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggeableClass);
        doReturn(partialConnectionTask).when(connectionTask).getPartialConnectionTask();        
        comSession1 = mockComSession(connectionTask, 888L, 1);
        when(connectionTaskService.findAllSessionsFor(connectionTask)).thenReturn(Arrays.asList(comSession1));

        ComTaskExecutionJournalEntry journalEntry1 = mockComTaskExecutionMessageJournalEntry(start.toDate(), ComServer.LogLevel.INFO, "Starting connection", "i/o error");
        ComTaskExecutionJournalEntry journalEntry1bis = mockComTaskExecutionCommandJournalEntry(start.plusSeconds(1).toDate(), ComServer.LogLevel.TRACE, "ATDT", "");
        ComTaskExecutionJournalEntry journalEntry2 = mockComTaskExecutionMessageJournalEntry(start.plusSeconds(10).toDate(), ComServer.LogLevel.INFO, "Ending connection", "finished");
        Finder<ComTaskExecutionJournalEntry> finder = mockFinder(Arrays.asList(journalEntry1, journalEntry1bis, journalEntry2));
        when(comSession1.getCommunicationTaskJournalEntries(anyObject())).thenReturn(finder);
        ComSessionJournalEntry journalEntry3 = mockComSessionJournalEntry(start.plusSeconds(1).toDate(), ComServer.LogLevel.DEBUG, "Set clock");
        ComSessionJournalEntry journalEntry4 = mockComSessionJournalEntry(start.plusSeconds(2).toDate(), ComServer.LogLevel.DEBUG, "Check clock");
        ComSessionJournalEntry journalEntry5 = mockComSessionJournalEntry(start.plusSeconds(3).toDate(), ComServer.LogLevel.DEBUG, "Reset clock");
        ComSessionJournalEntry journalEntry6 = mockComSessionJournalEntry(start.plusSeconds(4).toDate(), ComServer.LogLevel.DEBUG, "Fix clock");
        Finder<ComSessionJournalEntry> comSessionJournalEntryFinder = mockFinder(Arrays.asList(journalEntry3,journalEntry4,journalEntry5, journalEntry6));
        when(comSession1.getJournalEntries(anyObject())).thenReturn(comSessionJournalEntryFinder);
        ComSession.CombinedLogEntry combinedLogEntry1 = mockCombinedLogEntry(start.toDate(), ComServer.LogLevel.INFO, "Starting connection", null);
        ComSession.CombinedLogEntry combinedLogEntry2 = mockCombinedLogEntry(start.plusSeconds(1).toDate(), ComServer.LogLevel.DEBUG, "Set clock", "stack.trace.1");
        ComSession.CombinedLogEntry combinedLogEntry3 = mockCombinedLogEntry(start.plusSeconds(2).toDate(), ComServer.LogLevel.DEBUG, "Set clock", "stack.trace.2");
        ComSession.CombinedLogEntry combinedLogEntry4 = mockCombinedLogEntry(start.plusSeconds(3).toDate(), ComServer.LogLevel.DEBUG, "Set clock", "stack.trace.3");
        ComSession.CombinedLogEntry combinedLogEntry5 = mockCombinedLogEntry(start.plusSeconds(10).toDate(), ComServer.LogLevel.INFO, "Finished connection", null);
        when(comSession1.getAllLogs(anyObject(), anyInt(), anyInt())).thenReturn(Arrays.asList(combinedLogEntry1, combinedLogEntry2, combinedLogEntry3, combinedLogEntry4, combinedLogEntry5));
    }

    @Test
    public void testComTaskExecutionJournalEntries() throws Exception {
        setupJournalMocking();
        String response = target("/devices/XAW1/connectionmethods/3/comsessions/888/journals")
                .queryParam("filter", ExtjsFilter.filter().property("logLevels", Arrays.asList("Debug", "Information")).property("logTypes", Arrays.asList("communications")).create())
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<Long>get("$.journals[0].timestamp")).isEqualTo(1412939410000L);
        assertThat(jsonModel.<String>get("$.journals[0].logLevel")).isEqualTo("Information");
        assertThat(jsonModel.<String>get("$.journals[0].details")).isEqualTo("Starting connection");
        assertThat(jsonModel.<String>get("$.journals[0].errorDetails")).isEqualTo("i/o error");
        assertThat(jsonModel.<Long>get("$.journals[1].timestamp")).isEqualTo(1412939411000L);
        assertThat(jsonModel.<String>get("$.journals[1].logLevel")).isEqualTo("Trace");
        assertThat(jsonModel.<String>get("$.journals[1].details")).isEqualTo("ATDT");
        assertThat(jsonModel.<String>get("$.journals[1].errorDetails")).isEqualTo(""); // == OK
        assertThat(jsonModel.<Long>get("$.journals[2].timestamp")).isEqualTo(1412939420000L);
        assertThat(jsonModel.<String>get("$.journals[2].logLevel")).isEqualTo("Information");
        assertThat(jsonModel.<String>get("$.journals[2].details")).isEqualTo("Ending connection");
        assertThat(jsonModel.<String>get("$.journals[2].errorDetails")).isEqualTo("finished");
    }

    @Test
    public void testCombinedEntries() throws Exception {
        setupJournalMocking();
        String response = target("/devices/XAW1/connectionmethods/3/comsessions/888/journals")
                .queryParam("filter", ExtjsFilter.filter().property("logLevels", Arrays.asList("Debug", "Information")).property("logTypes", Arrays.asList("connections", "communications")).create())
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(5);
    }

    @Test
    public void testGetAllWithoutFilterOrPaging() throws Exception {
        setupJournalMocking();
        String response = target("/devices/XAW1/connectionmethods/3/comsessions/888/journals")
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(5);
    }

    private ComSession.CombinedLogEntry mockCombinedLogEntry(Date timestamp, ComServer.LogLevel logLevel, String message, String errorDetail) {
        ComSession.CombinedLogEntry mock = mock(ComSession.CombinedLogEntry.class);
        when(mock.getTimestamp()).thenReturn(timestamp);
        when(mock.getLogLevel()).thenReturn(logLevel);
        when(mock.getErrorDetail()).thenReturn(errorDetail);
        when(mock.getDetail()).thenReturn(message);
        return mock;
    }

    private ComSessionJournalEntry mockComSessionJournalEntry(Date timestamp, ComServer.LogLevel logLevel, String message) {
        ComSessionJournalEntry mock = mock(ComSessionJournalEntry.class);
        when(mock.getTimestamp()).thenReturn(timestamp);
        when(mock.getLogLevel()).thenReturn(logLevel);
        when(mock.getMessage()).thenReturn(message);
        return mock;
    }

    private ComTaskExecutionMessageJournalEntry mockComTaskExecutionMessageJournalEntry(Date timestamp, ComServer.LogLevel logLevel, String message, String errorDescription) {
        ComTaskExecutionMessageJournalEntry mock = mock(ComTaskExecutionMessageJournalEntry.class);
        when(mock.getMessage()).thenReturn(message);
        when(mock.getErrorDescription()).thenReturn(errorDescription);
        when(mock.getTimestamp()).thenReturn(timestamp);
        when(mock.getLogLevel()).thenReturn(logLevel);
        return mock;
    }

    private ComCommandJournalEntry mockComTaskExecutionCommandJournalEntry(Date timestamp, ComServer.LogLevel logLevel, String commandDescription, String errorDescription) {
        ComCommandJournalEntry mock = mock(ComCommandJournalEntry.class);
        when(mock.getCommandDescription()).thenReturn(commandDescription);
        when(mock.getErrorDescription()).thenReturn(errorDescription);
        when(mock.getTimestamp()).thenReturn(timestamp);
        when(mock.getLogLevel()).thenReturn(logLevel);
        return mock;
    }

    private ComSession mockComSession(ConnectionTask<?, ?> connectionTask, Long id, int startDelay) {
        ComSession comSession = mock(ComSession.class);
        when(comSession.getId()).thenReturn(id);
        when(comSession.getStartDate()).thenReturn(start.plusHours(startDelay).toDate().toInstant());
        when(comSession.getStopDate()).thenReturn(end.plusHours(startDelay).toDate().toInstant());
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("comPort 199812981212");
        when(comPort.getId()).thenReturn(199812981212L);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(1234654L);
        when(comServer.getName()).thenReturn("communication server alfa");
        when(comPort.getComServer()).thenReturn(comServer);
        when(comSession.getComPort()).thenReturn(comPort);
        when(comSession.getConnectionTask()).thenReturn(connectionTask);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(1001);
        when(comSession.getNumberOfFailedTasks()).thenReturn(1002);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(1003);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        return comSession;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}
