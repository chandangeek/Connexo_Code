package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.rest.BooleanAdapter;
import com.energyict.mdc.common.rest.IdListBuilder;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final DeviceDataService deviceDataService;
    private final Clock clock;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final Thesaurus thesaurus;

    @Inject
    public SchedulingResource(SchedulingService schedulingService, DeviceDataService deviceDataService, Clock clock, DeviceConfigurationService deviceConfigurationService, TaskService taskService, Thesaurus thesaurus) {
        this.schedulingService = schedulingService;
        this.deviceDataService = deviceDataService;
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getSchedules(@BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        Calendar calendar = Calendar.getInstance(clock.getTimeZone());
        List<ComSchedule> comSchedules = schedulingService.findAllSchedules(calendar).from(queryParameters).find();
        List<ComScheduleInfo> comScheduleInfos = new ArrayList<>();
        for (ComSchedule comSchedule : comSchedules) {
            comScheduleInfos.add(ComScheduleInfo.from(comSchedule, isInUse(comSchedule)));
        }

        return PagedInfoList.asJson("schedules", comScheduleInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComScheduleInfo getSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        return ComScheduleInfo.from(comSchedule, isInUse(comSchedule));
    }

    private ComSchedule findComScheduleOrThrowException(long id) {
        ComSchedule comSchedule = schedulingService.findSchedule(id);
        if (comSchedule==null) {
            throw new WebApplicationException("No such schedule", Response.Status.NOT_FOUND);
        }
        return comSchedule;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSchedule(ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleInfo.name, comScheduleInfo.temporalExpression.asTemporalExpression(),
                comScheduleInfo.startDate==null?null:new UtcInstant(comScheduleInfo.startDate)).mrid(comScheduleInfo.mRID).build();
        if(comScheduleInfo.comTaskUsages!=null){
            updateTasks(comSchedule, comScheduleInfo.comTaskUsages);
        }
        return Response.status(Response.Status.CREATED).entity(ComScheduleInfo.from(comSchedule, isInUse(comSchedule))).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        comSchedule.delete();
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComScheduleInfo updateSchedules(@PathParam("id") long id, ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        comSchedule.setName(comScheduleInfo.name);
        comSchedule.setTemporalExpression(comScheduleInfo.temporalExpression==null?null:comScheduleInfo.temporalExpression.asTemporalExpression());
        comSchedule.setSchedulingStatus(comScheduleInfo.schedulingStatus);
        comSchedule.setStartDate(comScheduleInfo.startDate==null?null:new UtcInstant(comScheduleInfo.startDate));
        comSchedule.setmRID(comScheduleInfo.mRID);
        comSchedule.save();
        if (comScheduleInfo.comTaskUsages!=null) {
            updateTasks(comSchedule, comScheduleInfo.comTaskUsages);
        }
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
            ComTask comTask = taskService.findComTask(comTaskInfo.id);
            if (comTask == null) {
                throw new WebApplicationException("No ComTask with id "+comTaskInfo.id, Response.Status.BAD_REQUEST);
            }
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComTasks(@PathParam("id") long id, @BeanParam JsonQueryFilter queryFilter) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        if (queryFilter.getFilterProperties().containsKey("available") && queryFilter.getProperty("available", new BooleanAdapter(), thesaurus)) {
            return Response.ok().entity(ComTaskInfo.from(getAvailableComTasksExcludingAlreadyAssigned(comSchedule))).build();
        } else {
            return Response.ok().entity(ComTaskInfo.from(comSchedule.getComTasks())).build();
        }
    }

    @PUT
    @Path("/preview")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response generatePreviewForSchedule(PreviewInfo previewInfo) {
        if (previewInfo.temporalExpression==null) {
            throw new LocalizedFieldValidationException(thesaurus, MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression");
        }
        if (previewInfo.temporalExpression.every==null) {
            throw new LocalizedFieldValidationException(thesaurus, MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression.every");
        }
        previewInfo.nextOccurrences = calculateNextOccurrences(previewInfo);
        return Response.ok().entity(previewInfo).build();
    }

    private List<Date> calculateNextOccurrences(PreviewInfo previewInfo) {
        TemporalExpression temporalExpression = previewInfo.temporalExpression.asTemporalExpression();
        List<Date> nextOccurrences = new ArrayList<>();
        Date occurrence = previewInfo.startDate==null ? new Date() : previewInfo.startDate;
        Calendar latestOccurrence = Calendar.getInstance();
        for (int i=0; i<5; i++) {
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
        return this.deviceDataService.isLinkedToDevices(comSchedule);
    }

}
