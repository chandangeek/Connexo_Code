/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.PathPrependingConstraintViolationException;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.rest.AliasInfo;
import com.energyict.mdc.device.data.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Setup:
 * DeviceType contains KeyAccessorTypes
 * Device may have a KeyAccessor for each KeyAccessorType
 * Towards FE, we pretend every KeyAccessorType has a KeyAccessor on device level. This is so the FE can display
 * properties even is no actual KeyAccessor exists. It is then up to this layer to figure out what to do.
 */
public class SecurityAccessorResource {
    private static final String CURRENT_PROPERTIES = "currentProperties";
    private static final String TEMP_PROPERTIES = "tempProperties";
    private static final String ALIAS = "alias";
    private static final String TRUST_STORE = "trustStore";

    private final Set<CryptographicType> CERTIFICATES = EnumSet.of(CryptographicType.Certificate, CryptographicType.ClientCertificate, CryptographicType.TrustedCertificate);
    private final Set<CryptographicType> KEYS = EnumSet.of(CryptographicType.SymmetricKey, CryptographicType.Passphrase);

    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final PkiService pkiService;
    private final ResourceHelper resourceHelper;
    private final Provider<KeyAccessorPlaceHolder> keyAccessorPlaceHolderProvider;
    private final ExceptionFactory exceptionFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final DeviceService deviceService;
    private final TrustStoreValuesProvider trustStoreValuesProvider;

    @Inject
    public SecurityAccessorResource(ResourceHelper resourceHelper,
                                    SecurityAccessorInfoFactory securityAccessorInfoFactory,
                                    PkiService pkiService,
                                    Provider<KeyAccessorPlaceHolder> keyAccessorPlaceHolderProvider,
                                    ExceptionFactory exceptionFactory,
                                    MdcPropertyUtils mdcPropertyUtils, DeviceService deviceService) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.resourceHelper = resourceHelper;
        this.pkiService = pkiService;
        this.keyAccessorPlaceHolderProvider = keyAccessorPlaceHolderProvider;
        this.exceptionFactory = exceptionFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceService = deviceService;
        this.trustStoreValuesProvider =  new TrustStoreValuesProvider();
    }

    @GET
    @Transactional
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getKeys(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<SecurityAccessorInfo> collect = getSecurityAccessorInfos(device, kat -> KEYS.contains(kat.getKeyType().getCryptographicType()),
                (keyAccessor) -> securityAccessorInfoFactory.asKeyWithLevels(keyAccessor, device.getDeviceType()));
        return PagedInfoList.fromCompleteList("keys", collect, queryParameters);
    }

    @GET
    @Transactional
    @Path("/keys/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response getKey(@PathParam("name") String name, @PathParam("id") long keyAccessorTypeId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);
        KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType)
                .orElseGet(() -> keyAccessorPlaceHolderProvider.get().init(keyAccessorType, device));
        return Response.ok(securityAccessorInfoFactory.asKey(keyAccessor)).build();
    }

    @GET
    @Transactional
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response getCertificate(@PathParam("name") String name, @PathParam("id") long keyAccessorTypeId, @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);
        KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType)
                .orElseGet(() -> keyAccessorPlaceHolderProvider.get().init(keyAccessorType, device));

        return Response.ok(securityAccessorInfoFactory.asCertificate(keyAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider)).build();
    }

    @GET
    @Transactional
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getCertificates(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<SecurityAccessorInfo> collect = getSecurityAccessorInfos(device, kat -> CERTIFICATES.contains(kat.getKeyType().getCryptographicType()), (keyAccessor) -> securityAccessorInfoFactory
                .asCertificate(keyAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider));
        return PagedInfoList.fromCompleteList("certificates", collect, queryParameters);
    }

    @POST
    @Transactional
    @Path("/keys/{id}/temp")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response generateTempValue(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);
        KeyAccessor<SecurityValueWrapper> keyAccessor = deviceService.findAndLockKeyAccessorByIdAndVersion(device, keyAccessorType, securityAccessorInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR));
        keyAccessor.renew();
        return Response.ok(securityAccessorInfoFactory.asKey(keyAccessor)).build();
    }

    @DELETE
    @Transactional
    @Path("/keys/{id}/temp")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response clearTempValue(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);
        KeyAccessor<SecurityValueWrapper> keyAccessor = deviceService.findAndLockKeyAccessorByIdAndVersion(device, keyAccessorType, securityAccessorInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR));
        keyAccessor.clearTempValue();
        return Response.ok(securityAccessorInfoFactory.asKey(keyAccessor)).build();
    }

    @PUT
    @Transactional
    @Path("/keys/{id}/swap")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response swapKeyValues(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);
        KeyAccessor<SecurityValueWrapper> keyAccessor = deviceService.findAndLockKeyAccessorByIdAndVersion(device, keyAccessorType, securityAccessorInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR));
        keyAccessor.swapValues();
        return Response.ok(securityAccessorInfoFactory.asKey(keyAccessor)).build();
    }

    @PUT
    @Transactional
    @Path("/certificates/{id}/swap")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response swapCertificateValues(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId,
                                          @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);
        KeyAccessor<SecurityValueWrapper> keyAccessor = deviceService.findAndLockKeyAccessorByIdAndVersion(device, keyAccessorType, securityAccessorInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR));
        keyAccessor.swapValues();
        return Response.ok(securityAccessorInfoFactory.asCertificate(keyAccessor, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider)).build();
    }

    @PUT
    @Transactional
    @Path("/keys/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response updateOrCreateKeyAccessor(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);

        Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> securityValueWrapperCreator = (keyAccessor1, properties) -> {
            SecurityValueWrapper securityValueWrapper;
            if (keyAccessor1.getKeyAccessorType().getKeyType().getCryptographicType().equals(CryptographicType.Passphrase)) {
                securityValueWrapper = pkiService.newPassphraseWrapper(keyAccessor1.getKeyAccessorType());
            } else {
                securityValueWrapper = pkiService.newSymmetricKeyWrapper(keyAccessor1.getKeyAccessorType());
            }
            securityValueWrapper.setProperties(properties);
            return securityValueWrapper;
        };

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater = (keyAccessor1, properties) -> {
            SecurityValueWrapper actualValue = keyAccessor1.getActualValue().get();
            actualValue.setProperties(properties);
            return actualValue;
        };

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> tempValueUpdater = (keyAccessor1, properties) -> {
            SecurityValueWrapper tempValue = keyAccessor1.getTempValue().get();
            tempValue.setProperties(properties);
            return tempValue;
        };

        KeyAccessor result = keyAccessor.map(ka -> updateKeyAccessor(device, ka, securityAccessorInfo, securityValueWrapperCreator, actualValueUpdater, tempValueUpdater))
                .orElseGet(() -> createKeyAccessor(device, keyAccessorType, securityAccessorInfo, securityValueWrapperCreator));
        if (result==null) {
            result = keyAccessorPlaceHolderProvider.get().init(keyAccessorType, device);
        }
        return Response.ok().entity(securityAccessorInfoFactory.asKey(result)).build();
    }

    @PUT
    @Transactional
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response updateOrCreateCertificateAccessor(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId,
                                                      @BeanParam AliasTypeAheadPropertyValueProvider aliasTypeAheadPropertyValueProvider,
                                                      SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = findKeyAccessorTypeOrThrowException(keyAccessorTypeId, device);

        Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> certificateReferenceGetter = (ignored, properties) -> {
            String alias;
            if (properties.containsKey(ALIAS)) {
                alias = (String) properties.get(ALIAS);
            } else {
                throw new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, ALIAS);
            }
            if (properties.containsKey(TRUST_STORE)) {
                return ((TrustStore)properties.get(TRUST_STORE))
                        .getCertificates()
                        .stream()
                        .filter(cert -> cert.getAlias().equals(alias))
                        .findAny()
                        .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTED_CERTIFICATE, ALIAS));
            } else {
                return pkiService.findCertificateWrapper(alias).orElseThrow(()-> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_CERTIFICATE, ALIAS));
            }
        };

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater = (keyAccessor1, properties) -> {
            SecurityValueWrapper securityValueWrapper = certificateReferenceGetter.apply(keyAccessor1, properties);
            keyAccessor1.setActualValue(securityValueWrapper);
            keyAccessor1.save();
            return securityValueWrapper;
        };

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> tempValueUpdater = (keyAccessor1, properties) -> {
            SecurityValueWrapper securityValueWrapper = certificateReferenceGetter.apply(keyAccessor1, properties);
            keyAccessor1.setTempValue(securityValueWrapper);
            keyAccessor1.save();
            return securityValueWrapper;
        };

        KeyAccessor result = keyAccessor.map(ka -> updateKeyAccessor(device, ka, securityAccessorInfo, certificateReferenceGetter, actualValueUpdater, tempValueUpdater))
                .orElseGet(() -> createKeyAccessor(device, keyAccessorType, securityAccessorInfo, certificateReferenceGetter));
        return Response.ok().entity(securityAccessorInfoFactory.asCertificate(result, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider)).build();
    }

    @GET
    @Path("/certificates/aliases")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList aliasSource(@BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {

        PkiService.AliasSearchFilter aliasSearchFilter = new PkiService.AliasSearchFilter();
        String alias = null;

        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey("alias")) {
            alias = params.getFirst("alias");
        }
        if (uriParams.containsKey("trustStore")) {
            Long trustStoreId = Long.valueOf(params.getFirst("trustStore"));
            aliasSearchFilter.trustStore = pkiService.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUST_STORE, "trustStore"));
        }
        if (alias ==null || alias.isEmpty() ) {
            aliasSearchFilter.alias="*";
        }
        if (alias!=null && !alias.isEmpty()) {
            if (!alias.contains("*") && !alias.contains("?")) {
                aliasSearchFilter.alias="*"+alias+"*";
            } else {
                aliasSearchFilter.alias=alias;
            }
        }
        List<AliasInfo> aliasInfos = AliasInfo.fromAliases(pkiService.getAliasesByFilter(aliasSearchFilter));
        return PagedInfoList.fromPagedList("aliases", aliasInfos, queryParameters);
    }

    private KeyAccessorType findKeyAccessorTypeOrThrowException(@PathParam("id") long keyAccessorTypeId, Device device) {
        return device.getDeviceType()
                .getKeyAccessorTypes()
                .stream()
                .filter(kat -> kat.getId() == keyAccessorTypeId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
    }

    private KeyAccessor<SecurityValueWrapper> createKeyAccessor(Device device, KeyAccessorType keyAccessorType,
                                                                SecurityAccessorInfo securityAccessorInfo,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> securityValueWrapperCreator) {
        List<PropertySpec> propertySpecs = pkiService.getPropertySpecs(keyAccessorType);
        Map<String, Object> tempProperties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.tempProperties);
        Map<String, Object> actualProperties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.currentProperties);
        if (propertiesContainValues(actualProperties) || propertiesContainValues(tempProperties)) {
            KeyAccessor<SecurityValueWrapper> keyAccessor = device.newKeyAccessor(keyAccessorType);
            createActualValueOnKeyAccessor(securityValueWrapperCreator, actualProperties, keyAccessor);
            createTempValueOnKeyAccessor(securityValueWrapperCreator, tempProperties, keyAccessor);
            return keyAccessor;
        }
        return keyAccessorPlaceHolderProvider.get().init(keyAccessorType, device);
    }

    private void createTempValueOnKeyAccessor(BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> securityValueWrapperCreator, Map<String, Object> tempProperties, KeyAccessor<SecurityValueWrapper> keyAccessor) {
        try {
            if (propertiesContainValues(tempProperties)) {
                SecurityValueWrapper securityValueWrapper = securityValueWrapperCreator.apply(keyAccessor, tempProperties);
                keyAccessor.setTempValue(securityValueWrapper);
                keyAccessor.save();
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, TEMP_PROPERTIES);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField(TEMP_PROPERTIES);
        }
    }

    private void createActualValueOnKeyAccessor(BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> securityValueWrapperCreator, Map<String, Object> actualProperties, KeyAccessor<SecurityValueWrapper> keyAccessor) {
        try {
            if (propertiesContainValues(actualProperties)) {
                SecurityValueWrapper securityValueWrapper = securityValueWrapperCreator.apply(keyAccessor, actualProperties);
                keyAccessor.setActualValue(securityValueWrapper);
                keyAccessor.save();
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, CURRENT_PROPERTIES);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField(CURRENT_PROPERTIES);
        }
    }

    private KeyAccessor<SecurityValueWrapper> updateKeyAccessor(Device device, KeyAccessor keyAccessor,
                                                                SecurityAccessorInfo securityAccessorInfo,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> valueCreator,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> tempValueUpdater) {
        KeyAccessor<SecurityValueWrapper> lockedKeyAccessor = deviceService.findAndLockKeyAccessorByIdAndVersion(device, keyAccessor.getKeyAccessorType(), securityAccessorInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR));
        List<PropertySpec> propertySpecs = lockedKeyAccessor.getPropertySpecs();
        createOrUpdateTempValueOnKeyAccessor(lockedKeyAccessor, securityAccessorInfo, valueCreator, tempValueUpdater, propertySpecs);
        Optional<KeyAccessor<SecurityValueWrapper>> result = createOrUpdateActualValueOnKeyAccessor(lockedKeyAccessor, securityAccessorInfo, propertySpecs, actualValueUpdater, valueCreator);

        return result.orElseGet(()->keyAccessorPlaceHolderProvider.get().init(lockedKeyAccessor.getKeyAccessorType(), device));
    }

    private Optional<KeyAccessor<SecurityValueWrapper>> createOrUpdateActualValueOnKeyAccessor(
            KeyAccessor<SecurityValueWrapper> keyAccessor,
            SecurityAccessorInfo securityAccessorInfo,
            List<PropertySpec> propertySpecs,
            BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater,
            BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> valueCreator) {
        try {
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.currentProperties);
            Optional<SecurityValueWrapper> actualValue = keyAccessor.getActualValue();
            if (actualValue.isPresent()) {
                return updateActualValueOnKeyAccessor(keyAccessor, actualValueUpdater, properties, actualValue);
            } else { // this can only mean one thing: there was a temp value, but it may already have been deleted in this call
                return createActualValueOnKeyAccessor(keyAccessor, properties, valueCreator);
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, CURRENT_PROPERTIES);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField(CURRENT_PROPERTIES);
        }
    }

    private Optional<KeyAccessor<SecurityValueWrapper>> createActualValueOnKeyAccessor(
            KeyAccessor<SecurityValueWrapper> keyAccessor, Map<String, Object> properties,
            BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> valueCreator) {
        if (!properties.isEmpty()) {
            SecurityValueWrapper securityValueWrapper = valueCreator.apply(keyAccessor, properties);
            keyAccessor.setActualValue(securityValueWrapper);
            keyAccessor.save();
            return Optional.of(keyAccessor);
        }
        if (keyAccessor.getTempValue().isPresent()) {
            return Optional.of(keyAccessor);
        }
        return Optional.empty();
    }

    private Optional<KeyAccessor<SecurityValueWrapper>> updateActualValueOnKeyAccessor(
            KeyAccessor<SecurityValueWrapper> keyAccessor,
            BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater,
            Map<String, Object> properties, Optional<SecurityValueWrapper> actualValue) {
        if (propertiesContainValues(properties)) {
            if (propertiesDiffer(properties, actualValue.get().getProperties())) {
                actualValueUpdater.apply(keyAccessor, properties);
            }
            return Optional.of(keyAccessor);
        } else {
            if (keyAccessor.getTempValue().isPresent()) {
                keyAccessor.clearActualValue();
                return Optional.of(keyAccessor);
            } else {
                keyAccessor.delete();
                return Optional.empty();
            }
        }
    }

    private void createOrUpdateTempValueOnKeyAccessor(KeyAccessor<SecurityValueWrapper> keyAccessor,
                                                      SecurityAccessorInfo securityAccessorInfo,
                                                      BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> valueCreator,
                                                      BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> tempValueUpdater,
                                                      List<PropertySpec> propertySpecs) {
        try {
            Optional<SecurityValueWrapper> tempValue = keyAccessor.getTempValue();
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.tempProperties);
            if (tempValue.isPresent()) {
                if (propertiesContainValues(properties)) {
                    if (propertiesDiffer(properties, tempValue.get().getProperties())) {
                        tempValueUpdater.apply(keyAccessor, properties);
                    }
                } else {
                    keyAccessor.clearTempValue();
                }
            } else if (!properties.isEmpty()) {
                SecurityValueWrapper securityValueWrapper = valueCreator.apply(keyAccessor, properties);
                keyAccessor.setTempValue(securityValueWrapper);
                keyAccessor.save();
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, TEMP_PROPERTIES);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField(TEMP_PROPERTIES);
        }
    }

    private boolean propertiesDiffer(Map<String, Object> newProperties, Map<String, Object> existingProperties) {
        return !newProperties.equals(existingProperties); // hmm, will equals() work well for all property-types? well, it should, right?
    }

    private Map<String, Object> getPropertiesAsMap(Collection<PropertySpec> propertySpecs, List<PropertyInfo> propertyInfos) {
        Map<String, Object> map = Collections.emptyMap();
        if (propertyInfos.isEmpty()) {
            return map;
        }
        return propertySpecs.stream()
                .filter(ps->mdcPropertyUtils.findPropertyValue(ps, propertyInfos)!=null)
                .collect(
                        toMap(
                                PropertySpec::getName,
                                ps->mdcPropertyUtils.findPropertyValue(ps, propertyInfos)));
    }

    private boolean propertiesContainValues(Map<String, Object> properties) {
        return properties.values().stream().anyMatch(Objects::nonNull);
    }

    private List<SecurityAccessorInfo> getSecurityAccessorInfos(Device device, Predicate<KeyAccessorType> keyAccessorPredicate,
                                                                Function<KeyAccessor, SecurityAccessorInfo> infoCreator) {
        return device.getDeviceType().getKeyAccessorTypes().stream()
                .filter(keyAccessorPredicate)
                .map(kat -> device.getKeyAccessor(kat).orElseGet(() -> keyAccessorPlaceHolderProvider.get().init(kat, device)))
                .map(infoCreator)
                .sorted(Comparator.comparing(ka -> ka.name.toLowerCase()))
                .collect(toList());
    }

    class TrustStoreValuesProvider implements PropertyDefaultValuesProvider {
        @Override
        public List<?> getPropertyPossibleValues(PropertySpec propertySpec, PropertyType propertyType) {
            if (propertySpec.getName().equals("trustStore")) {
                return pkiService.getAllTrustStores();
            }
            return Collections.emptyList();
        }
    };


}
