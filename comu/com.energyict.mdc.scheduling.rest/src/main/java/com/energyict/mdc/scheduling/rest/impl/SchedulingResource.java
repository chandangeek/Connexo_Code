package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.BooleanAdapter;
import com.energyict.mdc.common.rest.IdListBuilder;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.security.Privileges;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final DeviceDataService deviceDataService;
    private final Clock clock;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;

    @Inject
    public SchedulingResource(SchedulingService schedulingService, DeviceDataService deviceDataService, Clock clock, DeviceConfigurationService deviceConfigurationService, TaskService taskService) {
        this.schedulingService = schedulingService;
        this.deviceDataService = deviceDataService;
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SCHEDULE)
    public PagedInfoList getSchedules(@BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        String mrid = queryFilter.getFilterProperties().get("mrid")!=null?queryFilter.<String>getProperty("mrid"):null;
        boolean available = queryFilter.getFilterProperties().get("available")!=null?queryFilter.<Boolean>getProperty("available"):false;
        List<ComSchedule> comSchedules = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(clock.getTimeZone());
        if(mrid!= null && available){
            Device device = deviceDataService.findByUniqueMrid(mrid);
            List<ComSchedule> possibleComSchedules = schedulingService.findAllSchedules(calendar).from(queryParameters).find();
            List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
            for(ComSchedule comSchedule:possibleComSchedules){
                if(isValidComSchedule(comSchedule, comTaskExecutions,device.getDeviceConfiguration().getComTaskEnablements())){
                    comSchedules.add(comSchedule);
                }
            }
        } else {
            comSchedules = schedulingService.findAllSchedules(calendar).from(queryParameters).find();
        }

        List<ComScheduleInfo> comScheduleInfos = new ArrayList<>();
        for (ComSchedule comSchedule : comSchedules) {
            comScheduleInfos.add(ComScheduleInfo.from(comSchedule, isInUse(comSchedule)));
        }
        return PagedInfoList.asJson("schedules", comScheduleInfos, queryParameters);
    }

    private boolean isValidComSchedule(ComSchedule comSchedule, List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        Set<Long> allowedComTaskIds = getAllowedComTaskIds(comTaskEnablements);
        Set<Long> alreadyAssignedComTaskIds = getAlreadyAssignedComTaskIds(comTaskExecutions);
        Set<Long> toBeVerifiedComTaskIds = new HashSet<>();
        if(!hasSameConfigurationSettingsInEnablements(comSchedule, comTaskEnablements)){
            return false;
        }
        for(ComTask comTaskFromSchedule : comSchedule.getComTasks()){
            if(alreadyAssignedComTaskIds.contains(comTaskFromSchedule.getId())) {
                return false;
            }
            toBeVerifiedComTaskIds.add(comTaskFromSchedule.getId());
        }
        return allowedComTaskIds.containsAll(toBeVerifiedComTaskIds);
    }

    private boolean hasSameConfigurationSettingsInEnablements(ComSchedule comSchedule, List<ComTaskEnablement> comTaskEnablements) {
        List<ComTaskEnablement> comTaskEnablementsToCheck = getComTaskEnablementsForComTasks(comSchedule, comTaskEnablements);
        if(comTaskEnablementsToCheck.size()==0){
            return false;
        }
        else if(comTaskEnablementsToCheck.size()==1){
           return true;
        } else {
            Long protocolDialectConfigurationPropertiesId = null;
            if(comTaskEnablementsToCheck.get(0).getProtocolDialectConfigurationProperties().isPresent()){
                protocolDialectConfigurationPropertiesId = comTaskEnablementsToCheck.get(0).getProtocolDialectConfigurationProperties().get().getId();
            }
            long securityPropertySetId = comTaskEnablementsToCheck.get(0).getSecurityPropertySet().getId();
            Long partialConnectionTaskId = null;
            if(comTaskEnablementsToCheck.get(0).getPartialConnectionTask().isPresent()){
                partialConnectionTaskId = comTaskEnablementsToCheck.get(0).getPartialConnectionTask().get().getId();
            }
            int priority = comTaskEnablementsToCheck.get(0).getPriority();
            for(int i = 1;i< comTaskEnablementsToCheck.size();i++){

                Long compareProtocolDialectConfigurationPropertiesId = null;
                if(comTaskEnablementsToCheck.get(i).getProtocolDialectConfigurationProperties().isPresent()){
                    compareProtocolDialectConfigurationPropertiesId = comTaskEnablementsToCheck.get(i).getProtocolDialectConfigurationProperties().get().getId();
                }
                long compareSecurityPropertySetId = comTaskEnablementsToCheck.get(i).getSecurityPropertySet().getId();
                Long comparePartialConnectionTaskId = null;
                if(comTaskEnablementsToCheck.get(i).getPartialConnectionTask().isPresent()){
                    comparePartialConnectionTaskId = comTaskEnablementsToCheck.get(i).getPartialConnectionTask().get().getId();
                }
                int comparePriority = comTaskEnablementsToCheck.get(i).getPriority();
                if(!protocolDialectConfigurationPropertiesId.equals(compareProtocolDialectConfigurationPropertiesId) ||
                        securityPropertySetId!=compareSecurityPropertySetId ||
                        !partialConnectionTaskId.equals(comparePartialConnectionTaskId)||
                        priority!=comparePriority){
                    return false;
                }
            }
            return true;
        }

    }

    private List<ComTaskEnablement> getComTaskEnablementsForComTasks(ComSchedule comSchedule, List<ComTaskEnablement> comTaskEnablements) {
        List<ComTaskEnablement> comTaskEnablementsToCheck = new ArrayList<>();
        for(ComTask comTask:comSchedule.getComTasks()){
            for(ComTaskEnablement comTaskEnablement:comTaskEnablements){
                if(comTaskEnablement.getComTask().getId()==comTask.getId()){
                    comTaskEnablementsToCheck.add(comTaskEnablement);
                }
            }
        }
        return comTaskEnablementsToCheck;
    }

    private Set<Long> asIds(List<Optional<? extends HasId>> hasIdList) {
        Set<Long> idList = new HashSet<>();
        for (Optional<? extends HasId> hasId : hasIdList) {
            idList.add(hasId.get().getId());
        }
        return idList;
    }

    private Set<Long> getAllowedComTaskIds(List<ComTaskEnablement> comTaskEnablements) {
        Set<Long> allowedComTaskIds = new HashSet<>();
        for(ComTaskEnablement comTaskEnablement: comTaskEnablements){
            allowedComTaskIds.add(comTaskEnablement.getComTask().getId());
        }
        return allowedComTaskIds;
    }

    private Set<Long> getAlreadyAssignedComTaskIds(List<ComTaskExecution> comTaskExecutions) {
        Set<Long> alreadyAssignedComTaskIds = new HashSet<>();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            if(!comTaskExecution.isAdHoc()){
                for(ComTask comTask : comTaskExecution.getComTasks()){
                    alreadyAssignedComTaskIds.add(comTask.getId());
                }
            }
        }
        return alreadyAssignedComTaskIds;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SCHEDULE)
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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.CREATE_SCHEDULE)
    public Response createSchedule(ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleInfo.name, comScheduleInfo.temporalExpression.asTemporalExpression(),
                comScheduleInfo.startDate==null?null:new UtcInstant(comScheduleInfo.startDate)).mrid(comScheduleInfo.mRID).build();
        if(comScheduleInfo.comTaskUsages!=null){
            updateTasks(comSchedule, comScheduleInfo.comTaskUsages);
        }
        comSchedule.save();
        return Response.status(Response.Status.CREATED).entity(ComScheduleInfo.from(comSchedule, false)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.DELETE_SCHEDULE)
    public Response deleteSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        if (this.isInUse(comSchedule)) {
            comSchedule.makeObsolete();
        }
        else {
            comSchedule.delete();
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.UPDATE_SCHEDULE)
    public ComScheduleInfo updateSchedules(@PathParam("id") long id, ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        comSchedule.setName(comScheduleInfo.name);
        comSchedule.setTemporalExpression(comScheduleInfo.temporalExpression==null?null:comScheduleInfo.temporalExpression.asTemporalExpression());
        comSchedule.setStartDate(comScheduleInfo.startDate==null?null:new UtcInstant(comScheduleInfo.startDate));
        comSchedule.setmRID(comScheduleInfo.mRID);
        if (comScheduleInfo.comTaskUsages!=null) {
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
    @RolesAllowed(Privileges.VIEW_SCHEDULE)
    public Response getComTasks(@PathParam("id") long id, @BeanParam JsonQueryFilter queryFilter) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        if (queryFilter.getFilterProperties().containsKey("available") && queryFilter.getProperty("available", new BooleanAdapter())) {
            return Response.ok().entity(ComTaskInfo.from(getAvailableComTasksExcludingAlreadyAssigned(comSchedule))).build();
        } else {
            return Response.ok().entity(ComTaskInfo.from(comSchedule.getComTasks())).build();
        }
    }

    @PUT
    @Path("/preview")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SCHEDULE)
    public Response generatePreviewForSchedule(PreviewInfo previewInfo) {
        if (previewInfo.temporalExpression==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression");
        }
        if (previewInfo.temporalExpression.every==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression.every");
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
