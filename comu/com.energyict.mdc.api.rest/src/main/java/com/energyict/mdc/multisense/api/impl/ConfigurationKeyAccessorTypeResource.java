/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;


import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
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
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devicetypes/{deviceTypeId}/keyAccessorTypes")
public class ConfigurationKeyAccessorTypeResource {

    private final KeyAccessorTypeInfoFactory keyAccessorTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ConfigurationKeyAccessorTypeResource(DeviceConfigurationService deviceConfigurationService, ExceptionFactory exceptionFactory, KeyAccessorTypeInfoFactory keyAccessorTypeInfoFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.exceptionFactory = exceptionFactory;
        this.keyAccessorTypeInfoFactory = keyAccessorTypeInfoFactory;
    }

    /**
     * Fetch all defined keyAccessorTypes for a device type.
     *
     * @param deviceTypeId   Id of the device type
     * @param uriInfo        uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     *
     * @return Device information and links to related resources
     * @summary View all defined keyAccessortypes for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<KeyAccessorTypeInfo> getKeyAccessorTypes(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType devicetype = deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE_TYPE));

        List<KeyAccessorTypeInfo> infos = devicetype.getSecurityAccessorTypes().stream()
                .sorted(Comparator.comparing(SecurityAccessorType::getName))
                .map(accessor -> keyAccessorTypeInfoFactory.from(accessor, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ConfigurationKeyAccessorTypeResource.class)
                .resolveTemplate("deviceTypeId", devicetype.getId());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * View the contents of a keyAccessorType for a device type.
     *
     * @param deviceTypeId   Id of the device type
     * @param name           The name of the keyAccessorType
     * @param uriInfo        uriInfo
     * @param fieldSelection field selection
     * @return Device information and links to related resources
     * @summary View keyAccessortype identified by name for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorTypeName}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public KeyAccessorTypeInfo getKeyAccessorType(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("keyAccessorTypeName") String name, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceType devicetype = deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE_TYPE));
        SecurityAccessorType securityAccessorType = getKeyAccessorType(name, devicetype);
        return keyAccessorTypeInfoFactory.from(securityAccessorType, uriInfo, fieldSelection.getFields());
    }

    private SecurityAccessorType getKeyAccessorType(String name, DeviceType devicetype) {
        return devicetype.getSecurityAccessorTypes().stream().filter(keyAccessorType1 -> keyAccessorType1.getName().equals(name)).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
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
        return keyAccessorTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

