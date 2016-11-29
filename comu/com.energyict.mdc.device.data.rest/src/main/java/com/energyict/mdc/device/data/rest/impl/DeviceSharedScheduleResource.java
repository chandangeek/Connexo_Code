package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
@Path("/devices/{name}/sharedschedules")
public class DeviceSharedScheduleResource {

    private final ResourceHelper resourceHelper;
    private final SchedulingService schedulingService;

    @Inject
    public DeviceSharedScheduleResource(ResourceHelper resourceHelper, SchedulingService schedulingService) {
        this.resourceHelper = resourceHelper;
        this.schedulingService = schedulingService;
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response addComScheduleOnDevice(ScheduleIdsInfo info) {
        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
        List<ComSchedule> comSchedules = info.scheduleIds.stream()
                .map(schedulingService::findSchedule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        checkValidity(comSchedules, device);
        comSchedules.forEach(comSchedule -> {
            try {
                device.newScheduledComTaskExecution(comSchedule).add();
            } catch (ConstraintViolationException cve) {
                throw new AlreadyLocalizedException(cve.getConstraintViolations().iterator().next().getMessage());
            }
        });
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response removeComSchedulesOnDevice(@PathParam("mRID") String mrid, ScheduleIdsInfo info) {
        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
        info.scheduleIds.stream()
                .map(schedulingService::findSchedule)
                .forEach(comSchedule -> comSchedule.ifPresent(device::removeComSchedule));

        return Response.status(Response.Status.OK).build();
    }

    /**
     * Checks if the comschedules have overlapping tasks or if the device already has a comtask execution for one of the comtasks of the comschedules
     *
     * @param comSchedules
     * @param device
     */
    private void checkValidity(List<ComSchedule> comSchedules, Device device) {
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        boolean comTaskOfSchedulesAlreadyScheduled = comSchedules.stream()
                .filter(comSchedule -> isAlreadyScheduled(comSchedule.getComTasks(), comTaskExecutions))
                .findFirst()
                .isPresent();

        if (comTaskOfSchedulesAlreadyScheduled) {
            throw new IllegalArgumentException("One of the ComTasks of the given schedule is already scheduled using a shared schedule.");
        }

        boolean overlap = comSchedules.stream()
                .anyMatch(schedule1 -> comSchedules.stream()
                        .anyMatch(schedule2 -> !schedule1.equals(schedule2) && hasOverlappingTasks(schedule1, schedule2)));

        if (overlap) {
            throw new IllegalArgumentException("At least two of the given comschedules hava the same ComTask.");
        }

    }

    private boolean hasOverlappingTasks(ComSchedule schedule1, ComSchedule schedule2) {
        List<ComTask> overlappingComTasks = new ArrayList<>(schedule1.getComTasks());
        overlappingComTasks.retainAll(schedule2.getComTasks());

        return overlappingComTasks.size() > 0;
    }

    private boolean isAlreadyScheduled(List<ComTask> comTasks, List<ComTaskExecution> comTaskExecutions) {
        return comTaskExecutions.stream()
                .filter(ComTaskExecution::usesSharedSchedule)
                .map(ComTaskExecution::getComTask)
                .filter(comTasks::contains)
                .findFirst()
                .isPresent();
    }

    static class ScheduleIdsInfo {
        public List<Long> scheduleIds;
        public DeviceInfo device;

        public ScheduleIdsInfo() {
        }
    }

    private class AlreadyLocalizedException extends LocalizedException {
        private final String message;

        public AlreadyLocalizedException(String message) {
            super(null, MessageSeeds.BAD_ACTION);
            this.message = message;
        }

        @Override
        public String getLocalizedMessage() {
            return message;
        }
    }
}
