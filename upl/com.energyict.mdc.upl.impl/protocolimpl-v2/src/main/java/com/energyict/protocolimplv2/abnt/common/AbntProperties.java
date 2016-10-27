package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.protocol.MeterProtocol;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
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
public class AbntProperties implements ConfigurationSupport {

    public static final String READER_SERIAL_NUMBER_PROPERTY = "ReaderSerialNumber";
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(50, TimeDuration.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(100, TimeDuration.MILLISECONDS);
    private static final BigDecimal DEFAULT_READER_SERIAL_NUMBER = new BigDecimal(1);

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

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
        return getProperties().getTypedProperty(MeterProtocol.Property.SERIALNUMBER.getName());
    }

    public int getReaderSerialNumber() {
        return getProperties().getTypedProperty(READER_SERIAL_NUMBER_PROPERTY, DEFAULT_READER_SERIAL_NUMBER).intValue();
    }

    /**
     * The device timezone
     */
    public TimeZone getTimeZone() {
        TimeZoneInUse timeZoneInUse = getProperties().getTypedProperty(TIMEZONE);
        if (timeZoneInUse == null || timeZoneInUse.getTimeZone() == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return timeZoneInUse.getTimeZone();
        }
    }

    /**
     * The timeout interval of the communication session, expressed in milliseconds
     */
    public long getTimeout() {
        return getProperties().getTypedProperty(TIMEOUT, DEFAULT_TIMEOUT).getMilliSeconds();
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
        long forcedDelay = getProperties().getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY).getMilliSeconds();
        return (forcedDelay < DEFAULT_FORCED_DELAY.getMilliSeconds())
                ? DEFAULT_FORCED_DELAY.getMilliSeconds()
                : forcedDelay;
    }

    public long getDelayAfterError() {
        return getProperties().getTypedProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR).getMilliSeconds();
    }

    private int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        return getProperties().getTypedProperty(key, defaultValue).intValue();
    }

    /**
     * Add properties
     */
    public void addProperties(TypedProperties properties) {
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
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.timeZonePropertySpec(),
                this.readerSerialNumberPropertySpec()
        );
    }

    private PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }

    private PropertySpec readerSerialNumberPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(READER_SERIAL_NUMBER_PROPERTY, DEFAULT_READER_SERIAL_NUMBER);
    }
}