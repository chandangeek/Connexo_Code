package com.energyict.mdc.device.data.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

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

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.DeviceService;

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
    @RolesAllowed({Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.VIEW_ESTIMATION_CONFIGURATION, Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE})
    public PagedInfoList getEstimationRuleSetsForDevice(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<DeviceEstimationRuleSetActivation> pagedRuleSets = ListPager.of(device.forEstimation().getEstimationRuleSetActivations()).from(queryParameters).find();
        List<DeviceEstimationRuleSetRefInfo> infos = pagedRuleSets.stream().map(rs -> new DeviceEstimationRuleSetRefInfo(rs, device)).collect(Collectors.toList());
        return PagedInfoList.asJson("estimationRuleSets", infos, queryParameters);
    }

    @PUT
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE)
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
}
