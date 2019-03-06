/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityAccessorTypeOnDeviceTypeResource {
    private final ResourceHelper resourceHelper;
    private final SecurityManagementService securityManagementService;
    private final SecurityAccessorTypeInfoFactory keyFunctionTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public SecurityAccessorTypeOnDeviceTypeResource(ResourceHelper resourceHelper, SecurityManagementService securityManagementService, SecurityAccessorTypeInfoFactory keyFunctionTypeInfoFactory, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.securityManagementService = securityManagementService;
        this.keyFunctionTypeInfoFactory = keyFunctionTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceTypeSecurityAccessors(@PathParam("deviceTypeId") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorTypeInfo> infos = deviceType.getSecurityAccessorTypes().stream()
                .map(keyFunctionTypeInfoFactory::from)
                .sorted(Comparator.comparing(k -> k.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    @Path("/unassigned")
    public PagedInfoList getSecurityAccessorsUnassignedToDeviceType(@PathParam("deviceTypeId") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorTypeInfo> infos = subtract(securityManagementService.getSecurityAccessorTypes(SecurityAccessorType.Purpose.DEVICE_OPERATIONS), deviceType.getSecurityAccessorTypes())
                .map(keyFunctionTypeInfoFactory::from)
                .sorted(Comparator.comparing(k -> k.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    private static <T> Stream<T> subtract(Collection<T> minuend, Collection<T> subtrahend) {
        Set<T> toRemove = new HashSet<>(subtrahend);
        return minuend.stream()
                .filter(item -> !toRemove.contains(item));
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    @Path("/{securityAccessorId}")
    public SecurityAccessorTypeInfo getDeviceTypeSecurityAccessor(@PathParam("deviceTypeId") long id, @PathParam("securityAccessorId") long securityAccessorId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorType> securityAccessorTypes = deviceType.getSecurityAccessorTypes();
        SecurityAccessorType securityAccessorType = securityAccessorTypes.stream()
                .filter(kat -> kat.getId() == securityAccessorId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
        SecurityAccessorTypeInfo accessorTypeInfo =  keyFunctionTypeInfoFactory.withSecurityLevels(securityAccessorType);
	accessorTypeInfo.defaultServiceKey = deviceType.getDefaultKeyOfSecurityAccessorType(securityAccessorType);
	return accessorTypeInfo;
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response addSecurityAccessorTypesToDeviceType(@PathParam("deviceTypeId") long id, SecurityAccessorsForDeviceTypeInfo info) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, info.version, info.name);
        SecurityAccessorType[] securityAccessorTypes = info.securityAccessors.stream()
                .map(securityAccessorInfo -> resourceHelper.lockSecurityAccessorTypeOrThrowException(securityAccessorInfo.id, securityAccessorInfo.version, securityAccessorInfo.name))
                .toArray(SecurityAccessorType[]::new);
        if (deviceType.addSecurityAccessorTypes(securityAccessorTypes)) {
            deviceType.update();
        }
        return Response.ok().build();
    }

    @DELETE
    @Transactional
    @Path("/{securityAccessorId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response removeSecurityAccessorTypeFromDeviceType(@PathParam("deviceTypeId") long id, @PathParam("securityAccessorId") long securityAccessorId, SecurityAccessorsForDeviceTypeInfo info) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, info.version, info.name);
        SecurityAccessorType keyFunctionType = deviceType.getSecurityAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == securityAccessorId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
        if (deviceType.removeSecurityAccessorType(keyFunctionType)) {
            deviceType.update();
        }
        return Response.noContent().build();
    }

    /**
     * Sets the default key value for the device type for the given security accessor type.
     *
     * @param deviceTypeId       Identifier of the device type for which the default key will be updated
     * @param securityAccessorId Identifier of the security accessor for which the key will be updated
     * @param value              Default key value for the security accessor type
     * @summary sets/updates the default key value for the device type for the given security accessor type
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    @Path("/{securityAccessorId}/setDefaultKey")
    public Response setDefaultKeySecurityAccessor(@PathParam("deviceTypeId") long id, @PathParam("securityAccessorId") long securityAccessorId, String value) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorType> securityAccessorTypes = deviceType.getSecurityAccessorTypes();
        SecurityAccessorType keyFunctionType = deviceType.getSecurityAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == securityAccessorId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));

	deviceType.updateDefaultKeyOfSecurityAccessorType(keyFunctionType, value);
        return Response.ok(value).build();
    }

}
