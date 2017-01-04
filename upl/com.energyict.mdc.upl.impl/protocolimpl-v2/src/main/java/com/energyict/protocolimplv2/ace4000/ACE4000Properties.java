package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:56
 */
public class ACE4000Properties {

    public static final String TIMEOUT = DeviceProtocol.Property.TIMEOUT.getName();
    public static final String RETRIES = DeviceProtocol.Property.RETRIES.getName();
    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal("30000");
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal("3");

    public Properties properties;

    ACE4000Properties() {
        super();
        this.properties = new Properties();
    }

    private ACE4000Properties(TypedProperties properties) {
        this();
        this.copyProperties(properties);
    }

    private void copyProperties(TypedProperties properties) {
        this.copyPropertyValue(properties, TIMEOUT);
        this.copyPropertyValue(properties, RETRIES);
    }

    private void copyPropertyValue(TypedProperties properties, String propertyName) {
        Object propertyValue = properties.getProperty(TIMEOUT);
        if (propertyValue != null) {
            this.properties.put(propertyName, propertyValue);
        }
    }

    void setAllProperties(TypedProperties properties) {
        this.copyProperties(properties);
    }

    void setAllProperties(Properties properties) {
        this.properties = new Properties(properties);
    }

    List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec());
    }

    private PropertySpec timeoutPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(TIMEOUT, false, DEFAULT_TIMEOUT);
    }

    private PropertySpec retriesPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(RETRIES, false, DEFAULT_RETRIES);
    }

    List<PropertySpec> getOptionalKeys() {
        return Collections.singletonList(UPLPropertySpecFactory.bigDecimal(TIMEOUT, false));
    }

    List<PropertySpec> getRequiredKeys() {
        return Collections.emptyList();
    }

    public int getTimeout() {
        return this.getIntegerProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    public int getRetries() {
        return this.getIntegerProperty(RETRIES, DEFAULT_RETRIES);
    }

    private int getIntegerProperty(String name, BigDecimal defaultValue) {
        Object value = this.properties.get(name);
        if (value == null) {
            return defaultValue.intValue();
        } else {
            return ((BigDecimal) value).intValue();
        }
    }

}