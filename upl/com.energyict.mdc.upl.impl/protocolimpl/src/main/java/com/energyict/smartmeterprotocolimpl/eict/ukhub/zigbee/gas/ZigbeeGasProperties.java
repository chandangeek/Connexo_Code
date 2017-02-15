package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.UkHubSecurityProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;


/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class ZigbeeGasProperties extends DlmsProtocolProperties {

    private static final String DEFAULT_ZIGBEE_GAS_CLIENT_MAC_ADDRESS = "64";
    public static final int FIRMWARE_CLIENT = 0x50;

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    /**
     * Default it starts at 30, but if more devices are supported then it can go from 30 to 45
     */
    private static final String DEFAULT_ZIGBEE_GAS_LOGICAL_DEVICE_ADDRESS = "30";

    private SecurityProvider securityProvider;

    private final PropertySpecService propertySpecService;

    public ZigbeeGasProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public ZigbeeGasProperties(Properties properties, PropertySpecService propertySpecService) {
        super(properties);
        this.propertySpecService = propertySpecService;
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(SECURITY_LEVEL, true, PropertyTranslationKeys.EICT_SECURITY_LEVEL, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ADDRESSING_MODE, false, PropertyTranslationKeys.EICT_ADDRESSING_MODE, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_CLIENT_MAC_ADDRESS, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS, propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_FORCED_DELAY, false, PropertyTranslationKeys.EICT_FORCED_DELAY, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_DELAY_AFTER_ERROR, false, PropertyTranslationKeys.EICT_DELAY_AFTER_ERROR, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CIPHERING_TYPE, false, PropertyTranslationKeys.EICT_CIPHERING_TYPE, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(NTA_SIMULATION_TOOL, false, PropertyTranslationKeys.EICT_NTA_SIMULATION_TOOL, propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY, propertySpecService::hexStringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY, propertySpecService::hexStringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(LOGBOOK_SELECTOR, false, PropertyTranslationKeys.EICT_LOGBOOK_SELECTOR, propertySpecService::integerSpec).finish());
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_ZIGBEE_GAS_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_ZIGBEE_GAS_LOGICAL_DEVICE_ADDRESS);
    }

    public void setSecurityProvider(final UkHubSecurityProvider ukHubSecurityProvider) {
        this.securityProvider = ukHubSecurityProvider;
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new UkHubSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

}