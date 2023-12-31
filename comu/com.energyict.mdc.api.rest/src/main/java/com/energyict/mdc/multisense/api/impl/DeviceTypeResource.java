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
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

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

@Path("/devicetypes")
public class DeviceTypeResource {

    private final RegisteredCustomPropertySetInfoFactory registeredCustomPropertySetInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceTypeInfoFactory deviceTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceTypeResource(RegisteredCustomPropertySetInfoFactory registeredCustomPropertySetInfoFactory,
                              DeviceConfigurationService deviceConfigurationService,
                              DeviceTypeInfoFactory deviceTypeInfoFactory,
                              ExceptionFactory exceptionFactory) {
        this.registeredCustomPropertySetInfoFactory = registeredCustomPropertySetInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceTypeInfoFactory = deviceTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * DeviceType defines the basic common attributes of a
     * physical (or virtual) device type.
     * Each physical device is an instance referring to
     * a specific DeviceType.
     *
     * @param id      Id of the device type
     * @param uriInfo uriInfo
     * @param fields  fields
     * @return Uniquely identified device type
     * @summary Fetch device type
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{deviceTypeId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceTypeInfo getDeviceType(@PathParam("deviceTypeId") long id, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return deviceConfigurationService.findDeviceType(id)
                .map(d -> deviceTypeInfoFactory.from(d, uriInfo, fields.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE));
    }

    /**
     * DeviceType defines the basic common attributes of a
     * physical (or virtual) device type.
     * Each physical device is an instance referring to
     * a specific DeviceType.
     *
     * @param uriInfo         uriInfo
     * @param queryParameters queryParameters
     * @param fields          fields
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     * @summary Fetch a set of device types
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceTypeInfo> getHypermediaDeviceTypes(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldSelection fields,
                                                                  @Context UriInfo uriInfo) {
        List<DeviceTypeInfo> infos = deviceConfigurationService.findAllDeviceTypes().from(queryParameters).stream().map(d -> deviceTypeInfoFactory.from(d, uriInfo, fields.getFields())).collect(toList());

        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class);
        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
    }

    /**
     * DeviceType defines custom attribute set of a
     * physical (or virtual) device type.
     * Each physical device is an instance referring to
     * a specific DeviceType.
     *
     * @param id      Id of the device type
     * @param cpsId   Id of the custom attribute set
     * @param uriInfo uriInfo
     * @param fields  fields
     * @return Uniquely identified registered custom property set
     * @summary Fetch registered custom property set
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{deviceTypeId}/custompropertysets/{cpsId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public RegisteredCustomPropertySetInfo getDeviceTypeCustomPropertySet(@PathParam("deviceTypeId") long id,
                                                                          @PathParam("cpsId") long cpsId,
                                                                          @BeanParam FieldSelection fields,
                                                                          @Context UriInfo uriInfo) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND,
                        MessageSeeds.NO_SUCH_DEVICE_TYPE));
        RegisteredCustomPropertySetTypeInfo registeredCustomPropertySetTypeInfo =
                deviceTypeInfoFactory.getRegisteredCustomPropertySetTypeInfos(deviceType,cpsId)
                        .stream()
                        .findFirst()
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND,
                                MessageSeeds.NO_SUCH_CUSTOM_PROPERTY_SET));
        return registeredCustomPropertySetInfoFactory
                .from(registeredCustomPropertySetTypeInfo, uriInfo, fields.getFields());
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
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}