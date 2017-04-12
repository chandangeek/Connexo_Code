/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.rest.impl.SecurityAccessorInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import java.util.List;

public class SecurityAccessorInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public SecurityAccessorInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public SecurityAccessorInfo from(KeyAccessor<?> keyAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = keyAccessor.getKeyAccessorType().getId();
        info.name = keyAccessor.getKeyAccessorType().getName();
        info.description = keyAccessor.getKeyAccessorType().getDescription();
        info.swapped = keyAccessor.isSwapped();
        info.version = keyAccessor.getVersion();
        info.modificationDate = keyAccessor.getModTime();
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();
        keyAccessor.getActualValue().getExpirationTime().ifPresent(expiration -> info.expirationTime = expiration);

        TypedProperties actualTypedProperties = TypedProperties.empty();
        keyAccessor.getActualValue()
                .getProperties().entrySet().forEach(e1 -> actualTypedProperties.setProperty(e1.getKey(), e1.getValue()));
        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties);

        TypedProperties tempTypedProperties = TypedProperties.empty();
        keyAccessor.getTempValue().ifPresent(ka->ka.getProperties().entrySet().forEach(e->tempTypedProperties.setProperty(e.getKey(),e.getValue())));
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties);

        if (keyAccessor instanceof CertificateAccessor) {
            ((CertificateAccessor)keyAccessor).getActualValue().getLastReadDate().ifPresent(date -> info.lastReadDate = date);
        }

        return info;
    }

}