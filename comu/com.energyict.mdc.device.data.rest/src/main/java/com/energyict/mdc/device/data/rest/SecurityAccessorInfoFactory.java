/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
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
        keyAccessor.getActualValue().getExpirationTime().ifPresent(experation -> info.expirationTime = experation);

        info.currentProperties = getProperties(keyAccessor.getActualValue());
        keyAccessor.getTempValue().ifPresent(value -> info.tempProperties = getProperties(value));

        return info;
    }

    private List<PropertyInfo> getProperties(SecurityValueWrapper keyAccessor) {
        TypedProperties empty = TypedProperties.empty();
        keyAccessor.getProperties().entrySet().forEach(e->empty.setProperty(e.getKey(),e.getValue()));

        return mdcPropertyUtils.convertPropertySpecsToPropertyInfos(keyAccessor.getPropertySpecs(), empty);
    }
}