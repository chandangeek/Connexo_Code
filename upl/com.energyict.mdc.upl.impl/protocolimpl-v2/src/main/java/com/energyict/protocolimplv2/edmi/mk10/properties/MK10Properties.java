package com.energyict.protocolimplv2.edmi.mk10.properties;

import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;

/**
 * @author sva
 * @since 23/02/2017 - 16:42
 */
public class MK10Properties {

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";

    TypedProperties properties = TypedProperties.empty();
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

    public MK10Properties() {
    }

    public void addProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        getProperties().setAllProperties(properties);
    }

    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        getProperties().setAllProperties(dialectProperties);
    }

    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getProperties().setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    public MK10ConfigurationSupport.ConnectionMode getConnectionMode() {
        String connectionModeName = (String) getProperties().getProperty(MK10ConfigurationSupport.CONNECTION_MODE);
        return connectionModeName != null ? MK10ConfigurationSupport.ConnectionMode.fromName(connectionModeName) : MK10ConfigurationSupport.DEFAULT_CONNECTION_MODE;
    }

    public boolean preventCrossingIntervalBoundaryWhenReading() {
        return (Boolean) getProperties().getProperty(MK10ConfigurationSupport.PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES, false);
    }

    public TypedProperties getProperties() {
        return properties;
    }

    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet() {
        return deviceProtocolSecurityPropertySet;
    }

    public int getTimeout() {
        return (int) ((Duration) properties.getTypedProperty(TIMEOUT)).toMillis();
    }

    public int getMaxRetries() {
        return ((BigDecimal) properties.getTypedProperty(RETRIES)).intValue();
    }

    public long getforcedDelay() {
        return ((Duration) properties.getTypedProperty(FORCED_DELAY)).toMillis();
    }

    public String getDeviceId() {
        return properties.getTypedProperty(MeterProtocol.Property.ADDRESS.getName());
    }

    public String getPassword() {
        return properties.getTypedProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString());
    }

    public TimeZone getTimeZone() {
        return getProperties().getTypedProperty(TIMEZONE, TimeZone.getTimeZone(DEFAULT_TIMEZONE));
    }
}