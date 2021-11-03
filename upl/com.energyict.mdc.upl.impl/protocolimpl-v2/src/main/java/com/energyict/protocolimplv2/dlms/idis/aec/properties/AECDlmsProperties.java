package com.energyict.protocolimplv2.dlms.idis.aec.properties;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_UPPER_SERVER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS;

public class AECDlmsProperties extends AM540Properties {
    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH = 4;
    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET = 16;

    public static final String OVERWRITE_SERVER_LOWER_MAC_ADDRESS = "OverwriteServerLowerMacAddress";

    public AECDlmsProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }


    @Override
    public byte[] getSystemIdentifier() {
        return null;
    }

    @Override
    public int getServerUpperMacAddress() {
        return parseBigDecimalProperty(SERVER_UPPER_MAC_ADDRESS, DEFAULT_UPPER_SERVER_MAC_ADDRESS);
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

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
    }

    public boolean isOverwriteServerLowerMacAddress() {
        return getProperties().<Boolean>getTypedProperty(OVERWRITE_SERVER_LOWER_MAC_ADDRESS, true);
    }
}

