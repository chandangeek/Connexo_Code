package com.energyict.protocolimplv2.edmi.mk10.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.protocolimplv2.edmi.dialects.CommonEDMIDeviceProtocolDialect;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

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

    private TypedProperties properties = TypedProperties.empty();
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

    public MK10Properties() {
    }

    public void addProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        getProperties().setAllProperties(properties);
    }

    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getProperties().setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    public CommonEDMIDeviceProtocolDialect.ConnectionMode getConnectionMode() {
        String connectionModeName = (String) getProperties().getProperty(CommonEDMIDeviceProtocolDialect.CONNECTION_MODE);
        return CommonEDMIDeviceProtocolDialect.ConnectionMode.fromName(connectionModeName);
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
        return (int) ((Duration) properties.getTypedProperty(DlmsProtocolProperties.TIMEOUT)).toMillis();
    }

    public int getMaxRetries() {
        return ((BigDecimal) properties.getTypedProperty(DlmsProtocolProperties.RETRIES)).intValue();
    }

    public long getForcedDelay() {
        return ((Duration) properties.getTypedProperty(DlmsProtocolProperties.FORCED_DELAY)).toMillis();
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
