package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
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

@Path("/pluggableclasses")
public class DeviceProtocolPluggableClassResource {

    private final DeviceProtocolPluggableClassInfoFactory deviceProtocolPluggableClassInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceProtocolPluggableClassResource(ProtocolPluggableService protocolPluggableService, DeviceProtocolPluggableClassInfoFactory deviceProtocolPluggableClassInfoFactory, ExceptionFactory exceptionFactory) {
        this.protocolPluggableService = protocolPluggableService;
        this.deviceProtocolPluggableClassInfoFactory = deviceProtocolPluggableClassInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{deviceProtocolPluggableClassId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceProtocolPluggableClassInfo getDeviceProtocolPluggableClass(@PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                 .map(ct -> deviceProtocolPluggableClassInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceProtocolPluggableClassInfo> getDeviceProtocolPluggableClasss(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceProtocolPluggableClassInfo> infos = protocolPluggableService.findAllDeviceProtocolPluggableClasses().from(queryParameters).stream()
                .map(ct -> deviceProtocolPluggableClassInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceProtocolPluggableClassResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * List the fields available on this entity.
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
     * will be returned
     *
     * @summary List the fields available on this entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceProtocolPluggableClassInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
