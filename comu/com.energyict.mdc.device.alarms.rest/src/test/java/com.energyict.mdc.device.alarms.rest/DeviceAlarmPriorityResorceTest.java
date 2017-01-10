package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.Priority;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.request.SetPriorityRequest;
import com.energyict.mdc.device.alarms.rest.response.PriorityInfo;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class DeviceAlarmPriorityResorceTest extends DeviceAlarmApplicationTest {

    @Test
    public void testSetAlarmPriority(){
        Optional<DeviceAlarm> deviceAlarm = Optional.of(getDefaultAlarm());
        doReturn(deviceAlarm).when(deviceAlarmService).findAlarm(1);
        doReturn(deviceAlarm).when(deviceAlarmService).findAndLockDeviceAlarmByIdAndVersion(1, 1);

        SetPriorityRequest priorityRequest= new SetPriorityRequest();
        priorityRequest.id = 1L;
        priorityRequest.alarm = new IssueShortInfo(1L);
        priorityRequest.priority = new PriorityInfo(Priority.DEFAULT);
        priorityRequest.alarm.version = 1L;

        Response response = target("1/priority").request().put(Entity.json(priorityRequest));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
