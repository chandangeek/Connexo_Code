package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;

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

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devicetypes/{deviceTypeId}/deviceconfigurations")
public class DeviceConfigurationResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceConfigurationInfoFactory deviceConfigurationInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceConfigurationResource(DeviceConfigurationService deviceConfigurationService, DeviceConfigurationInfoFactory deviceConfigurationInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceConfigurationInfoFactory = deviceConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{deviceConfigId}")
    @RolesAllowed({com.energyict.mdc.multisense.api.security.Privileges.PUBLIC_REST_API})
    public DeviceConfigurationInfo getDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long id, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        DeviceConfigurationInfo deviceConfigurationInfo = deviceConfigurationService.
                findDeviceType(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE)).
                        getConfigurations().stream().filter(dc -> dc.getId() == id).
                        map(dc -> deviceConfigurationInfoFactory.from(dc, uriInfo, fields.getFields())).
                        findFirst()
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
        return deviceConfigurationInfo;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.multisense.api.security.Privileges.PUBLIC_REST_API})
    public PagedInfoList<DeviceConfigurationInfo> getHypermediaDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam FieldSelection fields,@Context UriInfo uriInfo) {
        List<DeviceConfiguration> allDeviceConfigurations = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE)).getConfigurations();
        List<DeviceConfigurationInfo> infos = ListPager.of(allDeviceConfigurations).from(queryParameters).find().stream().map(dc -> deviceConfigurationInfoFactory.from(dc, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).resolveTemplate("deviceTypeId", deviceTypeId);

        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceConfigurationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

