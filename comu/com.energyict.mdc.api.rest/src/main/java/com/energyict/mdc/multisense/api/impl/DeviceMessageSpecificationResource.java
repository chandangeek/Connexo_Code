/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
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

    /**
     * Models the specification of a device message,
     * i.e. the description of all of the attributes of the DeviceMessage
     * and which of these attributes are required or optional.
     * A DeviceMessage can be standard, meaning that it is supported
     * off the shelve by the ComServer and was not added to the ComServer
     * for the purpose of a single customer installation.
     * Any DeviceMessageSpec that is created through the ComServer
     * API will by default be a non-standard DeviceMessage.
     * Note that non standard message can still be part
     * of standard device message categories
     *
     * @summary Get a device message specification
     *
     * @param deviceMessageCategoryId Id of the device message category
     * @param deviceMessageSpecificationId Id of the device message specification
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified device message specification
     */
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

    /**
     * Models the specification of a device message,
     * i.e. the description of all of the attributes of the DeviceMessage
     * and which of these attributes are required or optional.
     * A DeviceMessage can be standard, meaning that it is supported
     * off the shelve by the ComServer and was not added to the ComServer
     * for the purpose of a single customer installation.
     * Any DeviceMessageSpec that is created through the ComServer
     * API will by default be a non-standard DeviceMessage.
     * Note that non standard message can still be part
     * of standard device message category.
     *
     * @summary Get a set of device message specifications
     *
     * @param deviceMessageCategoryId Id of the device message category
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
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

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @summary List the fields available on this type of entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageSpecificationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
