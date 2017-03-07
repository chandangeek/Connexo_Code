/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;


import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyAccessorType.Builder;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeDuration.TimeUnit;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.KeyAccessorTypeUpdater;
import com.energyict.mdc.device.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyFunctionTypeResource {
    private final ResourceHelper resourceHelper;
    private final PkiService pkiService;
    private final KeyFunctionTypeInfoFactory keyFunctionTypeInfoFactory;

    @Inject
    public KeyFunctionTypeResource(ResourceHelper resourceHelper, PkiService pkiService, KeyFunctionTypeInfoFactory keyFunctionTypeInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.pkiService = pkiService;
        this.keyFunctionTypeInfoFactory = keyFunctionTypeInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceTypeKeyFunctionTypes(@PathParam("deviceTypeId") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<KeyAccessorType> keyAccessorTypes = deviceType.getKeyAccessorTypes();
        List<KeyFunctionTypeInfo> infos = keyAccessorTypes.stream()
                .map(keyAccessorType -> keyFunctionTypeInfoFactory.from(keyAccessorType, deviceType))
                .sorted((k1, k2) -> k1.name.compareTo(k2.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/keytypes/{id}/keyencryptionmethods")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public List<String> getKeyEncryptionMethods(@PathParam("id") long id) {
        return new ArrayList<>();
//        return pkiService.findAllKeyTypes()
//                .stream()
//                .filter(keyType -> keyType.getId()==id)
//                .findAny()
//                .map(pkiService::getCryptographicType)
//                .orElseThrow(NoSuchElementException::new)
//                .collect(Collectors.toList());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public KeyFunctionTypeInfo addKeyFunctionTypeOnDeviceType(@PathParam("deviceTypeId") long id, KeyFunctionTypeInfo keyFunctionTypeInfo) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, keyFunctionTypeInfo.parent.version, keyFunctionTypeInfo.parent.id);
        KeyType keyType = keyFunctionTypeInfo.keyType != null && keyFunctionTypeInfo.keyType.name != null ? pkiService.getKeyType(keyFunctionTypeInfo.keyType.name).orElse(null) : null;
        //TODO: Encryption method not hardcoded, but for the moment not programmed yet
        //Should Encryption method should come from a drop down in th FE
        Builder keyFunctionTypeBuilder = deviceType.addKeyAccessorType(keyFunctionTypeInfo.name, keyType, "DataVault");
        keyFunctionTypeBuilder.description(keyFunctionTypeInfo.description);
        if(keyFunctionTypeInfo.validityPeriod != null && keyType.getCryptographicType().requiresDuration()) {
            checkValidDurationOrThrowException(keyFunctionTypeInfo.validityPeriod);
            keyFunctionTypeBuilder.duration(keyFunctionTypeInfo.validityPeriod.asTimeDuration());
        } else {
            keyFunctionTypeBuilder.duration(null);
        }
        KeyAccessorType keyFunctionType = keyFunctionTypeBuilder.add();
        return keyFunctionTypeInfoFactory.from(keyFunctionType, deviceType);
    }

    @GET
    @Transactional
    @Path("/{keyFunctionTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public KeyFunctionTypeInfo getKeyFunctionType(@PathParam("deviceTypeId") long id, @PathParam("keyFunctionTypeId") long keyFunctionTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        KeyAccessorType keyFunctionType = deviceType.getKeyAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == keyFunctionTypeId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException("No key function type with id " + keyFunctionTypeId, Response.Status.NOT_FOUND));
        return keyFunctionTypeInfoFactory.from(keyFunctionType, deviceType);
    }

    @PUT
    @Transactional
    @Path("/{keyFunctionTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public KeyFunctionTypeInfo changeKeyFunctionType(@PathParam("deviceTypeId") long id, @PathParam("keyFunctionTypeId") long keyFunctionTypeId, KeyFunctionTypeInfo keyFunctionTypeInfo) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, keyFunctionTypeInfo.parent.version, keyFunctionTypeInfo.parent.id);
        KeyAccessorType keyFunctionType = deviceType.getKeyAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == keyFunctionTypeId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException("No key function type with id " + keyFunctionTypeId, Response.Status.NOT_FOUND));
        KeyAccessorTypeUpdater updater = deviceType.getKeyAccessorTypeUpdater(keyFunctionType).get();
        updater.name(keyFunctionTypeInfo.name);
        updater.description(keyFunctionTypeInfo.description);
        if(keyFunctionTypeInfo.validityPeriod != null && keyFunctionType.getKeyType().getCryptographicType().requiresDuration()) {
            checkValidDurationOrThrowException(keyFunctionTypeInfo.validityPeriod);
            updater.duration(keyFunctionTypeInfo.validityPeriod.asTimeDuration());
        } else {
            updater.duration(null);
        }
        Set<DeviceSecurityUserAction> keyAccessorTypeUserActions = deviceType.getKeyAccessorTypeUserActions(keyFunctionType);
        keyAccessorTypeUserActions.stream()
                .forEach(updater::removeUserAction);
        keyFunctionTypeInfo.viewLevels.stream()
                .forEach(level -> updater.addUserAction(DeviceSecurityUserAction.forPrivilege(level.id).get()));
        keyFunctionTypeInfo.editLevels.stream()
                .forEach(level -> updater.addUserAction(DeviceSecurityUserAction.forPrivilege(level.id).get()));
        KeyAccessorType updated = updater.complete();
        return keyFunctionTypeInfoFactory.from(updated, deviceType);
    }

    @DELETE
    @Transactional
    @Path("/{keyFunctionTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response removeKeyFunctionType(@PathParam("deviceTypeId") long id, @PathParam("keyFunctionTypeId") long keyFunctionTypeId, KeyFunctionTypeInfo keyFunctionTypeInfo) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, keyFunctionTypeInfo.parent.version, keyFunctionTypeInfo.parent.id);
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
