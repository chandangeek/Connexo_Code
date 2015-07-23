package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.List;
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

import static java.util.stream.Collectors.toList;

@Path("/pluggableclasses")
public class DeviceProtocolPluggableClassResource {

    private final DeviceProtocolPluggableClassInfoFactory deviceProtocolPluggableClassInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public DeviceProtocolPluggableClassResource(ProtocolPluggableService protocolPluggableService, DeviceProtocolPluggableClassInfoFactory deviceProtocolPluggableClassInfoFactory) {
        this.protocolPluggableService = protocolPluggableService;
        this.deviceProtocolPluggableClassInfoFactory = deviceProtocolPluggableClassInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{deviceProtocolPluggableClassId}")
    public DeviceProtocolPluggableClassInfo getDeviceProtocolPluggableClass(@PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                 .map(ct -> deviceProtocolPluggableClassInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
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
    public List<String> getFields() {
        return deviceProtocolPluggableClassInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
