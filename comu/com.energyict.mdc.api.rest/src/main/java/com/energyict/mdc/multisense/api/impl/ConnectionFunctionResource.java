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
import com.energyict.mdc.protocol.api.ConnectionFunction;
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
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("pluggableclasses/{deviceProtocolPluggableClassId}/connectionfunctions")
public class ConnectionFunctionResource {

    private final ConnectionFunctionInfoFactory connectionFunctionInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionFunctionResource(ConnectionFunctionInfoFactory connectionFunctionInfoFactory, ProtocolPluggableService protocolPluggableService, ExceptionFactory exceptionFactory) {
        this.connectionFunctionInfoFactory = connectionFunctionInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * The connection function is identified by both the id of the pluggable class and the applicable connection function id
     *
     * @param deviceProtocolPluggableClassId id of the device protocol pluggable class
     * @param connectionFunctionId id of the connection function
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified connection function
     * @summary Get a certain connection function.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{connectionFunctionId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ConnectionFunctionInfo getConnectionFunction(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("connectionFunctionId") long connectionFunctionId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        return getAllProvidedOrConsumableConnectionFunctions(pluggableClass)
                .stream()
                .filter(function -> function.getId() == connectionFunctionId)
                .findFirst()
                .map(function -> connectionFunctionInfoFactory.from(pluggableClass, function, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CONNECTION_FUNCTION));
    }

    /**
     * Get a list of al known connection functions for a certain device protocol. The device protocol is
     * identified through the pluggable class that wraps it.<br/>
     * Note that the list will contain both the consumable and provided connection functions for the device protocol
     *
     * @param deviceProtocolPluggableClassId The ID of the device protocol pluggable class
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return List of connection functions. Paged if paging parameters were provided in the call.
     * @summary Get a list of all known connection functions.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ConnectionFunctionInfo> getProvidedConnectionFunctions(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection,
            @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        List<ConnectionFunction> connectionFunctions = getAllProvidedOrConsumableConnectionFunctions(pluggableClass);
        List<ConnectionFunctionInfo> infos = ListPager.of(connectionFunctions).from(queryParameters)
                .stream()
                .sorted(Comparator.comparing(ConnectionFunction::getId))
                .map(function -> connectionFunctionInfoFactory.from(pluggableClass, function, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ConnectionFunctionResource.class)
                .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClassId);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    private List<ConnectionFunction> getAllProvidedOrConsumableConnectionFunctions(DeviceProtocolPluggableClass pluggableClass) {
        List<ConnectionFunction> connectionFunctions = pluggableClass.getProvidedConnectionFunctions();
        pluggableClass.getConsumableConnectionFunctions()
                .stream()
                .filter(cf -> notAlreadyPresent(connectionFunctions, cf))
                .forEach(connectionFunctions::add);
        return connectionFunctions;
    }

    private boolean notAlreadyPresent(List<ConnectionFunction> connectionFunctions, ConnectionFunction connectionFunctionToTest) {
        return connectionFunctions.stream()
                .noneMatch(cf -> cf.getId() == connectionFunctionToTest.getId());
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
        return connectionFunctionInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}