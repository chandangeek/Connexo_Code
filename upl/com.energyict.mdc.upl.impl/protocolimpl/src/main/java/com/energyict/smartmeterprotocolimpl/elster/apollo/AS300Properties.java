package com.energyict.smartmeterprotocolimpl.elster.apollo;

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
 * Date: 7-feb-2011
 * Time: 14:16:15
 */
public class AS300Properties extends DlmsProtocolProperties {

    private static final String DEFAULT_AS300_CLIENT_MAC_ADDRESS = "64";
    private static final String DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS = "45";

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    private static final int FIRMWARE_CLIENT = 0x50;
    private static final String MaxReceivePduSize = "128";

    private final PropertySpecService propertySpecService;

    public AS300Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public AS300Properties(Properties properties, PropertySpecService propertySpecService) {
        super(properties);
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(SECURITY_LEVEL, true, PropertyTranslationKeys.EICT_SECURITY_LEVEL, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ADDRESSING_MODE, false, PropertyTranslationKeys.EICT_ADDRESSING_MODE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_CLIENT_MAC_ADDRESS, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_FORCED_DELAY, false, PropertyTranslationKeys.EICT_FORCED_DELAY, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_DELAY_AFTER_ERROR, false, PropertyTranslationKeys.EICT_DELAY_AFTER_ERROR, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SYSTEM_IDENTIFIER, false, PropertyTranslationKeys.EICT_SYSTEM_IDENTIFIER, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY, this.propertySpecService::hexStringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY, this.propertySpecService::hexStringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(LOGBOOK_SELECTOR, false, PropertyTranslationKeys.EICT_LOGBOOK_SELECTOR, this.propertySpecService::integerSpec).finish());
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_AS300_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "1");
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if(super.securityProvider == null){
            setSecurityProvider(new UkHubSecurityProvider(getProtocolProperties()));
        }
        return super.securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider){
        super.securityProvider = securityProvider;
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
    }

    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

}