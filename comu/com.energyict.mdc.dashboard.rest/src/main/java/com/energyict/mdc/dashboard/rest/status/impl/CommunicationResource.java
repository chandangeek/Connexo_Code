package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.SchedulingService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/communications")
public class CommunicationResource {

    private static final Comparator<ComTaskExecution> COM_TASK_EXECUTION_COMPARATOR = new ComTaskExecutionComparator();

    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();

    private static final String TASK_STATUSES = "currentStates";
    private static final String COM_SCHEDULES = "comSchedules";
    private static final String COM_TASKS = "comTasks";
    private static final String DEVICE_TYPES = "deviceTypes";
    private static final String START_INTERVAL_FROM = "startIntervalFrom";
    private static final String START_INTERVAL_TO = "startIntervalTo";
    private static final String FINISH_INTERVAL_FROM = "finishIntervalFrom";
    private static final String FINISH_INTERVAL_TO = "finishIntervalTo";

    private final Thesaurus thesaurus;
    private final DeviceDataService deviceDataService;
    private final SchedulingService schedulingService;

    @Inject
    public CommunicationResource(Thesaurus thesaurus, DeviceDataService deviceDataService, SchedulingService schedulingService) {
        this.thesaurus = thesaurus;
        this.deviceDataService = deviceDataService;
        this.schedulingService = schedulingService;
    }

    @GET
    @Consumes("application/json")
    public Response getCommunications(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters) throws Exception {
        ComTaskExecutionFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
        if (queryParameters.getStart()==null || queryParameters.getLimit()==null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<ComTaskExecution> communicationTasksByFilter = deviceDataService.findComTaskExecutionsByFilter(filter, queryParameters.getStart(), queryParameters.getLimit() + 1);
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(communicationTasksByFilter.size());
        for (ComTaskExecution comTaskExecution : communicationTasksByFilter) {
            comTaskExecutionInfos.add(ComTaskExecutionInfo.from(comTaskExecution, thesaurus));
        }

        return Response.ok(PagedInfoList.asJson("communicationTasks", comTaskExecutionInfos, queryParameters)).build();
    }

    private ComTaskExecutionFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) throws Exception {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        Map<String, String> filterProperties = jsonQueryFilter.getFilterProperties();
        if (filterProperties.containsKey(TASK_STATUSES)) {
            String[] taskStatuses = filterProperties.get(TASK_STATUSES).split(",");
            for (String taskStatus : taskStatuses) {
                filter.taskStatuses.add(TASK_STATUS_ADAPTER.unmarshal(taskStatus));
            }
        }

        filter.comSchedules = new HashSet<>();
        if (filterProperties.containsKey(COM_SCHEDULES)) {
            String[] comScheduleIds = filterProperties.get(COM_SCHEDULES).split(",");
            filter.comSchedules.addAll(getObjectsByIdFromList(comScheduleIds, schedulingService.findAllSchedules()));
        }

        if (filterProperties.containsKey(START_INTERVAL_FROM) || filterProperties.containsKey(START_INTERVAL_TO)) {
            Date start=null;
            Date end=null;
            if (filterProperties.containsKey(START_INTERVAL_FROM)) {
                start=new Date(Long.parseLong(filterProperties.get(START_INTERVAL_FROM)));
            }
            if (filterProperties.containsKey(START_INTERVAL_TO)) {
                end=new Date(Long.parseLong(filterProperties.get(START_INTERVAL_TO)));
            }
            filter.lastSessionStart=new Interval(start, end);
        }

        if (filterProperties.containsKey(FINISH_INTERVAL_FROM) || filterProperties.containsKey(FINISH_INTERVAL_TO)) {
            Date start=null;
            Date end=null;
            if (filterProperties.containsKey(FINISH_INTERVAL_FROM)) {
                start=new Date(Long.parseLong(filterProperties.get(FINISH_INTERVAL_FROM)));
            }
            if (filterProperties.containsKey(FINISH_INTERVAL_TO)) {
                end=new Date(Long.parseLong(filterProperties.get(FINISH_INTERVAL_TO)));
            }
            filter.lastSessionEnd=new Interval(start, end);
        }



        return filter;
    }

    private <H extends HasId> Collection<H> getObjectsByIdFromList(String[] ids, List<H> objects) {
        ArrayList<H> selectedObjects = new ArrayList<H>(ids.length);
        for (H object : objects) {
            String objectIdString = ""+object.getId();
            for (String id : ids) {
                if (objectIdString.equals(id)) {
                    selectedObjects.add(object);
                }
            }
        }
        return selectedObjects;
    }

}
