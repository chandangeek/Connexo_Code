package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DashboardFieldResource extends FieldResource {


    private static final BreakdownOptionAdapter BREAKDOWN_OPTION_ADAPTER = new BreakdownOptionAdapter();
    private final DeviceConfigurationService deviceConfigurationService;
    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;
    private final TaskService taskService;
    private final SchedulingService schedulingService;

    @Inject
    public DashboardFieldResource(NlsService nlsService, DeviceConfigurationService deviceConfigurationService, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, TaskService taskService, SchedulingService schedulingService) {
        super(nlsService.getThesaurus(DashboardApplication.COMPONENT_NAME, Layer.REST));
        this.deviceConfigurationService = deviceConfigurationService;
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.taskService = taskService;
        this.schedulingService = schedulingService;
    }

    @GET
    @Path("/breakdown")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getBreakdownValues() {
        return asJsonArrayObjectWithTranslation("breakdowns", "breakdown", BREAKDOWN_OPTION_ADAPTER.getClientSideValues());
    }

    @GET
    @Path("/taskstatus")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getTaskStatusValues() {
        return asJsonArrayObjectWithTranslation("taskStatuses", "taskStatus", new TaskStatusAdapter().getClientSideValues());
    }

    @GET
    @Path("/comsessionsuccessindicators")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getComSessionSuccessIndicatorValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", new ComSessionSuccessIndicatorAdapter().getClientSideValues());
    }

    @GET
    @Path("/connectiontasksuccessindicators")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getConnectionTaskSuccessIndicatorValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", new ConnectionTaskSuccessIndicatorAdapter().getClientSideValues());
    }

    @GET
    @Path("/lifecyclestatus")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getLifecycleStatus() {
        return asJsonArrayObjectWithTranslation("lifecycleStatuses", "lifecycleStatus", new ConnectionTaskLifecycleStatusAdaptor().getClientSideValues());
    }

    @GET
    @Path("/completioncodes")
    @Produces("application/json")
    public Object getCompletionCodes() {
        return asJsonArrayObjectWithTranslation("completionCodes", "completionCode", new CompletionCodeAdapter().getClientSideValues());
    }

    @GET
    @Path("/devicetypes")
    @Produces("application/json")
    public Object getDeviceTypes() {
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        List<IdWithNameInfo> infos = new ArrayList<>();
        map.put("deviceTypes", infos);
        for (DeviceType deviceType : deviceConfigurationService.findAllDeviceTypes().find()) {
            infos.add(new IdWithNameInfo(deviceType));
        }
        return map;
    }

    @GET
    @Path("/comportpools")
    @Produces("application/json")
    public Object getComPortPools() {
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        List<IdWithNameInfo> infos = new ArrayList<>();
        map.put("comPortPools", infos);
        for (ComPortPool comPortPool : engineModelService.findAllComPortPools()) {
            infos.add(new IdWithNameInfo(comPortPool));
        }
        return map;
    }

    @GET
    @Path("/comtasks")
    @Produces("application/json")
    public Object getComTasks() {
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        List<IdWithNameInfo> infos = new ArrayList<>();
        map.put("comTasks", infos);
        for (ComTask comTask : taskService.findAllComTasks()) {
            infos.add(new IdWithNameInfo(comTask));
        }
        return map;
    }

    @GET
    @Path("/comschedules")
    @Produces("application/json")
    public Object getComSchedules() {
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        List<IdWithNameInfo> infos = new ArrayList<>();
        map.put("comSchedules", infos);
        for (ComSchedule comSchedule : schedulingService.findAllSchedules()) {
            infos.add(new IdWithNameInfo(comSchedule));
        }
        return map;
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces("application/json")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getConnectionTypeValues() {
        List<IdWithNameInfo> names = new ArrayList<>();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : protocolPluggableService.findAllConnectionTypePluggableClasses()) {
            names.add(new IdWithNameInfo(connectionTypePluggableClass.getId(), connectionTypePluggableClass.getName()));
        }
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        map.put("connectiontypepluggableclasses", names);
        return Response.ok(map).build();
    }

}
