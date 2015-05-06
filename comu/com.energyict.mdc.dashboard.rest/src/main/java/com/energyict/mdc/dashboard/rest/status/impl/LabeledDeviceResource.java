package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.JsonQueryParameters;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.favorites.LabelCategory;


@Path("/mylabeleddevices")
public class LabeledDeviceResource {
    
    private final FavoritesService favoritesService;
    private final ExceptionFactory exceptionFactory;
    
    @Inject
    public LabeledDeviceResource(FavoritesService favoritesService, ExceptionFactory exceptionFactory) {
        this.favoritesService = favoritesService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getLabeledDevices(@QueryParam("category") String categoryId, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext context) {
        if (Checks.is(categoryId).emptyOrOnlyWhiteSpace()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        User user = (User) context.getUserPrincipal();
        Optional<LabelCategory> category = favoritesService.findLabelCategory(categoryId);
        if (!category.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LABEL_CATEGORY, categoryId);
        }
        List<DeviceLabel> devices = favoritesService.getDeviceLabelsOfCategory(user, category.get());
        List<DeviceWithLabelInfo> infos = devices.stream()
                .map(DeviceWithLabelInfo::new)
                .sorted((d1, d2) -> d2.deviceLabelInfo.creationDate.compareTo(d1.deviceLabelInfo.creationDate))//descending order
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("myLabeledDevices", infos, queryParameters)).build();
    }
}
