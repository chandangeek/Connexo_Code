package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.rest.util.KorePagedInfoList;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.DeviceService;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceEstimationResource {
    
    private final ResourceHelper resourceHelper;
    private final DeviceService deviceService;
    private final EstimationService estimationService;
    
    @Inject
    public DeviceEstimationResource(ResourceHelper resourceHelper, DeviceService deviceService, EstimationService estimationService) {
        this.resourceHelper = resourceHelper;
        this.deviceService = deviceService;
        this.estimationService = estimationService;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE})
    public KorePagedInfoList getEstimationRuleSetsForDevice(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<DeviceEstimationRuleSetActivation> pagedRuleSets = ListPager.of(device.forEstimation().getEstimationRuleSetActivations()).from(queryParameters).find();
        List<DeviceEstimationRuleSetRefInfo> infos = pagedRuleSets.stream().map(rs -> new DeviceEstimationRuleSetRefInfo(rs, device)).collect(Collectors.toList());
        return KorePagedInfoList.asJson("estimationRuleSets", infos, queryParameters);
    }

    @PUT
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE)
    @DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
    public Response toggleEstimationRuleSetActivation(@PathParam("mRID") String mRID, @PathParam("ruleSetId") long ruleSetId, DeviceEstimationRuleSetRefInfo info) {
        Device device = deviceService.findAndLockDeviceByIdAndVersion(info.parent.id, info.parent.version).orElseThrow(() -> new WebApplicationException(Status.CONFLICT));
        EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Status.NOT_FOUND));
        if (info.active) {
            device.forEstimation().activateEstimationRuleSet(estimationRuleSet);
        } else {
            device.forEstimation().deactivateEstimationRuleSet(estimationRuleSet);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/esimationstatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response toggleEstimationActivationForDevice(@PathParam("mRID") String mrid, DeviceInfo info) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        resourceHelper.findDeviceAndLock(device.getId(), info.version);
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
