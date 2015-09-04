package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.favorites.LabelCategory;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.users.User;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceLabelResource {

    private final ResourceHelper resourceHelper;
    private final FavoritesService favoritesService;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceLabelResource(ResourceHelper resourceHelper, FavoritesService favoritesService, ExceptionFactory exceptionFactory, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.favoritesService = favoritesService;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public PagedInfoList getDeviceLabels(@PathParam("mRID") String id, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        User user = (User) securityContext.getUserPrincipal();
        List<DeviceLabel> deviceLabels = favoritesService.getDeviceLabels(device, user);
        return PagedInfoList.fromPagedList("deviceLabels", deviceLabels.stream().map(dl -> new DeviceLabelInfo(dl, thesaurus)).collect(Collectors.toList()), queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response createDeviceLabel(@PathParam("mRID") String id, DeviceLabelInfo deviceLabelInfo, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        User user = (User) securityContext.getUserPrincipal();
        LabelCategory category = findLabelCategoryOrThrowException(deviceLabelInfo.category.id.toString());
        DeviceLabel deviceLabel = favoritesService.findOrCreateDeviceLabel(device, user, category, deviceLabelInfo.comment);
        return Response.ok(new DeviceLabelInfo(deviceLabel, thesaurus)).build();
    }

    @Path("/{categoryId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response deleteDeviceLabel(@PathParam("mRID") String id, @PathParam("categoryId") String categoryId, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
        User user = (User) securityContext.getUserPrincipal();
        LabelCategory category = findLabelCategoryOrThrowException(categoryId);
        Optional<DeviceLabel> deviceLabel = favoritesService.findDeviceLabel(device, user, category);
        if (!deviceLabel.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_LABEL, thesaurus.getString(category.getName(), category.getName()), device.getmRID());
        }
        favoritesService.removeDeviceLabel(deviceLabel.get());
        return Response.ok().build();
    }

    private LabelCategory findLabelCategoryOrThrowException(String categoryId) {
        Optional<LabelCategory> category = favoritesService.findLabelCategory(categoryId);
        return category.orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_LABEL_CATEGORY));
    }

}