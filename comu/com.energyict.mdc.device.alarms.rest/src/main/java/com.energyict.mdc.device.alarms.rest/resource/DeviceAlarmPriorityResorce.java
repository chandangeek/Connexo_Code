/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.rest.request.SetPriorityRequest;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/{id}/priority")
public class DeviceAlarmPriorityResorce extends BaseAlarmResource{

    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public DeviceAlarmPriorityResorce(ConcurrentModificationExceptionFactory conflictFactory){
        this.conflictFactory = conflictFactory;
    }


    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ALARM)
    public ActionInfo setPriority(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, SetPriorityRequest request) {
        ActionInfo actionInfo = new ActionInfo();
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAndLockDeviceAlarmByIdAndVersion(id, request.alarm.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.alarm.title)
                        .withActualVersion(() -> getDeviceAlarmService().findAlarm(id)
                                .map(Entity::getVersion)
                                .orElse(null))
                        .supplier());
        deviceAlarm.setPriority(Priority.get(request.priority.urgency, request.priority.impact));
        deviceAlarm.update();
        actionInfo.addSuccess(deviceAlarm.getId(), getThesaurus().getFormat(MessageSeeds.ACTION_ALARM_PRIORITY_WAS_CHANGED).format());
        return actionInfo;
    }
}
