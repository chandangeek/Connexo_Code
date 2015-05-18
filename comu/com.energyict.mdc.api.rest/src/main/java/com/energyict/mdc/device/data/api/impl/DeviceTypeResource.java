package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.List;
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
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devicetypes")
public class DeviceTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceTypeInfoFactory deviceTypeInfoFactory;

    @Inject
    public DeviceTypeResource(DeviceConfigurationService deviceConfigurationService, DeviceTypeInfoFactory deviceTypeInfoFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceTypeInfoFactory = deviceTypeInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{id}")
    public Response getDevice(@PathParam("id") long id, @BeanParam FieldList fields) {
        DeviceTypeInfo deviceTypeInfo = deviceConfigurationService.findDeviceType(id).map(dt -> deviceTypeInfoFactory.plain(dt, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceTypeInfo).build();
    }

    @GET
    @Produces("application/h+json;charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{id}")
    public Response getHypermediaDevice(@PathParam("id") long id, @BeanParam FieldList fields, @Context UriInfo uriInfo) {
        DeviceTypeInfo deviceTypeInfo = deviceConfigurationService.findDeviceType(id).map(d -> deviceTypeInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceTypeInfo).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldList fields, @Context UriInfo uriInfo) {
        List<DeviceTypeInfo> infos = deviceConfigurationService.findAllDeviceTypes().stream().map(d -> deviceTypeInfoFactory.plain(d, fields.getFields())).collect(toList());

        return Response.ok(com.energyict.mdc.common.rest.PagedInfoList.fromCompleteList("deviceTypes", infos, queryParameters)).build();
    }

    @GET
    @Produces("application/h+json;charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getHypermediaDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldList fields,@Context UriInfo uriInfo) {
        List<DeviceTypeInfo> infos = deviceConfigurationService.findAllDeviceTypes().stream().map(d -> deviceTypeInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).collect(toList());


        return Response.ok(PagedInfoList.fromCompleteList("deviceTypes", infos, queryParameters, uriInfo)).build();
    }

}

