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
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.RequestSecurityLevel;

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
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("pluggableclasses/{deviceProtocolPluggableClassId}/requestsecurityaccesslevels")
public class RequestSecurityDeviceAccessLevelResource {

    private final RequestSecurityDeviceAccessLevelInfoFactory requestSecurityDeviceAccessLevelInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public RequestSecurityDeviceAccessLevelResource(RequestSecurityDeviceAccessLevelInfoFactory requestSecurityDeviceAccessLevelInfoFactory, ProtocolPluggableService protocolPluggableService, ExceptionFactory exceptionFactory) {
        this.requestSecurityDeviceAccessLevelInfoFactory = requestSecurityDeviceAccessLevelInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * The request security device access level is identified by both the id of the pluggable class and the applicable request security access level id
     *
     * @param deviceProtocolPluggableClassId id of the device protocol pluggable class
     * @param requestSecurityDeviceAccessLevelId id of the request security access level
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified request security access level
     * @summary Get a certain request security access level.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{requestSecurityDeviceAccessLevelId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceAccessLevelInfo getRequestSecurityDeviceAccessLevel(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("requestSecurityDeviceAccessLevelId") long requestSecurityDeviceAccessLevelId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        com.energyict.mdc.upl.DeviceProtocol actualDeviceProtocol = getActualDeviceProtocol(pluggableClass.getDeviceProtocol());
        if (actualDeviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities) {
            return ((AdvancedDeviceProtocolSecurityCapabilities) actualDeviceProtocol).getRequestSecurityLevels()
                    .stream()
                    .filter(lvl -> lvl.getId() == requestSecurityDeviceAccessLevelId)
                    .findFirst()
                    .map(this.protocolPluggableService::adapt)
                    .map(lvl -> requestSecurityDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_REQUEST_SECURITY_DEVICE_ACCESS_LEVEL));
        } else {
            throw exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_REQUEST_SECURITY_DEVICE_ACCESS_LEVEL).get();
        }
    }

    /**
     * Get a list of al known request security levels for a certain device protocol. The device protocol is
     * identified through the pluggable class that wraps it.
     *
     * @param deviceProtocolPluggableClassId The ID of the device protocol pluggable class
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return List of request security device access levels. Paged if paging parameters were provided in the call.
     * @summary Get a list of all known request security device access levels.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceAccessLevelInfo> getRequestSecurityDeviceAccessLevels(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection,
            @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        com.energyict.mdc.upl.DeviceProtocol actualDeviceProtocol = getActualDeviceProtocol(pluggableClass.getDeviceProtocol());
        List<RequestSecurityLevel> requestSecurityLevels = actualDeviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities
                ? ((AdvancedDeviceProtocolSecurityCapabilities) actualDeviceProtocol).getRequestSecurityLevels()
                : Collections.emptyList();
        List<DeviceAccessLevelInfo> infos = ListPager.of(requestSecurityLevels).from(queryParameters)
                .stream()
                .map(this.protocolPluggableService::adapt)
                .map(lvl -> requestSecurityDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(RequestSecurityDeviceAccessLevelResource.class)
                .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClassId);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    private com.energyict.mdc.upl.DeviceProtocol getActualDeviceProtocol(DeviceProtocol deviceProtocol) {
        return deviceProtocol instanceof UPLProtocolAdapter
                ? (com.energyict.mdc.upl.DeviceProtocol) ((UPLProtocolAdapter) deviceProtocol).getActual()
                : deviceProtocol;
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
        return requestSecurityDeviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
