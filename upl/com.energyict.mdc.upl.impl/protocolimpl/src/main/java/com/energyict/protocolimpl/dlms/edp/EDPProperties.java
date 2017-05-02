package com.energyict.protocolimpl.dlms.edp;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper class that holds the EDP DLMS protocol properties, parses them and returns the proper values.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:11
 * Author: khe
 */
class EDPProperties {

    public  static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";
    public  static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    public  static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    public  static final String READCACHE_PROPERTY = "ReadCache";
    public  static final String READ_CACHE_DEFAULT_VALUE = "0";
    private static final int FIRMWARE_CLIENT = 3;

    private final TypedProperties properties;
    private final PropertySpecService propertySpecService;

    EDPProperties(PropertySpecService propertySpecService) {
        this(com.energyict.protocolimpl.properties.TypedProperties.empty(), propertySpecService);
    }

    EDPProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }

    public void addProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties);
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return "1".equals(properties.getTypedProperty(READCACHE_PROPERTY, READ_CACHE_DEFAULT_VALUE));
    }

    protected List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, PropertyTranslationKeys.DLMS_CLIENT_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS),
                this.stringSpec(DlmsProtocolProperties.CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION),
                this.stringSpec(DlmsProtocolProperties.PK_TIMEOUT, PropertyTranslationKeys.DLMS_TIMEOUT),
                this.stringSpec(DlmsProtocolProperties.PK_RETRIES, PropertyTranslationKeys.DLMS_RETRIES),
                this.stringSpec(READCACHE_PROPERTY, PropertyTranslationKeys.DLMS_READ_CACHE));
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, this.propertySpecService::stringSpec).finish();
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, this.propertySpecService::integerSpec).finish();
    }

    boolean isFirmwareClient() {
        int property = properties.getTypedProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, DlmsProtocolProperties.DEFAULT_CLIENT_MAC_ADDRESS);
        return property == FIRMWARE_CLIENT;
    }

    public int getClientMacAddress() {
        return properties.getTypedProperty(PROPNAME_CLIENT_MAC_ADDRESS, 1);
    }

    int getServerUpperMacAddress() {
        return properties.getTypedProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, 1);
    }

    int getServerLowerMacAddress() {
        return properties.getTypedProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, 16);
    }

}