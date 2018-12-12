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
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.SecurityAccessor;
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

@Path("/devices/{mrid}/keyAccessors")
public class KeyAccessorResource {

    private final KeyAccessorInfoFactory keyAccessorInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceService deviceService;

    @Inject
    public KeyAccessorResource(DeviceService deviceService, ExceptionFactory exceptionFactory, KeyAccessorInfoFactory keyAccessorInfoFactory) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.keyAccessorInfoFactory = keyAccessorInfoFactory;
    }

    /**
     * Fetch all defined keyAccessors for a device.
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     *
     * @return Device information and links to related resources
     * @summary View all defined keyAccessors for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<KeyAccessorInfo> getKeyAccessors(@PathParam("mrid") String mrid, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));

        List<KeyAccessorInfo> infos = device.getSecurityAccessors().stream()
                .sorted(Comparator.comparing(accessor -> accessor.getKeyAccessorType().getName()))
                .map(accessor -> keyAccessorInfoFactory.from(accessor, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(KeyAccessorResource.class)
                .resolveTemplate("mrid", device.getmRID());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * View the contents of a keyAccessor for a device.
     *
     * @param name The name of the keyAccessor
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Device information and links to related resources
     * @summary View keyAccessor identified by name for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorName}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public KeyAccessorInfo getKeyAccessor(@PathParam("mrid") String mrid, @PathParam("keyAccessorName") String name, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor securityAccessor = getSecurityAccessor(name, device);
        return keyAccessorInfoFactory.from(securityAccessor, uriInfo, fieldSelection.getFields());
    }

    private SecurityAccessor getSecurityAccessor(String name, Device device) {
        return device.getSecurityAccessors().stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().getName().equals(name))
                .findAny()
                .orElseThrow(() -> device.getDeviceType().getSecurityAccessorTypes().stream().anyMatch(sat -> sat.getName().equals(name)) ?
                        exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSOR_FOR_DEVICE) :
                        exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
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
        return keyAccessorInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

