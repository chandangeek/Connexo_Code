package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
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
@Path("/devicetypes")
public class DeviceTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceTypeInfoFactory deviceTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceTypeResource(DeviceConfigurationService deviceConfigurationService, DeviceTypeInfoFactory deviceTypeInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceTypeInfoFactory = deviceTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{deviceTypeId}")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public DeviceTypeInfo getHypermediaDeviceType(@PathParam("deviceTypeId") long id, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return deviceConfigurationService.findDeviceType(id)
                .map(d -> deviceTypeInfoFactory.from(d, uriInfo, fields.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public PagedInfoList<DeviceTypeInfo> getHypermediaDeviceTypes(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        List<DeviceTypeInfo> infos = deviceConfigurationService.findAllDeviceTypes().from(queryParameters).stream().map(d -> deviceTypeInfoFactory.from(d, uriInfo, fields.getFields())).collect(toList());

        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class);
        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}

