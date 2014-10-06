package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.joda.time.Duration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionResourceTest extends DeviceDataRestApplicationJerseyTest {
    private final Date start = new Date(1412341200000L);
    private final Date end = new Date(1412341300000L);

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
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        ComSession comSession1 = mockComSession(connectionTask);
        ComSession comSession2 = mockComSession(connectionTask);
        when(connectionTaskService.findAllSessionsFor(connectionTask)).thenReturn(Arrays.asList(comSession1, comSession2));
        String response = target("/devices/XAW1/connectionmethods/3/comsessions").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<String>get("$.connectionMethod")).isEqualTo("GPRS");
        assertThat(jsonModel.<Integer>get("$.comSessionPage.total")).isEqualTo(2);
        assertThat(jsonModel.<List>get("$.comSessionPage.comSessions")).hasSize(2);
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].connectionMethod")).isEqualTo("GPRS");
        assertThat(jsonModel.<Long>get("$.comSessionPage.comSessions[0].startedOn")).isEqualTo(start.getTime());
        assertThat(jsonModel.<Long>get("$.comSessionPage.comSessions[0].finishedOn")).isEqualTo(end.getTime());
        assertThat(jsonModel.<Integer>get("$.comSessionPage.comSessions[0].durationInSeconds")).isEqualTo(120);
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].direction")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].connectionType")).isEqualTo("IPDIALER");
        assertThat(jsonModel.<Integer>get("$.comSessionPage.comSessions[0].comServer.id")).isEqualTo(1234654);
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].comServer.name")).isEqualTo("communication server alfa");
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].comPort")).isEqualTo("comPort 199812981212");
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].result.id")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].result.displayValue")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.comSessionPage.comSessions[0].status")).isEqualTo("Success");
        assertThat(jsonModel.<Integer>get("$.comSessionPage.comSessions[0].comTaskCount.numberOfSuccessfulTasks")).isEqualTo(1001);
        assertThat(jsonModel.<Integer>get("$.comSessionPage.comSessions[0].comTaskCount.numberOfFailedTasks")).isEqualTo(1002);
        assertThat(jsonModel.<Integer>get("$.comSessionPage.comSessions[0].comTaskCount.numberOfIncompleteTasks")).isEqualTo(1003);
        assertThat(jsonModel.<Boolean>get("$.comSessionPage.comSessions[0].isDefault")).isEqualTo(true);


    }

    private ComSession mockComSession(ConnectionTask<?,?> connectionTask) {
        ComSession comSession = mock(ComSession.class);
        when(comSession.getStartDate()).thenReturn(start);
        when(comSession.getStopDate()).thenReturn(end);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("comPort 199812981212");
        when(comPort.getId()).thenReturn(199812981212L);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(1234654L);
        when(comServer.getName()).thenReturn("communication server alfa");
        when(comPort.getComServer()).thenReturn(comServer);
        when(comSession.getComPort()).thenReturn(comPort);
        when(comSession.getTotalDuration()).thenReturn(Duration.standardMinutes(2));
        when(comSession.getConnectionTask()).thenReturn(connectionTask);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(1001);
        when(comSession.getNumberOfFailedTasks()).thenReturn(1002);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(1003);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        return comSession;
    }
}
