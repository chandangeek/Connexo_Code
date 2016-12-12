package com.energyict.mdc.device.alarms.rest;


import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class DeviceAlarmResourceTest extends DeviceAlarmApplicationTest{

    @Test
    public void testGetAlarmById() {
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        Optional<DeviceAlarm> alarm = Optional.of(getDefaultAlarm());
        doReturn(alarm).when(deviceAlarmService).findAlarm(1);

        Map<?, ?> alarmMap = target("/alarms/1").request().get(Map.class);
        assertDefaultAlarmMap(alarmMap);
    }

    @Test
    public void testGetUnexistingAlarmById() {
        when(deviceAlarmService.findAlarm(1)).thenReturn(Optional.empty());

        Response response = target("/alarms/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    private void assertDefaultAlarmMap(Map<?, ?> alarmMap) {
        assertThat(alarmMap.get("id")).isEqualTo(1);
        assertThat(alarmMap.get("alarmId")).isEqualTo("ALM-001");

        Map<?, ?> reasonMap = (Map<?, ?>) alarmMap.get("reason");
        assertThat(reasonMap.get("id")).isEqualTo(0);
        assertThat(reasonMap.get("name")).isEqualTo("Reason");

        Map<?, ?> statusMap = (Map<?, ?>) alarmMap.get("status");
        assertThat(statusMap.get("id")).isEqualTo(0);
        assertThat(statusMap.get("name")).isEqualTo("open");

        Map<?, ?> userAssignee = (Map<?, ?>) alarmMap.get("userAssignee");
        assertThat(userAssignee.get("id")).isEqualTo(1);
        assertThat(userAssignee.get("name")).isEqualTo("Admin");

        Map<?, ?> workGroupAssignee = (Map<?, ?>) alarmMap.get("workGroupAssignee");
        assertThat(workGroupAssignee.get("id")).isEqualTo(1);
        assertThat(workGroupAssignee.get("name")).isEqualTo("WorkGroup");

        Map<?, ?> logBook = (Map<?, ?>) alarmMap.get("logBook");
        assertThat(logBook.get("id")).isEqualTo(1);
        assertThat(logBook.get("name")).isEqualTo("LogBookName");

        List<?> releatedEvents = (List<?>) alarmMap.get("relatedEvents");
        Map<?, ?> releatedEventMap = (Map<?, ?>) releatedEvents.get(0);
        assertThat(releatedEventMap.get("deviceType")).isEqualTo("Collector");
        assertThat(releatedEventMap.get("domain")).isEqualTo("Battery");
        assertThat(releatedEventMap.get("subDomain")).isEqualTo("Activation");
        assertThat(releatedEventMap.get("eventDate")).isEqualTo(1451606400000L);

    }

}
