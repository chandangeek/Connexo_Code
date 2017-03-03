package com.energyict.protocolimplv2.edmi.mk10.properties;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;

import java.math.BigDecimal;
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

    public void addProperties(TypedProperties properties) {
        getProperties().setAllProperties(properties);
    }

    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
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

    public TypedProperties getProperties() {
        return properties;
    }

    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet() {
        return deviceProtocolSecurityPropertySet;
    }

    public int getTimeout() {
        return (int) ((TimeDuration) properties.getTypedProperty(TIMEOUT)).getMilliSeconds();
    }

    public int getMaxRetries() {
        return ((BigDecimal) properties.getTypedProperty(RETRIES)).intValue();
    }

    public long getforcedDelay() {
        return ((TimeDuration) properties.getTypedProperty(FORCED_DELAY)).getMilliSeconds();
    }

    public String getDeviceId() {
        return properties.getTypedProperty(MeterProtocol.ADDRESS);
    }

    public String getPassword() {
        return properties.getTypedProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec().getName());
    }

    public TimeZone getTimeZone() {
        TimeZoneInUse timeZoneInUse = getProperties().getTypedProperty(TIMEZONE);
        if (timeZoneInUse == null || timeZoneInUse.getTimeZone() == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return ProtocolUtils.getWinterTimeZone(timeZoneInUse.getTimeZone());    // Convert to winter timezone, as all dates are transmitted in winter timezone
        }
    }
}