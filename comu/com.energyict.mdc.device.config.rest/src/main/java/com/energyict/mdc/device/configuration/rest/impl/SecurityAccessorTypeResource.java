/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.hsm.HsmPublicConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorType.Builder;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.rest.AliasInfo;
import com.elster.jupiter.pki.rest.AliasSearchFilterFactory;
import com.elster.jupiter.pki.rest.SecurityAccessorResourceHelper;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.PathPrependingConstraintViolationException;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.configuration.rest.TrustStoreValuesProvider;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

// TODO: move to pki.rest?
@Path("/securityaccessors")
public class SecurityAccessorTypeResource {
    private static final String CURRENT_PROPERTIES_PATH = "defaultValue.currentProperties";
    private static final String TEMP_PROPERTIES_PATH = "defaultValue.tempProperties";

    private final ResourceHelper resourceHelper;
    private final SecurityManagementService securityManagementService;
    private final SecurityAccessorTypeInfoFactory keyFunctionTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final SecurityAccessorResourceHelper securityAccessorResourceHelper;
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final TrustStoreValuesProvider trustStoreValuesProvider;
    private final AliasSearchFilterFactory aliasSearchFilterFactory;
    private final Thesaurus thesaurus;
    private final HsmPublicConfiguration hsmPublicConfiguration;

    @Inject
    public SecurityAccessorTypeResource(ResourceHelper resourceHelper,
                                        SecurityManagementService securityManagementService,
                                        SecurityAccessorTypeInfoFactory keyFunctionTypeInfoFactory,
                                        ExceptionFactory exceptionFactory,
                                        SecurityAccessorResourceHelper securityAccessorResourceHelper,
                                        SecurityAccessorInfoFactory securityAccessorInfoFactory,
                                        TrustStoreValuesProvider trustStoreValuesProvider,
                                        AliasSearchFilterFactory aliasSearchFilterFactory,
                                        Thesaurus thesaurus,
                                        HsmPublicConfiguration hsmPublicConfiguration) {
        this.resourceHelper = resourceHelper;
        this.securityManagementService = securityManagementService;
        this.keyFunctionTypeInfoFactory = keyFunctionTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.securityAccessorResourceHelper = securityAccessorResourceHelper;
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.trustStoreValuesProvider = trustStoreValuesProvider;
        this.aliasSearchFilterFactory = aliasSearchFilterFactory;
        this.thesaurus = thesaurus;
        this.hsmPublicConfiguration = hsmPublicConfiguration;
    }

    private static TimeDuration getDuration(SecurityAccessorTypeInfo securityAccessorInfo) {
        try {
            return securityAccessorInfo.duration.asTimeDuration();
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_TIME_DURATION, "duration");
        }
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public PagedInfoList getSecurityAccessorTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<SecurityAccessorTypeInfo> infoList = securityManagementService.getSecurityAccessorTypes().stream()
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
    public SecurityAccessorTypeInfo getSecurityAccessorType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters,
                                                            @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider) {
        return securityManagementService.findSecurityAccessorTypeById(id)
                .map(sat -> {
                    SecurityAccessorTypeInfo info = keyFunctionTypeInfoFactory.withSecurityLevels(sat);
                    securityManagementService.getDefaultValues(sat)
                            .ifPresent(value -> info.defaultValue = securityAccessorInfoFactory.asCertificate(value, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider));
                    return info;
                })
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

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/purposes")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public List<IdWithNameInfo> getPurposes() {
        return Arrays.stream(SecurityAccessorType.Purpose.values())
                .map(keyFunctionTypeInfoFactory::purposeToInfo)
                .collect(Collectors.toList());
    }

    @GET
    @Transactional
    @Path("/certificates/aliases")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public PagedInfoList aliasSource(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<AliasInfo> collect = securityManagementService.getAliasesByFilter(aliasSearchFilterFactory.from(uriInfo))
                .from(queryParameters)
                .stream()
                .filter(certificateWrapper -> certificateWrapper.getWrapperStatus() != CertificateWrapperStatus.REVOKED)
                .map(CertificateWrapper::getAlias)
                .map(AliasInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("aliases", collect, queryParameters);
    }

    @POST
    @Transactional
    @Path("/previewproperties")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public SecurityAccessorInfo previewValueProperties(@BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider,
                                                       SecurityAccessorTypeInfo securityAccessorTypeInfo) {
        List<PropertySpec> propertySpecs;
        if (securityAccessorTypeInfo.keyType != null && securityAccessorTypeInfo.keyType.name != null) {
            KeyType keyType = securityManagementService.getKeyType(securityAccessorTypeInfo.keyType.name)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_KEY_TYPE_FOUND_NAME, securityAccessorTypeInfo.keyType.name));
            propertySpecs = securityManagementService.getPropertySpecs(keyType, securityAccessorTypeInfo.storageMethod);
        } else {
            propertySpecs = Collections.emptyList();
        }
        return securityAccessorInfoFactory.asCertificateProperties(propertySpecs, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public SecurityAccessorTypeInfo addSecurityAccessorType(@BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider,
                                                            SecurityAccessorTypeInfo securityAccessorTypeInfo) {
        if (securityAccessorTypeInfo.keyType == null || securityAccessorTypeInfo.keyType.name == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyType");
        }
        if (securityAccessorTypeInfo.purpose == null || securityAccessorTypeInfo.purpose.id == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "purpose");
        }

        KeyType keyType = securityManagementService.getKeyType(securityAccessorTypeInfo.keyType.name)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_KEY_TYPE_FOUND_NAME, securityAccessorTypeInfo.keyType.name));
        Builder keyFunctionTypeBuilder = securityManagementService.addSecurityAccessorType(securityAccessorTypeInfo.name, keyType)
                .keyEncryptionMethod(securityAccessorTypeInfo.storageMethod)
                .purpose(keyFunctionTypeInfoFactory.purposeFromInfo(securityAccessorTypeInfo.purpose))
                .description(securityAccessorTypeInfo.description);
        if (keyType.getCryptographicType() != null && !keyType.getCryptographicType().isKey()) {
            TrustStore trustStore = securityManagementService.findTrustStore(securityAccessorTypeInfo.trustStoreId)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_TRUST_STORE_FOUND, securityAccessorTypeInfo.trustStoreId));
            keyFunctionTypeBuilder.trustStore(trustStore);
        }
        if (securityAccessorTypeInfo.duration != null && keyType.getCryptographicType().requiresDuration()) {
            keyFunctionTypeBuilder.duration(getDuration(securityAccessorTypeInfo));
        } else {
            keyFunctionTypeBuilder.duration(null);
        }
        if (securityAccessorTypeInfo.defaultValue != null) {
            keyFunctionTypeBuilder.managedCentrally();
        }

        if (CryptographicType.Hsm.equals(keyType.getCryptographicType())) {
            checkHSMLabel(securityAccessorTypeInfo.label);
            keyFunctionTypeBuilder.label(securityAccessorTypeInfo.label);
            keyFunctionTypeBuilder.jssType(securityAccessorTypeInfo.hsmJssKeyType);
            keyFunctionTypeBuilder.importCapability(securityAccessorTypeInfo.importCapability);
            keyFunctionTypeBuilder.renewCapability(securityAccessorTypeInfo.renewCapability);
            keyFunctionTypeBuilder.keySize(securityAccessorTypeInfo.keySize);
            keyFunctionTypeBuilder.reversible(securityAccessorTypeInfo.isReversible);
        }

        SecurityAccessorType keyFunctionType = keyFunctionTypeBuilder.add();
        SecurityAccessorTypeInfo resultInfo = keyFunctionTypeInfoFactory.from(keyFunctionType);

        if (securityAccessorTypeInfo.defaultValue != null) {
            CertificateWrapper actualValue = wrapValidationExceptions(CURRENT_PROPERTIES_PATH,
                    () -> securityAccessorResourceHelper.createMandatoryCertificateWrapper(keyFunctionType, securityAccessorTypeInfo.defaultValue.currentProperties));
            CertificateWrapper tempValue = wrapValidationExceptions(TEMP_PROPERTIES_PATH,
                    () -> securityAccessorResourceHelper.createCertificateWrapper(keyFunctionType, securityAccessorTypeInfo.defaultValue.tempProperties))
                    .orElse(null);
            SecurityAccessor<CertificateWrapper> certificateAccessor = securityManagementService.setDefaultValues(keyFunctionType, actualValue, tempValue);
            resultInfo.defaultValue = securityAccessorInfoFactory.asCertificate(certificateAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        }
        return resultInfo;
    }

    private void checkHSMLabel(String label){
        if (label == null || label.isEmpty()){
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "label");
        }
    }

    private <T> T wrapValidationExceptions(String pathPrefix, Supplier<T> action) {
        try {
            return action.get();
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(thesaurus, e, pathPrefix);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField(pathPrefix);
        }
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public SecurityAccessorTypeInfo changeSecurityAccessorType(@PathParam("id") long id,
                                                               @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider,
                                                               SecurityAccessorTypeInfo securityAccessorTypeInfo) {
        SecurityAccessorType securityAccessorType =
                resourceHelper.lockSecurityAccessorTypeOrThrowException(id, securityAccessorTypeInfo.version, securityAccessorTypeInfo.name);
        SecurityAccessorTypeUpdater updater = securityAccessorType.startUpdate();
        updater.name(securityAccessorTypeInfo.name);
        updater.description(securityAccessorTypeInfo.description);
        if (securityAccessorType.keyTypeIsHSM()){
            checkHSMLabel(securityAccessorTypeInfo.label);
            updater.jssKeyType(securityAccessorTypeInfo.hsmJssKeyType);
            updater.label(securityAccessorTypeInfo.label);
            updater.importCapabilty(securityAccessorTypeInfo.importCapability);
            updater.renewCapability(securityAccessorTypeInfo.renewCapability);
            updater.keySize(securityAccessorTypeInfo.keySize);
            updater.reversible(securityAccessorTypeInfo.isReversible);
        }
        if (securityAccessorTypeInfo.duration != null && securityAccessorType.getKeyType().getCryptographicType().requiresDuration()) {
            updater.duration(getDuration(securityAccessorTypeInfo));
        } else {
            updater.duration(null);
        }
        securityAccessorType.getUserActions()
                .forEach(updater::removeUserAction);
        securityAccessorTypeInfo.viewLevels
                .forEach(level -> updater.addUserAction(SecurityAccessorUserAction.forPrivilege(level.id).get()));
        securityAccessorTypeInfo.editLevels
                .forEach(level -> updater.addUserAction(SecurityAccessorUserAction.forPrivilege(level.id).get()));
        SecurityAccessorType updated = updater.complete();
        SecurityAccessorTypeInfo resultInfo = keyFunctionTypeInfoFactory.from(updated);
        if (updated.isManagedCentrally()) {
            SecurityAccessor<CertificateWrapper> certificateAccessor = (SecurityAccessor<CertificateWrapper>) resourceHelper
                    .lockSecurityAccessorOrThrowException(updated, securityAccessorTypeInfo);
            boolean actualUpdated = wrapValidationExceptions(CURRENT_PROPERTIES_PATH,
                    () -> securityAccessorResourceHelper.updateActualCertificateIfNeeded(certificateAccessor, securityAccessorTypeInfo.defaultValue.currentProperties, true));
            boolean tempUpdated = wrapValidationExceptions(TEMP_PROPERTIES_PATH,
                    () -> securityAccessorResourceHelper.updateTempCertificateIfNeeded(certificateAccessor, securityAccessorTypeInfo.defaultValue.tempProperties));
            if (actualUpdated || tempUpdated) {
                certificateAccessor.save();
            }
            resultInfo.defaultValue = securityAccessorInfoFactory.asCertificate(certificateAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        }
        return resultInfo;
    }

    @DELETE
    @Transactional
    @Path("/{id}/tempvalue")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public Response clearTempValue(@PathParam("id") long id,
                                   @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider,
                                   SecurityAccessorTypeInfo securityAccessorTypeInfo) {
        SecurityAccessorType securityAccessorType =
                resourceHelper.lockSecurityAccessorTypeOrThrowException(id, securityAccessorTypeInfo.version, securityAccessorTypeInfo.name);
        SecurityAccessorTypeInfo resultInfo = keyFunctionTypeInfoFactory.from(securityAccessorType);
        if (securityAccessorType.isManagedCentrally()) {
            SecurityAccessor<CertificateWrapper> certificateAccessor = (SecurityAccessor<CertificateWrapper>) resourceHelper
                    .lockSecurityAccessorOrThrowException(securityAccessorType, securityAccessorTypeInfo);
            certificateAccessor.clearTempValue();
            resultInfo.defaultValue = securityAccessorInfoFactory.asCertificate(certificateAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        }
        return Response.ok(resultInfo).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/swap")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public Response swapCertificateValues(@PathParam("id") long id,
                                          @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider,
                                          SecurityAccessorTypeInfo securityAccessorTypeInfo) {
        SecurityAccessorType securityAccessorType =
                resourceHelper.lockSecurityAccessorTypeOrThrowException(id, securityAccessorTypeInfo.version, securityAccessorTypeInfo.name);
        SecurityAccessorTypeInfo resultInfo = keyFunctionTypeInfoFactory.from(securityAccessorType);
        if (securityAccessorType.isManagedCentrally()) {
            SecurityAccessor<CertificateWrapper> certificateAccessor = (SecurityAccessor<CertificateWrapper>) resourceHelper
                    .lockSecurityAccessorOrThrowException(securityAccessorType, securityAccessorTypeInfo);
            certificateAccessor.swapValues();
            resultInfo.defaultValue = securityAccessorInfoFactory.asCertificate(certificateAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        }
        return Response.ok(resultInfo).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EDIT_SECURITY_ACCESSORS)
    public Response removeSecurityAccessorType(@PathParam("id") long id, SecurityAccessorTypeInfo securityAccessorTypeInfo) {
        SecurityAccessorType keyFunctionType =
                resourceHelper.lockSecurityAccessorTypeOrThrowException(id, securityAccessorTypeInfo.version, securityAccessorTypeInfo.name);
        if (keyFunctionType.isManagedCentrally()) {
            resourceHelper.lockSecurityAccessorOrThrowException(keyFunctionType, securityAccessorTypeInfo)
                    .delete();
        }
        keyFunctionType.delete();
        return Response.noContent().build();
    }


    @GET
    @Transactional
    @Path("/hsm/labels")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public Response getHsmLabels() {
        try {
            return Response.ok(hsmPublicConfiguration.labels()).build();
        } catch (HsmBaseException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * perhaps this can be split into 2 endpoints: one for import and one for renew capability (or one with filter)
     */
    @GET
    @Transactional
    @Path("/hsm/capabilities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public Response getHsmCapabilities() {
        return Response.ok(SessionKeyCapability.values()).build();
    }

    @GET
    @Transactional
    @Path("/hsm/keysizes")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public Response getKeySizes() {
        /**
         * This comes from AES device key (JSS)
         */
        int[] sizes = {16, 24, 32};
        return Response.ok(sizes).build();
    }

    @GET
    @Transactional
    @Path("/hsm/keyType")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public Response getHsmKeyTypes() {
        return Response.ok(HsmJssKeyType.values()).build();
    }

}
