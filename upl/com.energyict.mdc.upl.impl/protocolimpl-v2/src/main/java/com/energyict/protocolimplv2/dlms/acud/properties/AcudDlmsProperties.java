package com.energyict.protocolimplv2.dlms.acud.properties;


import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class AcudDlmsProperties extends DlmsProperties {

    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH = 4;
    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET = 16;

    public static final String OVERWRITE_SERVER_LOWER_MAC_ADDRESS = "OverwriteServerLowerMacAddress";

    public static final String POST_GATEWAY_CONFIG_URL = "PostGatewayConfigUrl";
    public static final String POST_GATEWAY_FIRMWARE_URL = "PostGatewayFirmwareUrl";
    public static final String GET_GATEWAY_FIRMWARE_VERSION_URL = "GetGatewayFirmwareVersionUrl";
    public static final String GATEWAY_FIRMWARE_PARTITION_SIZE = "GatewayFirmwarePartitionSize";

    public static final String POST_GATEWAY_CONFIG_URL_DEFAULT = "/config";
    public static final String POST_GATEWAY_FIRMWARE_URL_DEFAULT = "/firmware";
    public static final String GET_GATEWAY_FIRMWARE_VERSION_URL_DEFAULT = "/version";
    public static final String GATEWAY_FIRMWARE_PARTITION_SIZE_DEFAULT = "1024";

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

    public String getGatewayConfigUrl() {
        return getProperties().getTypedProperty(POST_GATEWAY_CONFIG_URL, POST_GATEWAY_CONFIG_URL_DEFAULT);
    }

    public String getGatewayFirmwareUrl() {
        return getProperties().getTypedProperty(POST_GATEWAY_FIRMWARE_URL, POST_GATEWAY_FIRMWARE_URL_DEFAULT);
    }

    public String getGatewayFirmwareVersionUrl() {
        return getProperties().getTypedProperty(GET_GATEWAY_FIRMWARE_VERSION_URL, GET_GATEWAY_FIRMWARE_VERSION_URL_DEFAULT);
    }

    public String getGatewayFirmwarePartitionSize() {
        return getProperties().getTypedProperty(GATEWAY_FIRMWARE_PARTITION_SIZE, GATEWAY_FIRMWARE_PARTITION_SIZE_DEFAULT);
    }
}