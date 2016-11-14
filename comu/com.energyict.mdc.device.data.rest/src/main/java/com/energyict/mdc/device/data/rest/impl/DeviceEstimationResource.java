package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceEstimationResource {
    
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceEstimationResource(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }
    
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE})
    public PagedInfoList getEstimationRuleSetsForDevice(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<DeviceEstimationRuleSetRefInfo> infos = device.forEstimation().getEstimationRuleSetActivations()
                .stream()
                .map(rs -> new DeviceEstimationRuleSetRefInfo(rs, device))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimationRuleSets", infos, queryParameters);
    }

    @PUT @Transactional
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE)
    @DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
    public Response toggleEstimationRuleSetActivation(@PathParam("name") String name, @PathParam("ruleSetId") long ruleSetId, DeviceEstimationRuleSetRefInfo info) {
        info.id = ruleSetId;
        EstimationRuleSet estimationRuleSet = resourceHelper.lockEstimationRuleSetOrThrowException(info);
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        if (info.active) {
            device.forEstimation().activateEstimationRuleSet(estimationRuleSet);
        } else {
            device.forEstimation().deactivateEstimationRuleSet(estimationRuleSet);
        }
        return Response.ok().build();
    }

    @PUT @Transactional
    @Path("/esimationstatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response toggleEstimationActivationForDevice(@PathParam("name") String name, DeviceInfo info) {
        Device device = resourceHelper.lockDeviceOrThrowException(info);
        if (info.estimationStatus != null) {
            updateEstimationStatus(info.estimationStatus, device);
        }
        return Response.ok().build();
    }

    private void updateEstimationStatus(DeviceEstimationStatusInfo info, Device device) {
        if (info.active) {
            device.forEstimation().activateEstimation();
        } else {
            device.forEstimation().deactivateEstimation();
        }
    }
}
