/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
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
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> securityValueWrapperCreator = (keyAccessor1, properties) -> {
            SymmetricKeyWrapper symmetricKeyWrapper = pkiService.newSymmetricKeyWrapper(keyAccessor1.getKeyAccessorType());
            symmetricKeyWrapper.setProperties(properties);
            return symmetricKeyWrapper;
        };

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater = (keyAccessor1, properties) -> {
            SecurityValueWrapper actualValue = keyAccessor1.getActualValue();
            actualValue.setProperties(properties);
            return actualValue;
        };

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> tempValueUpdater = (keyAccessor1, properties) -> {
            SecurityValueWrapper tempValue = keyAccessor1.getTempValue().get();
            tempValue.setProperties(properties);
            return tempValue;
        };

        KeyAccessor result = keyAccessor.map(ka -> updateKeyAccessor(ka, securityAccessorInfo, securityValueWrapperCreator, actualValueUpdater, tempValueUpdater))
                .orElseGet(() -> createKeyAccessor(device, keyAccessorType, securityAccessorInfo, securityValueWrapperCreator));
        if (result==null) {
            result = keyAccessorPlaceHolderProvider.get().init(keyAccessorType, device);
        }
        return Response.ok().entity(securityAccessorInfoFactory.from(result)).build();
    }

    @PUT
    @Transactional
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response updateOrCreateCertificateAccessor(@PathParam("name") String deviceName, @PathParam("id") long keyAccessorTypeId, SecurityAccessorInfo securityAccessorInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        KeyAccessorType keyAccessorType = device.getDeviceType()
                .getKeyAccessorTypes()
                .stream()
                .filter(kat -> kat.getId() == keyAccessorTypeId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));

        Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);

        BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> certificateReferenceGetter = (ignored, properties) -> {
            String alias;
            if (properties.containsKey(ALIAS)) {
                alias = (String) properties.get(ALIAS);
            } else {
                throw new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, ALIAS);
            }
            if (properties.containsKey(TRUST_STORE)) {
                return pkiService.findTrustStore((String) properties.get(TRUST_STORE))
                        .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUST_STORE, TRUST_STORE))
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

        KeyAccessor result = keyAccessor.map(ka -> updateKeyAccessor(ka, securityAccessorInfo, certificateReferenceGetter, actualValueUpdater, tempValueUpdater))
                .orElseGet(() -> createKeyAccessor(device, keyAccessorType, securityAccessorInfo, certificateReferenceGetter));
        return Response.ok().entity(securityAccessorInfoFactory.from(result)).build();
    }

    private KeyAccessor<SecurityValueWrapper> createKeyAccessor(Device device, KeyAccessorType keyAccessorType,
                                                                SecurityAccessorInfo securityAccessorInfo,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> securityValueWrapperCreator) {
        List<PropertySpec> propertySpecs = pkiService.getPropertySpecs(keyAccessorType);
        Map<String, Object> tempProperties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.tempProperties);
        Map<String, Object> actualProperties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.currentProperties);
        if (propertiesContainValues(actualProperties) || propertiesContainValues(tempProperties)) {
            KeyAccessor<SecurityValueWrapper> keyAccessor = device.newKeyAccessor(keyAccessorType);
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
            return keyAccessor;
        }
        return keyAccessorPlaceHolderProvider.get().init(keyAccessorType, device);
    }

    private KeyAccessor<SecurityValueWrapper> updateKeyAccessor(KeyAccessor keyAccessor,
                                                                SecurityAccessorInfo securityAccessorInfo,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> valueCreator,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater,
                                                                BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> tempValueUpdater) {
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();
        updateKeyAccessorTempValue(keyAccessor, securityAccessorInfo, valueCreator, tempValueUpdater, propertySpecs);
        updateKeyAccessorActualValue(keyAccessor, securityAccessorInfo, propertySpecs, actualValueUpdater);

        return keyAccessor;
    }

    private void updateKeyAccessorActualValue(KeyAccessor<SecurityValueWrapper> keyAccessor, SecurityAccessorInfo securityAccessorInfo,
                                              List<PropertySpec> propertySpecs,
                                              BiFunction<KeyAccessor<SecurityValueWrapper>, Map<String, Object>, SecurityValueWrapper> actualValueUpdater) {
        try {
            Map<String, Object> properties = getPropertiesAsMap(propertySpecs, securityAccessorInfo.currentProperties);
            if (propertiesContainValues(properties)) {
                if (propertiesDiffer(properties, keyAccessor.getActualValue().getProperties())) {
                    actualValueUpdater.apply(keyAccessor, properties);
                }
            } else {
                keyAccessor.delete();
            }
        } catch (ConstraintViolationException e) {
            throw new PathPrependingConstraintViolationException(e, CURRENT_PROPERTIES);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField(CURRENT_PROPERTIES);
        }
    }

    private void updateKeyAccessorTempValue(KeyAccessor<SecurityValueWrapper> keyAccessor,
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

    private List<SecurityAccessorInfo> getSecurityAccessorInfos(Device device, Predicate<KeyAccessorType> keyAccessorPredicate) {
        return device.getDeviceType().getKeyAccessorTypes().stream()
                .filter(keyAccessorPredicate)
                .map(kat -> device.getKeyAccessor(kat).orElseGet(() -> keyAccessorPlaceHolderProvider.get().init(kat, device)))
                .map(securityAccessorInfoFactory::from)
                .sorted(Comparator.comparing(ka -> ka.name.toLowerCase()))
                .collect(toList());
    }

}
