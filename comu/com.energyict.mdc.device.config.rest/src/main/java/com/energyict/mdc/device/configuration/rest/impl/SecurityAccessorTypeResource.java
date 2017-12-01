/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorType.Builder;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeDuration;

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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// TODO: move to pki?
@Path("/securityaccessors")
public class SecurityAccessorTypeResource {
    private final ResourceHelper resourceHelper;
    private final SecurityManagementService securityManagementService;
    private final KeyFunctionTypeInfoFactory keyFunctionTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public SecurityAccessorTypeResource(ResourceHelper resourceHelper, SecurityManagementService securityManagementService, KeyFunctionTypeInfoFactory keyFunctionTypeInfoFactory, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.securityManagementService = securityManagementService;
        this.keyFunctionTypeInfoFactory = keyFunctionTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public PagedInfoList getSecurityAccessorTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<SecurityAccessorInfo> infoList = securityManagementService.getSecurityAccessorTypes().stream()
                .map(keyFunctionTypeInfoFactory::from)
                .sorted(Comparator.comparing(k -> k.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityaccessors", infoList, queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    @Path("/{id}")
    public SecurityAccessorInfo getSecurityAccessorType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        return securityManagementService.findSecurityAccessorTypeById(id)
                .map(keyFunctionTypeInfoFactory::withSecurityLevels)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/keytypes")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public List<KeyTypeInfo> getKeyTypes() {
        return securityManagementService.findAllKeyTypes()
                .stream()
                .map(KeyTypeInfo::new)
                .collect(Collectors.toList());
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/keytypes/{id}/keyencryptionmethods")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public List<KeyEncryptionMethodInfo> getKeyEncryptionMethods(@PathParam("id") long id) {
        return securityManagementService.findAllKeyTypes()
                .stream()
                .filter(keyType -> keyType.getId() == id)
                .findAny()
                .map(keyType -> securityManagementService.getKeyEncryptionMethods(keyType.getCryptographicType()))
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_KEY_TYPE_FOUND, id))
                .stream()
                .map(KeyEncryptionMethodInfo::new)
                .collect(Collectors.toList());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public SecurityAccessorInfo addSecurityAccessorType(SecurityAccessorInfo securityAccessorInfo) {
        if (securityAccessorInfo.keyType == null || securityAccessorInfo.keyType.name == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyType");
        }
        KeyType keyType = securityManagementService.getKeyType(securityAccessorInfo.keyType.name)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_KEY_TYPE_FOUND_NAME, securityAccessorInfo.keyType.name));
        Builder keyFunctionTypeBuilder = securityManagementService.addSecurityAccessorType(securityAccessorInfo.name, keyType)
                .keyEncryptionMethod(securityAccessorInfo.storageMethod)
                .description(securityAccessorInfo.description);
        if (keyType.getCryptographicType() != null && !keyType.getCryptographicType().isKey()) {
            TrustStore trustStore = securityManagementService.findTrustStore(securityAccessorInfo.trustStoreId)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_TRUST_STORE_FOUND, securityAccessorInfo.trustStoreId));
            keyFunctionTypeBuilder.trustStore(trustStore);
        }
        if (securityAccessorInfo.duration != null && keyType.getCryptographicType().requiresDuration()) {
            keyFunctionTypeBuilder.duration(getDuration(securityAccessorInfo));
        } else {
            keyFunctionTypeBuilder.duration(null);
        }
        SecurityAccessorType keyFunctionType = keyFunctionTypeBuilder.add();
        return keyFunctionTypeInfoFactory.from(keyFunctionType);
    }

    private static TimeDuration getDuration(SecurityAccessorInfo securityAccessorInfo) {
        try {
            return securityAccessorInfo.duration.asTimeDuration();
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_TIME_DURATION, "duration");
        }
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public SecurityAccessorInfo changeSecurityAccessorType(@PathParam("id") long id, SecurityAccessorInfo securityAccessorInfo) {
        SecurityAccessorType securityAccessorType =
                resourceHelper.lockSecurityAccessorTypeOrThrowException(id, securityAccessorInfo.version, securityAccessorInfo.name);
        SecurityAccessorTypeUpdater updater = securityAccessorType.startUpdate();
        updater.name(securityAccessorInfo.name);
        updater.description(securityAccessorInfo.description);
        if (securityAccessorInfo.duration != null && securityAccessorType.getKeyType().getCryptographicType().requiresDuration()) {
            updater.duration(getDuration(securityAccessorInfo));
        } else {
            updater.duration(null);
        }
        securityAccessorType.getUserActions()
                .forEach(updater::removeUserAction);
        securityAccessorInfo.viewLevels
                .forEach(level -> updater.addUserAction(SecurityAccessorUserAction.forPrivilege(level.id).get()));
        securityAccessorInfo.editLevels
                .forEach(level -> updater.addUserAction(SecurityAccessorUserAction.forPrivilege(level.id).get()));
        SecurityAccessorType updated = updater.complete();
        return keyFunctionTypeInfoFactory.from(updated);
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public Response removeSecurityAccessorType(@PathParam("id") long id, SecurityAccessorInfo securityAccessorInfo) {
        SecurityAccessorType keyFunctionType =
                resourceHelper.lockSecurityAccessorTypeOrThrowException(id, securityAccessorInfo.version, securityAccessorInfo.name);
        keyFunctionType.delete();
        return Response.noContent().build();
    }
}
