/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;


import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyAccessorType.Builder;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeDuration.TimeUnit;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.KeyAccessorTypeUpdater;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityAccessorResource {
    private final ResourceHelper resourceHelper;
    private final PkiService pkiService;
    private final KeyFunctionTypeInfoFactory keyFunctionTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public SecurityAccessorResource(ResourceHelper resourceHelper, PkiService pkiService, KeyFunctionTypeInfoFactory keyFunctionTypeInfoFactory, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.pkiService = pkiService;
        this.keyFunctionTypeInfoFactory = keyFunctionTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceTypeSecurityAccessors(@PathParam("deviceTypeId") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<KeyAccessorType> keyAccessorTypes = deviceType.getKeyAccessorTypes();
        List<SecurityAccessorInfo> infos = keyAccessorTypes.stream()
                .map(keyAccessorType -> keyFunctionTypeInfoFactory.from(keyAccessorType, deviceType))
                .sorted(Comparator.comparing(k -> k.name.toLowerCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    @Path("/{securityAccessorId}")
    public SecurityAccessorInfo getDeviceTypeSecurityAccessor(@PathParam("deviceTypeId") long id, @PathParam("securityAccessorId") long securityAccessorId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<KeyAccessorType> keyAccessorTypes = deviceType.getKeyAccessorTypes();
        return keyAccessorTypes.stream().filter(kat->kat.getId() == securityAccessorId)
                .map(keyAccessorType -> keyFunctionTypeInfoFactory.withSecurityLevels(keyAccessorType, deviceType))
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/keytypes")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public List<KeyTypeInfo> getKeyTypes(@PathParam("deviceTypeId") long id) {
        return pkiService.findAllKeyTypes()
                .stream()
                .map(KeyTypeInfo::new)
                .collect(Collectors.toList());
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/keytypes/{id}/keyencryptionmethods")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public List<KeyEncryptionMethodInfo> getKeyEncryptionMethods(@PathParam("id") long id) {
        List<String> names = pkiService.findAllKeyTypes()
            .stream()
            .filter(keyType -> keyType.getId() == id)
            .findAny()
            .map(keyType -> pkiService.getKeyEncryptionMethods(keyType.getCryptographicType()))
            .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_KEY_TYPE_FOUND, id));
        List<KeyEncryptionMethodInfo> result = new ArrayList<>();
        for (String name : names) {
            result.add(new KeyEncryptionMethodInfo(name));
        }
        return result;
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public SecurityAccessorInfo addKeyFunctionTypeOnDeviceType(@PathParam("deviceTypeId") long id, SecurityAccessorInfo securityAccessorInfo) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, securityAccessorInfo.parent.version, securityAccessorInfo.parent.id);
        if (securityAccessorInfo.keyType == null || securityAccessorInfo.keyType.name == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyType");
        }
        KeyType keyType = pkiService.getKeyType(securityAccessorInfo.keyType.name)
            .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_KEY_TYPE_FOUND_NAME, securityAccessorInfo.keyType.name));
        Builder keyFunctionTypeBuilder = deviceType.addKeyAccessorType(securityAccessorInfo.name, keyType)
                .keyEncryptionMethod(securityAccessorInfo.storageMethod)
                .description(securityAccessorInfo.description);
        if (keyType.getCryptographicType()!=null && !keyType.getCryptographicType().isKey()) {
            TrustStore trustStore = pkiService.findTrustStore(securityAccessorInfo.trustStoreId)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_TRUST_STORE_FOUND, securityAccessorInfo.trustStoreId));
            keyFunctionTypeBuilder.trustStore(trustStore);
        }
        if(securityAccessorInfo.duration != null && keyType.getCryptographicType().requiresDuration()) {
            checkValidDurationOrThrowException(securityAccessorInfo.duration);
            keyFunctionTypeBuilder.duration(securityAccessorInfo.duration.asTimeDuration());
        } else {
            keyFunctionTypeBuilder.duration(null);
        }
        KeyAccessorType keyFunctionType = keyFunctionTypeBuilder.add();
        return keyFunctionTypeInfoFactory.from(keyFunctionType, deviceType);
    }

    @PUT
    @Transactional
    @Path("/{keyFunctionTypeId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public SecurityAccessorInfo changeKeyFunctionType(@PathParam("deviceTypeId") long id, @PathParam("keyFunctionTypeId") long keyFunctionTypeId, SecurityAccessorInfo securityAccessorInfo) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, securityAccessorInfo.parent.version, securityAccessorInfo.parent.id);
        KeyAccessorType keyAccessorType = deviceType.getKeyAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == keyFunctionTypeId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
        KeyAccessorTypeUpdater updater = deviceType.getKeyAccessorTypeUpdater(keyAccessorType).get();
        updater.name(securityAccessorInfo.name);
        updater.description(securityAccessorInfo.description);
        if(securityAccessorInfo.duration != null && keyAccessorType.getKeyType().getCryptographicType().requiresDuration()) {
            checkValidDurationOrThrowException(securityAccessorInfo.duration);
            updater.duration(securityAccessorInfo.duration.asTimeDuration());
        } else {
            updater.duration(null);
        }
        Set<DeviceSecurityUserAction> keyAccessorTypeUserActions = deviceType.getKeyAccessorTypeUserActions(keyAccessorType);
        keyAccessorTypeUserActions.stream()
                .forEach(updater::removeUserAction);
        securityAccessorInfo.viewLevels.stream()
                .forEach(level -> updater.addUserAction(DeviceSecurityUserAction.forPrivilege(level.id).get()));
        securityAccessorInfo.editLevels.stream()
                .forEach(level -> updater.addUserAction(DeviceSecurityUserAction.forPrivilege(level.id).get()));
        KeyAccessorType updated = updater.complete();
        return keyFunctionTypeInfoFactory.from(updated, deviceType);
    }

    @DELETE
    @Transactional
    @Path("/{keyFunctionTypeId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response removeKeyFunctionType(@PathParam("deviceTypeId") long id, @PathParam("keyFunctionTypeId") long keyFunctionTypeId, SecurityAccessorInfo securityAccessorInfo) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, securityAccessorInfo.parent.version, securityAccessorInfo.parent.id);
        KeyAccessorType keyFunctionType = deviceType.getKeyAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == keyFunctionTypeId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException("No key function type with id " + keyFunctionTypeId, Response.Status.NOT_FOUND));
        deviceType.removeKeyAccessorType(keyFunctionType);
        return Response.ok().build();
    }

    private void checkValidDurationOrThrowException(TimeDurationInfo validityPeriod) {
        if(!(validityPeriod.asTimeDuration().getTimeUnit() == TimeUnit.MONTHS || validityPeriod.asTimeDuration().getTimeUnit() == TimeUnit.YEARS)) {
            throw new WebApplicationException("Invalid validity period", Response.Status.BAD_REQUEST);
        }
    }
}
