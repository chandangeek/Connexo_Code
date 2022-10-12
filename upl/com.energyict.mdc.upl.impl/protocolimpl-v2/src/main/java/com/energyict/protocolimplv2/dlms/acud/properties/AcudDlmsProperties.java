package com.energyict.protocolimplv2.dlms.acud.properties;


import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class AcudDlmsProperties extends DlmsProperties {

    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH = 4;
    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET = 16;

    public static final String OVERWRITE_SERVER_LOWER_MAC_ADDRESS = "OverwriteServerLowerMacAddress";

    @Override
    public byte[] getSystemIdentifier() {
        return null;
    }

    @Override
    public int getServerLowerMacAddress() {
        if (isOverwriteServerLowerMacAddress())
            return super.getServerLowerMacAddress();
        return createServerLowerMacAddress();
    }

    private int createServerLowerMacAddress() {
        String serialNumber = getSerialNumber();
        if (serialNumber != null && serialNumber.length() >= SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH)
            try {
                String macAddress = serialNumber.substring(serialNumber.length() - SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH);
                return Integer.parseInt(macAddress) + SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET;
            } catch (NumberFormatException e) {
            }
        throw DeviceConfigurationException.invalidPropertyFormat(DlmsProtocolProperties.SYSTEM_IDENTIFIER, serialNumber, "Last " + SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH + " characters should be a number");
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
    }

    public boolean isOverwriteServerLowerMacAddress() {
        return getProperties().<Boolean>getTypedProperty(OVERWRITE_SERVER_LOWER_MAC_ADDRESS, false);
    }

    public boolean useUndefinedAsTimeDeviation() {
        return getProperties().<Boolean>getTypedProperty(IDISConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false);
    }
}