package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{deviceProtocolPluggableClassId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceProtocolPluggableClassInfo getDeviceProtocolPluggableClass(@PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                 .map(ct -> deviceProtocolPluggableClassInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
    }

    @GET
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

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceProtocolPluggableClassInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
