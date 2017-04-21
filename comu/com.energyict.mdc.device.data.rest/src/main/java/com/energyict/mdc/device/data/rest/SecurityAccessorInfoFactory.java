/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.rest.impl.SecurityAccessorInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;

import javax.inject.Inject;
import java.util.List;

public class SecurityAccessorInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;

    @Inject
    public SecurityAccessorInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
    }

    private SecurityAccessorInfo from(KeyAccessor<?> keyAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = keyAccessor.getKeyAccessorType().getId();
        info.name = keyAccessor.getKeyAccessorType().getName();
        info.description = keyAccessor.getKeyAccessorType().getDescription();
        info.swapped = keyAccessor.isSwapped();
        info.version = keyAccessor.getVersion();
        info.modificationDate = keyAccessor.getModTime();
        info.status = thesaurus.getFormat(keyAccessor.getStatus()).format();
        keyAccessor.getActualValue().getExpirationTime().ifPresent(expiration -> info.expirationTime = expiration);

        return info;
    }

    public SecurityAccessorInfo asKey(KeyAccessor<?> keyAccessor) {
        SecurityAccessorInfo info = from(keyAccessor);
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();

        TypedProperties actualTypedProperties = getPropertiesActualValue(keyAccessor);
        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties);

        TypedProperties tempTypedProperties = getPropertiesTempValue(keyAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties);
        return info;
    }

    public SecurityAccessorInfo asCertificate(KeyAccessor<?> keyAccessor, PropertyValuesResourceProvider aliasTypeAheadPropertyValueProvider, PropertyDefaultValuesProvider trustStoreValuesProvider) {
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();
        SecurityAccessorInfo info = from(keyAccessor);
        TypedProperties actualTypedProperties = getPropertiesActualValue(keyAccessor);

        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);

        TypedProperties tempTypedProperties = getPropertiesTempValue(keyAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);

        if (keyAccessor instanceof CertificateAccessor) {
            ((CertificateAccessor)keyAccessor).getActualValue().getLastReadDate().ifPresent(date -> info.lastReadDate = date);
        }

        return info;
    }

    private TypedProperties getPropertiesTempValue(KeyAccessor<?> keyAccessor) {
        TypedProperties tempTypedProperties = TypedProperties.empty();
        keyAccessor.getTempValue().ifPresent(ka->ka.getProperties().entrySet().forEach(e->tempTypedProperties.setProperty(e.getKey(),e.getValue())));
        return tempTypedProperties;
    }

    private TypedProperties getPropertiesActualValue(KeyAccessor<?> keyAccessor) {
        TypedProperties actualTypedProperties = TypedProperties.empty();
        keyAccessor.getActualValue()
                .getProperties().entrySet().forEach(e1 -> actualTypedProperties.setProperty(e1.getKey(), e1.getValue()));
        return actualTypedProperties;
    }

}