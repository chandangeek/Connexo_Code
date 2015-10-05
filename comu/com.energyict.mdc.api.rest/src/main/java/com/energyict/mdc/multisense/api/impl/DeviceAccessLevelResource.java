package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
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

@Path("/deviceaccesslevels")
public class DeviceAccessLevelResource {

//    private final DeviceAccessLevelInfoFactory deviceAccessLevelInfoFactory;
//    private final DeviceAccessLevelService deviceAccessLevelService;
//
//    @Inject
//    public DeviceAccessLevelResource(DeviceAccessLevelService deviceAccessLevelService, DeviceAccessLevelInfoFactory deviceAccessLevelInfoFactory) {
//        this.deviceAccessLevelService = deviceAccessLevelService;
//        this.deviceAccessLevelInfoFactory = deviceAccessLevelInfoFactory;
//    }
//
//    @GET
//    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
//    @Path("/{deviceAccessLevelId}")
//    public DeviceAccessLevelInfo getDeviceAccessLevel(@PathParam("deviceAccessLevelId") long deviceAccessLevelId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
//         return deviceAccessLevelService.findDeviceAccessLevel(deviceAccessLevelId)
//                 .map(ct -> deviceAccessLevelInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
//                 .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
//    }
//
//    @GET
//    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
//    public PagedInfoList<DeviceAccessLevelInfo> getDeviceAccessLevels(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
//        List<DeviceAccessLevelInfo> infos = deviceAccessLevelService.findAllDeviceAccessLevels().from(queryParameters).stream()
//                .map(ct -> deviceAccessLevelInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
//                .collect(toList());
//        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
//                .path(DeviceAccessLevelResource.class);
//        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
//    }
//
//    @PROPFIND
//    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
//    public List<String> getFields() {
//        return deviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
//    }

}
