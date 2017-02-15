package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DELAY_AFTER_ERROR;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;

/**
 * @author sva
 * @since 17/06/2014 - 13:40
 */
public class AbntProperties implements HasDynamicProperties {

    public static final String READER_SERIAL_NUMBER_PROPERTY = "ReaderSerialNumber";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(50);
    public static final Duration DEFAULT_DELAY_AFTER_ERROR = Duration.ofMillis(100);
    private static final BigDecimal DEFAULT_READER_SERIAL_NUMBER = new BigDecimal(1);
    private final PropertySpecService propertySpecService;
    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

    public AbntProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    /**
     * The security set of a device. It contains all properties related to security.
     */
    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet;
    }

    /**
     * Setter for the device's security set
     */
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.securityPropertySet = deviceProtocolSecurityPropertySet;
        this.properties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
    }

    /**
     * The SerialNumber of the concentrator
     */
    public String getSerialNumber() {
        return getProperties().getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
    }

    public int getReaderSerialNumber() {
        return getProperties().getTypedProperty(READER_SERIAL_NUMBER_PROPERTY, DEFAULT_READER_SERIAL_NUMBER).intValue();
    }

    /**
     * The device timezone
     */
    public TimeZone getTimeZone() {
        TimeZone timeZone = getProperties().getTypedProperty(TIMEZONE);
        if (timeZone == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return timeZone;
        }
    }

    /**
     * The timeout interval of the communication session, expressed in milliseconds
     */
    public long getTimeout() {
        return getProperties().getTypedProperty(TIMEOUT, DEFAULT_TIMEOUT).toMillis();
    }

    /**
     * The number of retries
     */
    public int getRetries() {
        return parseBigDecimalProperty(RETRIES, DEFAULT_RETRIES);
    }

    /**
     * The delay before sending the requests, expressed in milliseconds
     */
    public long getForcedDelay() {
        long forcedDelay = getProperties().getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY).toMillis();
        if (forcedDelay < DEFAULT_FORCED_DELAY.toMillis()) {
            return DEFAULT_FORCED_DELAY.toMillis();
        } else {
            return forcedDelay;
        }
    }

    public long getDelayAfterError() {
        return getProperties().getTypedProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR).toMillis();
    }

    private int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        return getProperties().getTypedProperty(key, defaultValue).intValue();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.addProperties(TypedProperties.copyOf(properties));
    }

    public void addProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        this.addProperties(TypedProperties.copyOf(properties));
    }

    private void addProperties(TypedProperties properties) {
        if (properties.getInheritedProperties() != null) {
            addProperties(properties.getInheritedProperties());
        }
        this.getProperties().setAllProperties(properties);
    }

    /**
     * Return all properties
     */
    public TypedProperties getProperties() {
        if (this.properties == null) {
            this.properties = TypedProperties.empty();
        }
        return this.properties;
    }


    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeZonePropertySpec(),
                this.readerSerialNumberPropertySpec()
        );
    }

    private PropertySpec timeZonePropertySpec() {
        return propertySpecService
                .timeZoneSpec()
                .named(TIMEZONE, TIMEZONE)
                .describedAs("Description for " + TIMEZONE)
                .finish();
    }

    private PropertySpec readerSerialNumberPropertySpec() {
        return this.bigDecimalSpec(READER_SERIAL_NUMBER_PROPERTY, false, DEFAULT_READER_SERIAL_NUMBER, PropertyTranslationKeys.V2_ABNT_READER_SERIAL_NUMBER);
    }

    private PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }
}