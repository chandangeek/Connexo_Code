package com.energyict.protocolimplv2.dlms.as3000.properties;

import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.common.properties.DlmsPropertiesFrameCounterSupport;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;

public class AS3000Properties extends DlmsPropertiesFrameCounterSupport {

    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH = 2;
    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET = 16;

    public static final String OVERWRITE_SERVER_LOWER_MAC_ADDRESS = "OverwriteServerLowerMacAddress";

    public ObisCode frameCounterObisCode() {
        return FrameCounterProvider.getDefaultObisCode();
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(AS3000ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new NTASecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    @Override
    public int getServerLowerMacAddress() {
        if (!isOverwriteServerLowerMacAddress()) {
            return super.getServerLowerMacAddress();
        }
        return createServerLowerMacAddress();
    }

    private int createServerLowerMacAddress() {
        String serialNumber = getSerialNumber();
        if (serialNumber != null && serialNumber.length() >= SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH) {
            try {
                String macAddress = serialNumber.substring(serialNumber.length() - SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH);
                return Integer.parseInt(macAddress) + SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET;
            } catch (NumberFormatException e) {
            }
        }
        throw DeviceConfigurationException.invalidPropertyFormat(DlmsProtocolProperties.SYSTEM_IDENTIFIER, serialNumber, "Last " + SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH + " characters should be a number");
    }

    public boolean isOverwriteServerLowerMacAddress() {
        return getProperties().<Boolean>getTypedProperty(OVERWRITE_SERVER_LOWER_MAC_ADDRESS, false);
    }

    public boolean flushCachedObjectList() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    public int getMaxDaysToReadLoadProfile() {
        return getProperties().getTypedProperty(
                AS3000ConfigurationSupport.LIMIT_MAX_NR_OF_DAYS_PROPERTY,
                BigDecimal.valueOf(30)
        ).intValue();
    }
}
