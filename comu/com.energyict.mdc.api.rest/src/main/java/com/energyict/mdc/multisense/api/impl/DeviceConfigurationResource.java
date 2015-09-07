package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
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
@Path("/devicetypes/{deviceTypeId}/deviceconfigurations")
public class DeviceConfigurationResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceConfigurationInfoFactory deviceConfigurationInfoFactory;

    @Inject
    public DeviceConfigurationResource(DeviceConfigurationService deviceConfigurationService, DeviceConfigurationInfoFactory deviceConfigurationInfoFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceConfigurationInfoFactory = deviceConfigurationInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    @Path("/{deviceConfigId}")
    public DeviceConfigurationInfo getDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long id, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        DeviceConfigurationInfo deviceConfigurationInfo = deviceConfigurationService.
                findDeviceType(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode())).
                        getConfigurations().stream().filter(dc -> dc.getId() == id).
                        map(dc -> deviceConfigurationInfoFactory.asHypermedia(dc, uriInfo, fields.getFields())).
                        findFirst()
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return deviceConfigurationInfo;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList<DeviceConfigurationInfo> getHypermediaDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam FieldSelection fields,@Context UriInfo uriInfo) {
        List<DeviceConfiguration> allDeviceConfigurations = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode())).getConfigurations();
        List<DeviceConfigurationInfo> infos = ListPager.of(allDeviceConfigurations).from(queryParameters).find().stream().map(dc -> deviceConfigurationInfoFactory.asHypermedia(dc, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).resolveTemplate("deviceTypeId", deviceTypeId);

        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
    }


}

