package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
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
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devicemessagecategories/{categoryId}/devicemessagespecifications")
public class DeviceMessageSpecificationResource {

    private final DeviceMessageSpecificationInfoFactory deviceMessageSpecificationInfoFactory;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageSpecificationResource(DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceMessageSpecificationInfoFactory deviceMessageSpecificationInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceMessageSpecificationInfoFactory = deviceMessageSpecificationInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{deviceMessageSpecificationId}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public DeviceMessageSpecificationInfo getDeviceMessageSpecification(@PathParam("categoryId") long deviceMessageCategoryId,
                                                                        @PathParam("deviceMessageSpecificationId") long deviceMessageSpecificationId,
                                                                        @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService.findCategoryById((int) deviceMessageCategoryId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_MESSAGE_CATEGORY))
                .getMessageSpecifications().stream()
                .filter(spec -> spec.getId().dbValue() == deviceMessageSpecificationId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_MESSAGE_SPEC));
        return deviceMessageSpecificationInfoFactory.from(deviceMessageSpec, uriInfo, fieldSelection.getFields());
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<DeviceMessageSpecificationInfo> getDeviceMessageSpecifications(
            @PathParam("categoryId") long deviceMessageCategoryId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceMessageSpecificationInfo> infos = deviceMessageSpecificationService.findCategoryById((int) deviceMessageCategoryId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_MESSAGE_CATEGORY))
                .getMessageSpecifications().stream()
                .sorted(Comparator.comparingLong(spec -> spec.getId().dbValue()))
                .map(spec -> deviceMessageSpecificationInfoFactory.from(spec, uriInfo, fieldSelection.getFields())).collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageSpecificationResource.class)
		        .resolveTemplate("categoryId", deviceMessageCategoryId);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageSpecificationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
