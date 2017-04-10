/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.PathPrependingConstraintViolationException;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SecurityAccessorResource {
    private final Set<CryptographicType> CERTIFICATES = EnumSet.of(CryptographicType.Certificate, CryptographicType.ClientCertificate, CryptographicType.TrustedCertificate);
    private final Set<CryptographicType> KEYS = EnumSet.of(CryptographicType.SymmetricKey, CryptographicType.Passphrase);

    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final PkiService pkiService;
    private final ResourceHelper resourceHelper;
    private final Provider<KeyAccessorPlaceHolder> keyAccessorPlaceHolderProvider;
    private final ExceptionFactory exceptionFactory;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public SecurityAccessorResource(ResourceHelper resourceHelper,
                                    SecurityAccessorInfoFactory securityAccessorInfoFactory,
                                    PkiService pkiService,
                                    Provider<KeyAccessorPlaceHolder> keyAccessorPlaceHolderProvider,
                                    ExceptionFactory exceptionFactory,
                                    MdcPropertyUtils mdcPropertyUtils) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.resourceHelper = resourceHelper;
        this.pkiService = pkiService;
        this.keyAccessorPlaceHolderProvider = keyAccessorPlaceHolderProvider;
        this.exceptionFactory = exceptionFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Transactional
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getKeys(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<SecurityAccessorInfo> collect = getSecurityAccessorInfos(device, kat -> KEYS.contains(kat.getKeyType().getCryptographicType()));
        return PagedInfoList.fromCompleteList("keys", collect, queryParameters);
    }

    @GET
    @Transactional
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getCertificates(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<SecurityAccessorInfo> collect = getSecurityAccessorInfos(device, kat -> CERTIFICATES.contains(kat.getKeyType().getCryptographicType()));
        return PagedInfoList.fromCompleteList("certificates", collect, queryParameters);
    }

    @PUT
    @Transactional
    @Path("/keys/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response updateOrCreateKeyAccessor(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = device.getDeviceType()
                .getKeyAccessorTypes()
                .stream()
                .filter(kat -> kat.getId() == keyAccessorTypeId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));

        Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);
        KeyAccessor result = keyAccessor.map(ka -> updateKeyAccessor(ka, securityAccessorInfo))
                .orElseGet(() -> createKeyAccessor(device, keyAccessorType, securityAccessorInfo));
        return Response.ok().entity(securityAccessorInfoFactory.from(result)).build();
    }

    private KeyAccessor<SecurityValueWrapper> createKeyAccessor(Device device, KeyAccessorType keyAccessorType, SecurityAccessorInfo securityAccessorInfo) {
        KeyAccessor<SecurityValueWrapper> keyAccessor = device.newKeyAccessor(keyAccessorType);
        List<PropertySpec> propertySpecs = pkiService.getPropertySpecs(keyAccessorType);
        try {
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.currentProperties);
            if (propertiesContainValues(properties)) {
                createActualValue(keyAccessor, properties);
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, "currentProperties");
        }
        try {
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.tempProperties);
            if (propertiesContainValues(properties)) {
                createTempValue(keyAccessor, properties);
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, "tempProperties");
        }
        return keyAccessor;
    }

    private void createActualValue(KeyAccessor keyAccessor, Map<String, Object> properties) {
        SymmetricKeyWrapper symmetricKeyWrapper = pkiService.newSymmetricKeyWrapper(keyAccessor.getKeyAccessorType());
        symmetricKeyWrapper.setProperties(properties);
        keyAccessor.setActualValue(symmetricKeyWrapper);
        keyAccessor.save();
    }

    private KeyAccessor<SecurityValueWrapper> updateKeyAccessor(KeyAccessor keyAccessor, SecurityAccessorInfo securityAccessorInfo) {
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();
        try {
            Optional<SecurityValueWrapper> tempValue = keyAccessor.getTempValue();
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.tempProperties);
            if (tempValue.isPresent()) {
                if (propertiesContainValues(properties) && propertiesDiffer(properties, tempValue.get().getProperties())) {
                    updateTempValue(tempValue.get(), properties);
                } else {
                    keyAccessor.clearTempValue();
                }
            } else if (!properties.isEmpty()) {
                createTempValue(keyAccessor, properties);
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, "tempProperties");
        }

        try {
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.currentProperties);
            if (propertiesContainValues(properties)) {
                if  (propertiesDiffer(properties, keyAccessor.getActualValue().getProperties())) {
                    updateActualValue(keyAccessor, properties);
                }
            } else {
                keyAccessor.delete();
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, "currentProperties");
        }


        return keyAccessor;
    }

    private boolean propertiesDiffer(Map<String, Object> newProperties, Map<String, Object> existingProperties) {
        return !newProperties.equals(existingProperties); // hmm, will equals() work well for all property-types? well, it should
    }

    private void createTempValue(KeyAccessor keyAccessor, Map<String, Object> properties) {
        SymmetricKeyWrapper symmetricKeyWrapper = pkiService.newSymmetricKeyWrapper(keyAccessor.getKeyAccessorType());
        symmetricKeyWrapper.setProperties(properties);
        keyAccessor.setTempValue(symmetricKeyWrapper);
        keyAccessor.save();
    }

    private void updateTempValue(SecurityValueWrapper tempValueWrapper, Map<String, Object> properties) {
        tempValueWrapper.setProperties(properties);
    }

    private void updateActualValue(KeyAccessor keyAccessor, Map<String, Object> properties) {
        SecurityValueWrapper actualValue = keyAccessor.getActualValue();
        actualValue.setProperties(properties);
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

    private List<SecurityAccessorInfo> getSecurityAccessorInfos(Device device, Predicate<KeyAccessorType> keyAccessorPredicate) {
        return device.getDeviceType().getKeyAccessorTypes().stream()
                .filter(keyAccessorPredicate)
                .map(kat -> device.getKeyAccessor(kat).orElseGet(() -> keyAccessorPlaceHolderProvider.get().init(kat, device)))
                .map(securityAccessorInfoFactory::from)
                .sorted(Comparator.comparing(ka -> ka.name.toLowerCase()))
                .collect(toList());
    }

}
