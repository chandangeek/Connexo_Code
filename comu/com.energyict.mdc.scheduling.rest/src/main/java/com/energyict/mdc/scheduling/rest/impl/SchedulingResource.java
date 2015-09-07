package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.common.rest.IdListBuilder;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.security.Privileges;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/schedules")
public class SchedulingResource {

    private final SchedulingService schedulingService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;

    @Inject
    public SchedulingResource(SchedulingService schedulingService, DeviceService deviceService, Clock clock, DeviceConfigurationService deviceConfigurationService, TaskService taskService) {
        this.schedulingService = schedulingService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public PagedInfoList getSchedules(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        String mrid = queryFilter.hasProperty("mrid") ? queryFilter.getString("mrid") : null;
        boolean available = queryFilter.hasProperty("available") ? queryFilter.getBoolean("available") : false;
        List<ComSchedule> comSchedules = schedulingService.findAllSchedules();
        Collections.sort(comSchedules, new CompareBySchedulingStatus());
        if (mrid != null && available) {
            filterAvailableSchedulesOnly(mrid, comSchedules);
        }
        comSchedules = ListPager.of(comSchedules).from(queryParameters).find();

        List<ComScheduleInfo> comScheduleInfos = new ArrayList<>();
        for (ComSchedule comSchedule : comSchedules) {
            comScheduleInfos.add(ComScheduleInfo.from(comSchedule, isInUse(comSchedule)));
        }
        return PagedInfoList.fromPagedList("schedules", comScheduleInfos, queryParameters);
    }

    private void filterAvailableSchedulesOnly(String mrid, List<ComSchedule> comSchedules) {
        deviceService
            .findByUniqueMrid(mrid)
            .ifPresent(device -> {
                List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
                List<ComTaskEnablement> comTaskEnablements = device.getDeviceConfiguration().getComTaskEnablements();
                Iterator<ComSchedule> iterator = comSchedules.iterator();
                while(iterator.hasNext()) {
                    ComSchedule comSchedule = iterator.next();
                    if (!isValidComSchedule(comSchedule, comTaskExecutions, comTaskEnablements)) {
                        iterator.remove();
                    }
                }
        });
    }

    private static class CompareBySchedulingStatus implements Comparator<ComSchedule> {
        @Override
        public int compare(ComSchedule o1, ComSchedule o2) {
            if (SchedulingStatus.PAUSED.equals(o1.getSchedulingStatus()) && SchedulingStatus.PAUSED.equals(o2.getSchedulingStatus())) {
                return 0;
            }
            if (SchedulingStatus.PAUSED.equals(o1.getSchedulingStatus()) && !SchedulingStatus.PAUSED.equals(o2.getSchedulingStatus())) {
                return 1;
            }
            if (!SchedulingStatus.PAUSED.equals(o1.getSchedulingStatus()) && SchedulingStatus.PAUSED.equals(o2.getSchedulingStatus())) {
                return -1;
            }
            // Neither are paused so planned date is always there
            return o1.getPlannedDate().get().compareTo(o2.getPlannedDate().get());
        }
    }



    private boolean isValidComSchedule(ComSchedule comSchedule, List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        Set<Long> allowedComTaskIds = getAllowedComTaskIds(comTaskEnablements);
        Set<Long> alreadyAssignedComTaskIds = getAlreadyAssignedComTaskIds(comTaskExecutions);
        Set<Long> toBeVerifiedComTaskIds = new HashSet<>();
        if (!hasSameConfigurationSettingsInEnablements(comSchedule, comTaskEnablements)) {
            return false;
        }
        for (ComTask comTaskFromSchedule : comSchedule.getComTasks()) {
            if (alreadyAssignedComTaskIds.contains(comTaskFromSchedule.getId())) {
                return false;
            }
            toBeVerifiedComTaskIds.add(comTaskFromSchedule.getId());
        }
        return allowedComTaskIds.containsAll(toBeVerifiedComTaskIds);
    }

    private boolean hasSameConfigurationSettingsInEnablements(ComSchedule comSchedule, List<ComTaskEnablement> comTaskEnablements) {
        List<ComTaskEnablement> comTaskEnablementsToCheck = getComTaskEnablementsForComTasks(comSchedule, comTaskEnablements);
        if (comTaskEnablementsToCheck.size() == 0) {
            return false;
        } else if (comTaskEnablementsToCheck.size() == 1) {
            return true;
        } else {
            long protocolDialectConfigurationPropertiesId = 0;
            ComTaskEnablement firstComTaskEnablement = comTaskEnablementsToCheck.get(0);
            protocolDialectConfigurationPropertiesId = firstComTaskEnablement.getProtocolDialectConfigurationProperties().getId();
            long securityPropertySetId = firstComTaskEnablement.getSecurityPropertySet().getId();
            long partialConnectionTaskId = 0;
            if (firstComTaskEnablement.getPartialConnectionTask().isPresent()) {
                partialConnectionTaskId = firstComTaskEnablement.getPartialConnectionTask().get().getId();
            }
            int priority = firstComTaskEnablement.getPriority();
            for (int i = 1; i < comTaskEnablementsToCheck.size(); i++) {

                long compareProtocolDialectConfigurationPropertiesId = 0;
                ComTaskEnablement otherComTaskEnablement = comTaskEnablementsToCheck.get(i);
                compareProtocolDialectConfigurationPropertiesId = otherComTaskEnablement.getProtocolDialectConfigurationProperties().getId();
                long compareSecurityPropertySetId = otherComTaskEnablement.getSecurityPropertySet().getId();
                long comparePartialConnectionTaskId = 0;
                if (otherComTaskEnablement.getPartialConnectionTask().isPresent()) {
                    comparePartialConnectionTaskId = otherComTaskEnablement.getPartialConnectionTask().get().getId();
                }
                int comparePriority = otherComTaskEnablement.getPriority();
                if (protocolDialectConfigurationPropertiesId != compareProtocolDialectConfigurationPropertiesId ||
                        securityPropertySetId != compareSecurityPropertySetId ||
                        partialConnectionTaskId != comparePartialConnectionTaskId ||
                        priority != comparePriority) {
                    return false;
                }
            }
            return true;
        }

    }

    private List<ComTaskEnablement> getComTaskEnablementsForComTasks(ComSchedule comSchedule, List<ComTaskEnablement> comTaskEnablements) {
        List<ComTaskEnablement> comTaskEnablementsToCheck = new ArrayList<>();
        for (ComTask comTask : comSchedule.getComTasks()) {
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                if (comTaskEnablement.getComTask().getId() == comTask.getId()) {
                    comTaskEnablementsToCheck.add(comTaskEnablement);
                }
            }
        }
        return comTaskEnablementsToCheck;
    }

    private Set<Long> getAllowedComTaskIds(List<ComTaskEnablement> comTaskEnablements) {
        Set<Long> allowedComTaskIds = new HashSet<>();
        for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
            allowedComTaskIds.add(comTaskEnablement.getComTask().getId());
        }
        return allowedComTaskIds;
    }

    private Set<Long> getAlreadyAssignedComTaskIds(List<ComTaskExecution> comTaskExecutions) {
        Set<Long> alreadyAssignedComTaskIds = new HashSet<>();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            if (!comTaskExecution.isAdHoc()) {
                for (ComTask comTask : comTaskExecution.getComTasks()) {
                    alreadyAssignedComTaskIds.add(comTask.getId());
                }
            }
        }
        return alreadyAssignedComTaskIds;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public ComScheduleInfo getSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        return ComScheduleInfo.from(comSchedule, isInUse(comSchedule));
    }

    private ComSchedule findComScheduleOrThrowException(long id) {
        Optional<ComSchedule> comSchedule = schedulingService.findSchedule(id);
        if (!comSchedule.isPresent()) {
            throw new WebApplicationException("No such schedule", Response.Status.NOT_FOUND);
        }
        return comSchedule.get();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public Response createSchedule(ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleInfo.name, comScheduleInfo.temporalExpression.asTemporalExpression(),
                comScheduleInfo.startDate == null ? null : comScheduleInfo.startDate).mrid(comScheduleInfo.mRID).build();
        if (comScheduleInfo.comTaskUsages != null) {
            updateTasks(comSchedule, comScheduleInfo.comTaskUsages);
        }
        comSchedule.save();
        return Response.status(Response.Status.CREATED).entity(ComScheduleInfo.from(comSchedule, false)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public Response deleteSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        if (this.isInUse(comSchedule)) {
            comSchedule.makeObsolete();
        } else {
            comSchedule.delete();
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public ComScheduleInfo updateSchedules(@PathParam("id") long id, ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        comSchedule.setName(comScheduleInfo.name);
        comSchedule.setTemporalExpression(comScheduleInfo.temporalExpression == null ? null : comScheduleInfo.temporalExpression.asTemporalExpression());
        comSchedule.setStartDate(comScheduleInfo.startDate == null ? null : comScheduleInfo.startDate);
        comSchedule.setmRID(comScheduleInfo.mRID);
        if (comScheduleInfo.comTaskUsages != null) {
            updateTasks(comSchedule, comScheduleInfo.comTaskUsages);
        }
        comSchedule.save();
        return ComScheduleInfo.from(findComScheduleOrThrowException(id), isInUse(comSchedule));
    }

    private void updateTasks(ComSchedule comSchedule, List<ComTaskInfo> comTaskUsages) {
        Map<Long, ComTaskInfo> newComTaskIdMap = asIdz(comTaskUsages);
        for (ComTask comTask : comSchedule.getComTasks()) {
            if (newComTaskIdMap.containsKey(comTask.getId())) {
                // Updating ComTasks not allowed here
                newComTaskIdMap.remove(comTask.getId());
            } else {
                comSchedule.removeComTask(comTask);
            }
        }

        for (ComTaskInfo comTaskInfo : newComTaskIdMap.values()) {
            ComTask comTask = taskService.findComTask(comTaskInfo.id).orElseThrow(() -> new WebApplicationException("No ComTask with id " + comTaskInfo.id, Response.Status.BAD_REQUEST));
            comSchedule.addComTask(comTask);
        }
    }

    private Map<Long, ComTaskInfo> asIdz(Collection<ComTaskInfo> comTaskInfos) {
        Map<Long, ComTaskInfo> comTaskIdMap = new HashMap<>();
        for (ComTaskInfo comTaskInfo : comTaskInfos) {
            comTaskIdMap.put(comTaskInfo.id, comTaskInfo);
        }
        return comTaskIdMap;
    }

    @GET
    @Path("/{id}/comTasks")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public Response getComTasks(@PathParam("id") long id, @BeanParam JsonQueryFilter queryFilter) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        if (queryFilter.hasProperty("available") && queryFilter.getBoolean("available")) {
            return Response.ok().entity(ComTaskInfo.from(getAvailableComTasksExcludingAlreadyAssigned(comSchedule))).build();
        } else {
            return Response.ok().entity(ComTaskInfo.from(comSchedule.getComTasks())).build();
        }
    }

    @PUT
    @Path("/preview")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public Response generatePreviewForSchedule(PreviewInfo previewInfo) {
        if (previewInfo.temporalExpression == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression");
        }
        if (previewInfo.temporalExpression.every == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression.every");
        }
        previewInfo.nextOccurrences = calculateNextOccurrences(previewInfo);
        return Response.ok().entity(previewInfo).build();
    }

    private List<Date> calculateNextOccurrences(PreviewInfo previewInfo) {
        TemporalExpression temporalExpression = previewInfo.temporalExpression.asTemporalExpression();
        List<Date> nextOccurrences = new ArrayList<>();
        Date occurrence = previewInfo.startDate == null ? Date.from(this.clock.instant()) : previewInfo.startDate;
        Calendar latestOccurrence = Calendar.getInstance();
        for (int i = 0; i < 5; i++) {
            latestOccurrence.setTime(occurrence);
            occurrence = temporalExpression.nextOccurrence(latestOccurrence);
            nextOccurrences.add(occurrence);
        }
        return nextOccurrences;
    }

    private List<ComTask> getAvailableComTasksExcludingAlreadyAssigned(ComSchedule comSchedule) {
        List<ComTask> remainingComTasks = new ArrayList<>();
        Map<Long, ComTask> existingComTasks = IdListBuilder.asIdMap(comSchedule.getComTasks());
        for (ComTask availableComTask : deviceConfigurationService.findAvailableComTasks(comSchedule)) {
            if (!existingComTasks.containsKey(availableComTask.getId())) {
                remainingComTasks.add(availableComTask);
            }
        }

        return remainingComTasks;
    }

    private boolean isInUse(ComSchedule comSchedule) {
        return this.deviceService.isLinkedToDevices(comSchedule);
    }

}
