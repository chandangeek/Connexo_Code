package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.rest.ConnectionTaskLifecycleStatusAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DashboardFieldResource extends FieldResource {

    private static final Comparator<HasName> BY_NAME_COMPARATOR = Comparator.comparing(HasName::getName, String.CASE_INSENSITIVE_ORDER);
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

    @GET @Transactional
    @Path("/breakdown")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getBreakdownValues() {
        return asJsonArrayObjectWithTranslation("breakdowns", "breakdown", BREAKDOWN_OPTION_ADAPTER.getClientSideValues());
    }

    @GET @Transactional
    @Path("/taskstatus")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getTaskStatusValues() {
        return asJsonArrayObjectWithTranslation("taskStatuses", "taskStatus", this.taskStatusClientSideValues());
    }

    private List<String> taskStatusClientSideValues() {
        return Stream.of(TaskStatus.values()).map(TaskStatus::name).collect(Collectors.toList());
    }
    @GET @Transactional
    @Path("/comsessionsuccessindicators")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getComSessionSuccessIndicatorValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", this.comSessionSuccessIndicatorClientSideValues());
    }

    private List<String> comSessionSuccessIndicatorClientSideValues() {
        return Stream.of(ComSessionSuccessIndicatorTranslationKeys.values()).map(ComSessionSuccessIndicatorTranslationKeys::getKey).collect(Collectors.toList());
    }

    @GET @Transactional
    @Path("/connectiontasksuccessindicators")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getConnectionTaskSuccessIndicatorValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", connectionTaskSuccessIndicatorClientSideValues());
    }

    private List<String> connectionTaskSuccessIndicatorClientSideValues() {
        return Stream.of(ConnectionTask.SuccessIndicator.values()).map(Enum::name).collect(Collectors.toList());
    }

    @GET @Transactional
    @Path("/lifecyclestatus")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getLifecycleStatus() {
        return asJsonArrayObjectWithTranslation("lifecycleStatuses", "lifecycleStatus", new ConnectionTaskLifecycleStatusAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/completioncodes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getCompletionCodes() {
        return asJsonArrayObjectWithTranslation("completionCodes", "completionCode", this.completionCodeClientSideValues());
    }

    private List<String> completionCodeClientSideValues() {
        return Stream.of(CompletionCode.values()).map(Enum::name).collect(Collectors.toList());
    }

    @GET @Transactional
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getDeviceTypes() {
        return Response.ok(asInfoMap("deviceTypes", deviceConfigurationService.findAllDeviceTypes().find())).build();
    }

    @GET @Transactional
    @Path("/comportpools")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getComPortPools() {
        return Response.ok(asInfoMap("comPortPools", engineConfigurationService.findAllComPortPools().stream().collect(Collectors.toList()))).build();
    }

    @GET @Transactional
    @Path("/comtasks")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getComTasks() {
        return Response.ok(asInfoMap("comTasks", taskService.findAllComTasks().find())).build();
    }

    @GET @Transactional
    @Path("/comschedules")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Object getComSchedules() {
        return Response.ok(asInfoMap("comSchedules", schedulingService.getAllSchedules())).build();
    }

    @GET @Transactional
    @Path("/connectiontypepluggableclasses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Object getConnectionTypeValues() {
        return Response.ok(asInfoMap("connectiontypepluggableclasses", protocolPluggableService.findAllConnectionTypePluggableClasses())).build();
    }

    private <H extends HasId & HasName> Map<String, List<IdWithNameInfo>> asInfoMap(String name, List<H> list) {
        Map<String, List<IdWithNameInfo>> map = new HashMap<>();
        map.put(name, list.stream().sorted(BY_NAME_COMPARATOR).map(IdWithNameInfo::new).collect(toList()));
        return map;
    }

}
