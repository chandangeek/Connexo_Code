package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
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
import javax.ws.rs.core.UriBuilder;
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
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    @Path("/{deviceTypeId}")
    public DeviceTypeInfo getHypermediaDeviceType(@PathParam("deviceTypeId") long id, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return deviceConfigurationService.findDeviceType(id).map(d -> deviceTypeInfoFactory.asInfo(d, uriInfo, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList<DeviceTypeInfo> getHypermediaDeviceTypes(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        List<DeviceTypeInfo> infos = deviceConfigurationService.findAllDeviceTypes().from(queryParameters).stream().map(d -> deviceTypeInfoFactory.asInfo(d, uriInfo, fields.getFields())).collect(toList());

        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class);
        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
    }

}

