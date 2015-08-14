package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
@Path("/devices/{mRID}/sharedschedules")
public class DeviceSharedScheduleResource {

    private final ResourceHelper resourceHelper;
    private final SchedulingService schedulingService;

    @Inject
    public DeviceSharedScheduleResource(ResourceHelper resourceHelper, SchedulingService schedulingService) {
        this.resourceHelper = resourceHelper;
        this.schedulingService = schedulingService;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response addComScheduleOnDevice(@PathParam("mRID") String mrid, List<Long> comScheduleIds) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        comScheduleIds.stream()
                .map(schedulingService::findSchedule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(comSchedule -> device.newScheduledComTaskExecution(comSchedule).add());
        device.save();
        return Response.status(Response.Status.OK).build();
    }

}
