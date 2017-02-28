/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.rest.request.SetPriorityRequest;
import com.energyict.mdc.device.alarms.rest.response.PriorityInfo;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceAlarmPriorityResorceTest extends DeviceAlarmApplicationTest {

    @Test
    public void testSetAlarmPriority(){
        NlsMessageFormat message = mock(NlsMessageFormat.class);
        when(message.format(any())).thenReturn("ABCDEFG");

        when(thesaurus.getFormat(any(MessageSeeds.class))).thenReturn(message);

        Optional<DeviceAlarm> deviceAlarm = Optional.of(getDefaultAlarm());
        doReturn(deviceAlarm).when(deviceAlarmService).findAlarm(1);
        doReturn(deviceAlarm).when(deviceAlarmService).findAndLockDeviceAlarmByIdAndVersion(1, 1);
        NlsMessageFormat nlsMessageFormat = this.mockNlsMessageFormat(MessageSeeds.ACTION_ALARM_PRIORITY_WAS_CHANGED.getDefaultFormat());
        when(getThesaurus().getFormat(MessageSeeds.ACTION_ALARM_PRIORITY_WAS_CHANGED)).thenReturn(nlsMessageFormat);

        SetPriorityRequest priorityRequest= new SetPriorityRequest();
        priorityRequest.id = 1L;
        priorityRequest.alarm = new IssueShortInfo(1L);
        priorityRequest.priority = new PriorityInfo(Priority.DEFAULT);
        priorityRequest.alarm.version = 1L;

        Response response = target("1/priority").request().put(Entity.json(priorityRequest));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
