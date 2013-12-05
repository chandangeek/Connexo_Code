package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.OptionalPropertySpecFactory;

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
        List<PropertySpec> optional = new ArrayList<>();
        optional.add(OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(TIMEOUT));
        optional.add(OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(RETRIES));
        return optional;
    }

    public int getTimeout() {
        return properties.getIntegerProperty(TIMEOUT, DEFAULT_TIMEOUT).intValue();
    }

    public int getRetries() {
        return properties.getIntegerProperty(RETRIES, DEFAULT_RETRIES).intValue();
    }
}