package com.energyict.mdc.device.data.rest.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.favorites.DeviceLabel;
import com.elster.jupiter.favorites.FavoritesService;
import com.elster.jupiter.favorites.LabelCategory;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;
import com.energyict.mdc.device.data.security.Privileges;

public class DeviceLabelResource {
    
    private final MeteringService meteringService;
    private final ResourceHelper resourceHelper;
    private final FavoritesService favoritesService;
    private final ExceptionFactory exceptionFactory;
    
    @Inject
    public DeviceLabelResource(ResourceHelper resourceHelper, FavoritesService favoritesService, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.favoritesService = favoritesService;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getDeviceLabels(@PathParam("mRID") String id, @BeanParam QueryParameters queryParameters, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        User user = (User) securityContext.getUserPrincipal();
        EndDevice endDevice = findEndDeviceOrThrowException(device);
        List<DeviceLabel> deviceLabels = favoritesService.getDeviceLabels(endDevice, user);
        return PagedInfoList.asJson("deviceLabels", deviceLabels.stream().map(DeviceLabelInfo::new).collect(Collectors.toList()), queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response createDeviceLabel(@PathParam("mRID") String id, DeviceLabelInfo deviceLabelInfo, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        User user = (User) securityContext.getUserPrincipal();
        EndDevice endDevice = findEndDeviceOrThrowException(device);
        LabelCategory category = findLabelCategoryOrThrowException(deviceLabelInfo.category.id.toString());
        DeviceLabel deviceLabel = favoritesService.findOrCreateDeviceLabel(endDevice, user, category, deviceLabelInfo.comment);
        return Response.ok(new DeviceLabelInfo(deviceLabel)).build();
    }

    @Path("/{categoryId}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response deleteDeviceLabel(@PathParam("mRID") String id, @PathParam("categoryId") String categoryId, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        User user = (User) securityContext.getUserPrincipal();
        EndDevice endDevice = findEndDeviceOrThrowException(device);
        LabelCategory category = findLabelCategoryOrThrowException(categoryId);
        Optional<DeviceLabel> deviceLabel = favoritesService.findDeviceLabel(endDevice, user, category);
        if (!deviceLabel.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_LABEL, category.getTranlatedName());
        }
        favoritesService.removeDeviceLabel(deviceLabel.get());
        return Response.ok().build();
    }

    private LabelCategory findLabelCategoryOrThrowException(String categoryId) {
        Optional<LabelCategory> category = favoritesService.findLabelCategory(categoryId);
        return category.orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_LABEL_CATEGORY));
    }

    private EndDevice findEndDeviceOrThrowException(Device device) {
        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter(String.valueOf(device.getId()));
        return meter.orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, device.getmRID()));
    }
}
