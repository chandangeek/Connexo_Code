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
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
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

    /**
     * Device configurations are used to customise or limit some of the functionality of the device type, but also
     * configure additional parameters to interact with the physical device. The device configuration will determine
     * the actual register specifications together with the number of channels. It also contains information on security
     * levels, connection methods, etc.
     *
     * @summary Fetch a device configuration
     *
     * @param deviceTypeId Id of the devuce type
     * @param id Id of the device configuration
     * @param uriInfo uriInfo
     * @param fields fields
     * @return Uniquely identified device configuration
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{deviceConfigId}")
    @RolesAllowed({com.energyict.mdc.multisense.api.security.Privileges.Constants.PUBLIC_REST_API})
    public DeviceConfigurationInfo getDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long id,
                                                          @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        DeviceConfigurationInfo deviceConfigurationInfo = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE)).
                        getConfigurations().stream().filter(dc -> dc.getId() == id).
                        map(dc -> deviceConfigurationInfoFactory.from(dc, uriInfo, fields.getFields())).
                        findFirst()
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
        return deviceConfigurationInfo;
    }

    /**
     * Device configurations are used to customise or limit some of the functionality of the device type, but also
     * configure additional parameters to interact with the physical device. The device configuration will determine
     * the actual register specifications together with the number of channels. It also contains information on security
     * levels, connection methods, etc.
     *
     * @summary Fetch a set of device configurations
     *
     * @param deviceTypeId Id of the devuce type
     * @param uriInfo uriInfo
     * @param queryParameters queryParameters
     * @param fields fields
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.multisense.api.security.Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceConfigurationInfo> getHypermediaDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId,
                                                                                    @BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam FieldSelection fields,@Context UriInfo uriInfo) {
        List<DeviceConfiguration> allDeviceConfigurations = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE)).getConfigurations();
        List<DeviceConfigurationInfo> infos = ListPager.of(allDeviceConfigurations).from(queryParameters).find().stream().map(dc -> deviceConfigurationInfoFactory.from(dc, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).resolveTemplate("deviceTypeId", deviceTypeId);

        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
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
     * @summary List the fields available on this type of entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceConfigurationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

