package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/17/15.
 */
@Path("/categories")
public class DeviceMessageCategoryResource {

    private final DeviceMessageCategoryInfoFactory deviceMessageCategoriesInfoFactory;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageCategoryResource(DeviceMessageCategoryInfoFactory deviceMessageCategoriesInfoFactory, DeviceMessageSpecificationService deviceMessageSpecificationService, ExceptionFactory exceptionFactory) {
        this.deviceMessageCategoriesInfoFactory = deviceMessageCategoriesInfoFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{messageCategoryId}")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public DeviceMessageCategoryInfo getDeviceMessageCategory(@PathParam("messageCategoryId") int messageCategoryId,
                                  @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return deviceMessageSpecificationService.findCategoryById(messageCategoryId)
                .map(dmc -> deviceMessageCategoriesInfoFactory.asHypermedia(dmc, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_MESSAGE_CATEGORY));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public PagedInfoList<DeviceMessageCategoryInfo> getDeviceMessageCategories(@PathParam("comTaskId") long comTaskId,
                                  @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo,
                                  @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceMessageCategoryInfo> infos = ListPager.of(deviceMessageSpecificationService.allCategories(), (cat1, cat2) -> cat1.getName().compareToIgnoreCase(cat2.getName()))
                .from(queryParameters)
                .stream()
                .map(dmc -> deviceMessageCategoriesInfoFactory.asHypermedia(dmc, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageCategoryResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceMessageCategoriesInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
