package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("pluggableclasses/{deviceProtocolPluggableClassId}/authenticationaccesslevels")
public class AuthenticationDeviceAccessLevelResource {

    private final AuthenticationDeviceAccessLevelInfoFactory authenticationDeviceAccessLevelInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public AuthenticationDeviceAccessLevelResource(AuthenticationDeviceAccessLevelInfoFactory authenticationDeviceAccessLevelInfoFactory, ProtocolPluggableService protocolPluggableService) {
        this.authenticationDeviceAccessLevelInfoFactory = authenticationDeviceAccessLevelInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{authenticationDeviceAccessLevelId}")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public DeviceAccessLevelInfo getAuthenticationDeviceAccessLevel(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("authenticationDeviceAccessLevelId") long authenticationDeviceAccessLevelId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return pluggableClass.getDeviceProtocol()
                .getAuthenticationAccessLevels()
                .stream()
                .filter(lvl -> lvl.getId() == authenticationDeviceAccessLevelId)
                .findFirst()
                .map(lvl -> authenticationDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public PagedInfoList<DeviceAccessLevelInfo> getAuthenticationDeviceAccessLevels(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("authenticationDeviceAccessLevelId") long authenticationDeviceAccessLevelId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<DeviceAccessLevelInfo> infos = ListPager.of(pluggableClass.getDeviceProtocol().getAuthenticationAccessLevels()).from(queryParameters)
                .stream()
                .map(lvl -> authenticationDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(AuthenticationDeviceAccessLevelResource.class)
                .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClassId);
        return PagedInfoList.from(infos,queryParameters,uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public List<String> getFields() {
        return authenticationDeviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
