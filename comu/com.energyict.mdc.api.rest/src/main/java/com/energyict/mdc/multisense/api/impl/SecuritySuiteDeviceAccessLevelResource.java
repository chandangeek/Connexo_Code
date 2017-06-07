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
import com.energyict.mdc.upl.security.SecuritySuite;

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

@Path("pluggableclasses/{deviceProtocolPluggableClassId}/securitysuites")
public class SecuritySuiteDeviceAccessLevelResource {

    private final SecuritySuiteDeviceAccessLevelInfoFactory securitySuiteDeviceAccessLevelInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public SecuritySuiteDeviceAccessLevelResource(SecuritySuiteDeviceAccessLevelInfoFactory securitySuiteDeviceAccessLevelInfoFactory, ProtocolPluggableService protocolPluggableService, ExceptionFactory exceptionFactory) {
        this.securitySuiteDeviceAccessLevelInfoFactory = securitySuiteDeviceAccessLevelInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * The security suite is identified by both the id of the pluggable class and the applicable security suite access level id
     *
     * @param deviceProtocolPluggableClassId id of the device protocol pluggable class
     * @param securitySuiteDeviceAccessLevelId id of the security suite
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified security suite
     * @summary Get a certain security suite.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{securitySuiteDeviceAccessLevelId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceAccessLevelInfo getSecuritySuiteDeviceAccessLevel(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("securitySuiteDeviceAccessLevelId") long securitySuiteDeviceAccessLevelId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        com.energyict.mdc.upl.DeviceProtocol actualDeviceProtocol = getActualDeviceProtocol(pluggableClass.getDeviceProtocol());
        if (actualDeviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities) {
            return ((AdvancedDeviceProtocolSecurityCapabilities) actualDeviceProtocol).getSecuritySuites()
                    .stream()
                    .filter(lvl -> lvl.getId() == securitySuiteDeviceAccessLevelId)
                    .findFirst()
                    .map(this.protocolPluggableService::adapt)
                    .map(lvl -> securitySuiteDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SECURITY_SUITE));
        } else {
            throw exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SECURITY_SUITE).get();
        }
    }

    /**
     * Get a list of al known security suites for a certain device protocol. The device protocol is
     * identified through the pluggable class that wraps it.
     *
     * @param deviceProtocolPluggableClassId The ID of the device protocol pluggable class
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return List of security suites. Paged if paging parameters were provided in the call.
     * @summary Get a list of all known security suites.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceAccessLevelInfo> getSecuritySuiteDeviceAccessLevels(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection,
            @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        com.energyict.mdc.upl.DeviceProtocol actualDeviceProtocol = getActualDeviceProtocol(pluggableClass.getDeviceProtocol());
        List<SecuritySuite> securitySuites = actualDeviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities
                ? ((AdvancedDeviceProtocolSecurityCapabilities) actualDeviceProtocol).getSecuritySuites()
                : Collections.emptyList();
        List<DeviceAccessLevelInfo> infos = ListPager.of(securitySuites).from(queryParameters)
                .stream()
                .map(this.protocolPluggableService::adapt)
                .map(lvl -> securitySuiteDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(SecuritySuiteDeviceAccessLevelResource.class)
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
        return securitySuiteDeviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
