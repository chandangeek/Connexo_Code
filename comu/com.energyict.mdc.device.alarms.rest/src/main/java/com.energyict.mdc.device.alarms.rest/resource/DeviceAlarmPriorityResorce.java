package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.response.PriorityInfo;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/{id}/priority")
public class DeviceAlarmPriorityResorce extends BaseAlarmResource{

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ALARM)
    public Response setPriority(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, PriorityInfo priorityInfo) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        deviceAlarm.setPriority(Priority.get(priorityInfo.urgency, priorityInfo.impact));
        deviceAlarm.update();
        return Response.ok().build();
    }
}
