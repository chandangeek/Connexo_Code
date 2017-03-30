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
import com.energyict.mdc.device.config.SecurityPropertySet;
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
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securitypropertysets")
public class ConfigurationSecurityPropertySetResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ConfigurationSecurityPropertySetInfoFactory securityPropertySetInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConfigurationSecurityPropertySetResource(DeviceConfigurationService deviceConfigurationService,
                                                    ConfigurationSecurityPropertySetInfoFactory securityPropertySetInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Models named set of security properties whose values
     * are managed against a Device.
     * The exact set of PropertySpecs that are used is determined by the AuthenticationDeviceAccessLevel
     * and/or EncryptionDeviceAccessLevel select in the SecurityPropertySet.
     * That in turn depends on the actual DeviceProtocol.
     *
     * @summary Fetch a set of pre-configured security sets
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigurationId Id of the device configuration
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     *
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ConfigurationSecurityPropertySetInfo> getSecurityPropertySets(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigurationId,
                                                 @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        DeviceConfiguration deviceConfiguration = findDeviceConfigurationOrThrowException(deviceTypeId, deviceConfigurationId);
        List<ConfigurationSecurityPropertySetInfo> securityPropertySetInfos =
                ListPager.of(deviceConfiguration.getSecurityPropertySets(), Comparator.comparing(SecurityPropertySet::getName, String.CASE_INSENSITIVE_ORDER))
                         .from(queryParameters)
                         .stream()
                         .map(ss -> securityPropertySetInfoFactory.from(ss, uriInfo, fieldSelection.getFields()))
                         .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ConfigurationSecurityPropertySetResource.class);

        return PagedInfoList.from(securityPropertySetInfos, queryParameters, uriBuilder, uriInfo);
    }


    /**
     * Models named set of security properties whose values
     * are managed against a Device.
     * The exact set of PropertySpecs that are used is determined by the AuthenticationDeviceAccessLevel
     * and/or EncryptionDeviceAccessLevel select in the SecurityPropertySet.
     * That in turn depends on the actual DeviceProtocol.
     *
     * @summary Fetch a set of pre-configured security sets

     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param securityPropertySetId Id of the security set
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified security property set
     */
    @GET @Transactional
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ConfigurationSecurityPropertySetInfo getSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId,
                                                                       @PathParam("deviceConfigId") long deviceConfigId,
                                                                       @PathParam("securityPropertySetId") long securityPropertySetId,
                                                                       @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        SecurityPropertySet securityPropertySet = findSecurityPropertySetOrThrowException(deviceTypeId, deviceConfigId, securityPropertySetId);

        return securityPropertySetInfoFactory.from(securityPropertySet, uriInfo, fieldSelection.getFields());
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
        return securityPropertySetInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

    private SecurityPropertySet findSecurityPropertySetOrThrowException(long deviceTypeId, long deviceConfigId, long securityPropertySetId) {
        return findDeviceConfigurationOrThrowException(deviceTypeId, deviceConfigId)
                .getSecurityPropertySets().stream()
                .filter(sps -> sps.getId() == securityPropertySetId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET));
    }

    private DeviceConfiguration findDeviceConfigurationOrThrowException(long deviceTypeId, long deviceConfigurationId) {
        return deviceConfigurationService.
                    findDeviceType(deviceTypeId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE)).
                            getConfigurations().stream().filter(dc -> dc.getId() == deviceConfigurationId).
                            findFirst()
                            .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
    }

}
