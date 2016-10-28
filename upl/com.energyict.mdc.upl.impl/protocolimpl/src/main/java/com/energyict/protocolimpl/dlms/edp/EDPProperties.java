package com.energyict.protocolimpl.dlms.edp;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Wrapper class that holds the EDP DLMS protocol properties, parses them and returns the proper values.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:11
 * Author: khe
 */
public class EDPProperties {

    private static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    private static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    private static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    private static final String READCACHE_PROPERTY = "ReadCache";
    private static final String READ_CACHE_DEFAULT_VALUE = "0";
    private static final int FIRMWARE_CLIENT = 3;

    private final Properties properties;

    public EDPProperties(Properties properties) {
        this.properties = properties;
    }

    public void addProperties(Properties properties) {
        this.properties.putAll(properties);
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return Integer.parseInt(properties.getProperty(READCACHE_PROPERTY, READ_CACHE_DEFAULT_VALUE).trim()) == 1;
    }

    protected List<String> getOptionalKeys() {
        return Arrays.asList(
                    DlmsProtocolProperties.CLIENT_MAC_ADDRESS,
                    PROPNAME_SERVER_UPPER_MAC_ADDRESS,
                    PROPNAME_SERVER_LOWER_MAC_ADDRESS,
                    DlmsProtocolProperties.CONNECTION,
                    DlmsProtocolProperties.TIMEOUT,
                    DlmsProtocolProperties.RETRIES,
                    READCACHE_PROPERTY);
    }

    public boolean isFirmwareClient() {
        String property = properties.getProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, DlmsProtocolProperties.DEFAULT_CLIENT_MAC_ADDRESS);
        try {
            return Integer.parseInt(property) == FIRMWARE_CLIENT;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int getClientMacAddress() {
        try {
            return Integer.parseInt(properties.getProperty(PROPNAME_CLIENT_MAC_ADDRESS, "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public int getServerUpperMacAddress() {
        try {
            return Integer.parseInt(properties.getProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public int getServerLowerMacAddress() {
        try {
            return Integer.parseInt(properties.getProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, "16"));
        } catch (NumberFormatException e) {
            return 16;
        }
    }
}