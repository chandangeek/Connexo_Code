package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
    public Response addComScheduleOnDevice(@PathParam("mRID") String mrid, ScheduleIdsInfo info) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        info.scheduleIds.stream()
                .map(schedulingService::findSchedule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(comSchedule -> device.newScheduledComTaskExecution(comSchedule).add());
        device.save();
        return Response.status(Response.Status.OK).build();
    }

    static class ScheduleIdsInfo {
        public List<Long> scheduleIds;

        public ScheduleIdsInfo() {
        }
    }
}
