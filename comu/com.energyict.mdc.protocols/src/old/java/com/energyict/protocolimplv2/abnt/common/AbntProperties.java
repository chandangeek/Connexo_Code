package com.energyict.protocolimplv2.abnt.common;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimplv2.abnt.AbntTranslationKeys;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 17/06/2014 - 13:40
 */
public class AbntProperties extends BasicDynamicPropertySupport {

    public static final String READER_SERIAL_NUMBER_PROPERTY = "ReaderSerialNumber";

    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(50, TimeDuration.TimeUnit.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);
    private static final BigDecimal DEFAULT_READER_SERIAL_NUMBER = BigDecimal.ONE;

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

    public AbntProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.securityPropertySet = deviceProtocolSecurityPropertySet;
        this.properties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
    }

    /**
     * The SerialNumber of the concentrator
     */
    public String getSerialNumber() {
        return getProperties().getTypedProperty(MeterProtocol.SERIALNUMBER);
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
        if (forcedDelay < DEFAULT_FORCED_DELAY.getMilliSeconds()) {
            return DEFAULT_FORCED_DELAY.getMilliSeconds();
        }
        else {
            return forcedDelay;
        }
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

    private PropertySpec readerSerialNumberPropertySpec() {
        return this.getPropertySpecService()
                .bigDecimalSpec()
                .named(READER_SERIAL_NUMBER_PROPERTY, AbntTranslationKeys.READER_SERIAL_NUMBER)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(DEFAULT_READER_SERIAL_NUMBER)
                .finish();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(readerSerialNumberPropertySpec());
        return propertySpecs;
    }

    @Override
    public TimeDuration getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public TimeDuration getDefaultForcedDelay() {
        return DEFAULT_FORCED_DELAY;
    }

    @Override
    public TimeDuration getDefaultDelayAfterError() {
        return DEFAULT_DELAY_AFTER_ERROR;
    }
}