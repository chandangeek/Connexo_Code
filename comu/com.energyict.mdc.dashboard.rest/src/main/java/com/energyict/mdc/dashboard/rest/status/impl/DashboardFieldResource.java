package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.rest.ComSessionSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.CompletionCodeAdapter;
import com.energyict.mdc.device.data.rest.ConnectionTaskLifecycleStatusAdapter;
import com.energyict.mdc.device.data.rest.ConnectionTaskSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DashboardFieldResource extends FieldResource {

    private static final Comparator<HasName> byNameComparator = (HasName d1, HasName d2) -> d1.getName().compareToIgnoreCase(d2.getName());
    private static final BreakdownOptionAdapter BREAKDOWN_OPTION_ADAPTER = new BreakdownOptionAdapter();
    private final DeviceConfigurationService deviceConfigurationService;
    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final TaskService taskService;
    private final SchedulingService schedulingService;

    @Inject
    public DashboardFieldResource(NlsService nlsService, DeviceConfigurationService deviceConfigurationService, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, TaskService taskService, SchedulingService schedulingService) {
        super(nlsService.getThesaurus(DashboardApplication.COMPONENT_NAME, Layer.REST));
        this.deviceConfigurationService = deviceConfigurationService;
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.taskService = taskService;
        this.schedulingService = schedulingService;
    }

    @GET
    @Path("/breakdown")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getBreakdownValues() {
        return asJsonArrayObjectWithTranslation("breakdowns", "breakdown", BREAKDOWN_OPTION_ADAPTER.getClientSideValues());
    }

    @GET
    @Path("/taskstatus")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getTaskStatusValues() {
        return asJsonArrayObjectWithTranslation("taskStatuses", "taskStatus", new TaskStatusAdapter().getClientSideValues());
    }

    @GET
    @Path("/comsessionsuccessindicators")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getComSessionSuccessIndicatorValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", new ComSessionSuccessIndicatorAdapter().getClientSideValues());
    }

    @GET
    @Path("/connectiontasksuccessindicators")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getConnectionTaskSuccessIndicatorValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", new ConnectionTaskSuccessIndicatorAdapter().getClientSideValues());
    }

    @GET
    @Path("/lifecyclestatus")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getLifecycleStatus() {
        return asJsonArrayObjectWithTranslation("lifecycleStatuses", "lifecycleStatus", new ConnectionTaskLifecycleStatusAdapter().getClientSideValues());
    }

    @GET
    @Path("/completioncodes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getCompletionCodes() {
        return asJsonArrayObjectWithTranslation("completionCodes", "completionCode", new CompletionCodeAdapter().getClientSideValues());
    }

    @GET
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getDeviceTypes() {
        return Response.ok(asInfoMap("deviceTypes", deviceConfigurationService.findAllDeviceTypes().find())).build();
    }

    @GET
    @Path("/comportpools")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getComPortPools() {
        return Response.ok(asInfoMap("comPortPools", engineConfigurationService.findAllComPortPools().stream().collect(Collectors.toList()))).build();
    }

    @GET
    @Path("/comtasks")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getComTasks() {
        return Response.ok(asInfoMap("comTasks", taskService.findAllComTasks().find())).build();
    }

    @GET
    @Path("/comschedules")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Object getComSchedules() {
        return Response.ok(asInfoMap("comSchedules", schedulingService.findAllSchedules())).build();
    }

    @GET
    @Path("/connectiontypepluggableclasses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getConnectionTypeValues() {
        return Response.ok(asInfoMap("connectiontypepluggableclasses", protocolPluggableService.findAllConnectionTypePluggableClasses())).build();
    }

    private <H extends HasId & HasName> Map<String, List<IdWithNameInfo>> asInfoMap(String name, List<H> list) {
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        map.put(name, list.stream().sorted(byNameComparator).map(IdWithNameInfo::new).collect(toList()));
        return map;
    }
}
