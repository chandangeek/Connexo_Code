package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:56
 */
public class ACE4000Properties {

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal("30000");
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal("3");

    public TypedProperties properties;

    public ACE4000Properties() {
        this(TypedProperties.empty());
    }

    public ACE4000Properties(TypedProperties properties) {
        this.properties = properties;
    }

    public List<PropertySpec> getPropertySpecs () {
        PropertySpecService propertySpecService = Bus.getPropertySpecService();
        List<PropertySpec> optional = new ArrayList<>();
        optional.add(propertySpecService.basicPropertySpec(TIMEOUT, false, new BigDecimalFactory()));
        optional.add(propertySpecService.basicPropertySpec(RETRIES, false, new BigDecimalFactory()));
        return optional;
    }

    public int getTimeout() {
        return properties.getIntegerProperty(TIMEOUT, DEFAULT_TIMEOUT).intValue();
    }

    public int getRetries() {
        return properties.getIntegerProperty(RETRIES, DEFAULT_RETRIES).intValue();
    }

}