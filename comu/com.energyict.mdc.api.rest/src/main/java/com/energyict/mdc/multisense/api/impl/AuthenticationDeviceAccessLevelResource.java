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
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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

@Path("pluggableclasses/{deviceProtocolPluggableClassId}/authenticationaccesslevels")
public class AuthenticationDeviceAccessLevelResource {

    private final AuthenticationDeviceAccessLevelInfoFactory authenticationDeviceAccessLevelInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public AuthenticationDeviceAccessLevelResource(AuthenticationDeviceAccessLevelInfoFactory authenticationDeviceAccessLevelInfoFactory, ProtocolPluggableService protocolPluggableService, ExceptionFactory exceptionFactory) {
        this.authenticationDeviceAccessLevelInfoFactory = authenticationDeviceAccessLevelInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * The authentication is identified by both the id of the pluggable class and the applicable authentication access level id
     *
     * @summary Get a certain authentication access level.
     *
     * @param deviceProtocolPluggableClassId id of the device protocol pluggable class
     * @param authenticationDeviceAccessLevelId  id of the authentication device access level
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified authentication device access level
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{authenticationDeviceAccessLevelId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceAccessLevelInfo getAuthenticationDeviceAccessLevel(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("authenticationDeviceAccessLevelId") long authenticationDeviceAccessLevelId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        return pluggableClass.getDeviceProtocol()
                .getAuthenticationAccessLevels()
                .stream()
                .filter(lvl -> lvl.getId() == authenticationDeviceAccessLevelId)
                .findFirst()
                .map(this.protocolPluggableService::adapt)
                .map(lvl -> authenticationDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_AUTH_DEVICE_ACCESS_LEVEL));
    }

    /**
     * Get a list of al known authentication access levels for a certain device protocol. The device protocol is
     * identified through the pluggable class that wraps it.
     *
     * @summary Get a list of all known authentication access levels.
     *
     * @param deviceProtocolPluggableClassId The ID of the device protocol pluggable class
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return List of authentication access levels. Paged if paging parameters were provided in the call.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceAccessLevelInfo> getAuthenticationDeviceAccessLevels(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection,
            @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        List<DeviceAccessLevelInfo> infos = ListPager.of(pluggableClass.getDeviceProtocol().getAuthenticationAccessLevels()).from(queryParameters)
                .stream()
                .map(this.protocolPluggableService::adapt)
                .map(lvl -> authenticationDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(AuthenticationDeviceAccessLevelResource.class)
                .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClassId);
        return PagedInfoList.from(infos,queryParameters,uriBuilder, uriInfo);
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
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return authenticationDeviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
