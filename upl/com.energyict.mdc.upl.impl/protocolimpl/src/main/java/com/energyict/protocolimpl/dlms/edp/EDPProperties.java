package com.energyict.protocolimpl.dlms.edp;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

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
class EDPProperties {

    private static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    private static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    private static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    private static final String READCACHE_PROPERTY = "ReadCache";
    private static final String READ_CACHE_DEFAULT_VALUE = "0";
    private static final int FIRMWARE_CLIENT = 3;

    private final Properties properties;
    private final PropertySpecService propertySpecService;

    EDPProperties(Properties properties, PropertySpecService propertySpecService) {
        this.properties = properties;
        this.propertySpecService = propertySpecService;
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

    protected List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, PropertyTranslationKeys.DLMS_CLIENT_MAC_ADDRESS),
                this.stringSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS),
                this.stringSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS),
                this.stringSpec(DlmsProtocolProperties.CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION),
                this.stringSpec(DlmsProtocolProperties.PK_TIMEOUT, PropertyTranslationKeys.DLMS_TIMEOUT),
                this.stringSpec(DlmsProtocolProperties.PK_RETRIES, PropertyTranslationKeys.DLMS_RETRIES),
                this.stringSpec(READCACHE_PROPERTY, PropertyTranslationKeys.DLMS_READ_CACHE));
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, this.propertySpecService::stringSpec).finish();
    }

    boolean isFirmwareClient() {
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

    int getServerUpperMacAddress() {
        try {
            return Integer.parseInt(properties.getProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    int getServerLowerMacAddress() {
        try {
            return Integer.parseInt(properties.getProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, "16"));
        } catch (NumberFormatException e) {
            return 16;
        }
    }
}