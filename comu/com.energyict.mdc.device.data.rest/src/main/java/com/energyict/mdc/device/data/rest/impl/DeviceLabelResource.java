package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.favorites.LabelCategory;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeviceLabelResource {

    private final ResourceHelper resourceHelper;
    private final DeviceService deviceService;
    private final FavoritesService favoritesService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceLabelResource(ResourceHelper resourceHelper, DeviceService deviceService, FavoritesService favoritesService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.deviceService = deviceService;
        this.favoritesService = favoritesService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public PagedInfoList getDeviceLabels(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        User user = (User) securityContext.getUserPrincipal();
        List<DeviceLabel> deviceLabels = favoritesService.getDeviceLabels(device, user);
        return PagedInfoList.fromPagedList("deviceLabels", deviceLabels.stream().map(dl -> new DeviceLabelInfo(dl, thesaurus)).collect(Collectors.toList()), queryParameters);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response createDeviceLabel(@PathParam("name") String name, DeviceLabelInfo info, @Context SecurityContext securityContext) {
        Device device = deviceService.findAndLockDeviceByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(buildCreateFlagConflictException(name, info));
        User user = (User) securityContext.getUserPrincipal();
        LabelCategory category = findLabelCategoryOrThrowException(info.category.id.toString());
        favoritesService.findDeviceLabel(device, user, category).ifPresent(label -> {
            throw buildCreateFlagConflictException(name, info).get();
        });
        DeviceLabel deviceLabel = favoritesService.findOrCreateDeviceLabel(device, user, category, info.comment);
        return Response.ok(new DeviceLabelInfo(deviceLabel, thesaurus)).build();
    }

    private Supplier<ConcurrentModificationException> buildCreateFlagConflictException(String deviceName, DeviceLabelInfo info) {
        return conflictFactory.conflict()
                .withActualParent(() -> resourceHelper.getCurrentDeviceVersion(deviceName), info.parent.id)
                .withMessageTitle(MessageSeeds.FLAG_DEVICE_CONCURRENT_TITLE, deviceName)
                .withMessageBody(MessageSeeds.FLAG_DEVICE_CONCURRENT_BODY, deviceName)
                .supplier();
    }

    @Path("/{categoryId}")
    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response deleteDeviceLabel(@PathParam("name") String name, @PathParam("categoryId") String categoryId, @Context SecurityContext securityContext, DeviceLabelInfo info) {
        Device device = deviceService.findAndLockDeviceByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(buildRemoveFlagConflictException(name, info));
        User user = (User) securityContext.getUserPrincipal();
        LabelCategory category = findLabelCategoryOrThrowException(categoryId);
        favoritesService.findDeviceLabel(device, user, category).ifPresent(favoritesService::removeDeviceLabel);
        return Response.ok().build();
    }

    private Supplier<ConcurrentModificationException> buildRemoveFlagConflictException(String deviceName, DeviceLabelInfo info) {
        return conflictFactory.conflict()
                .withActualParent(() -> resourceHelper.getCurrentDeviceVersion(deviceName), info.parent.id)
                .withMessageTitle(MessageSeeds.REMOVE_FLAG_DEVICE_CONCURRENT_TITLE, deviceName)
                .withMessageBody(MessageSeeds.FLAG_DEVICE_CONCURRENT_BODY, deviceName)
                .supplier();
    }

    private LabelCategory findLabelCategoryOrThrowException(String categoryId) {
        Optional<LabelCategory> category = favoritesService.findLabelCategory(categoryId);
        return category.orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_LABEL_CATEGORY));
    }
}
