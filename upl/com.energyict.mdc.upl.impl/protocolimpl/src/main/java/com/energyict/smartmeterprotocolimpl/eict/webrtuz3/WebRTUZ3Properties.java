package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

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
class WebRTUZ3Properties extends DlmsProtocolProperties {

    private static final String MaxReceivePduSize = "4096";
    private static final String DefaultZ3BulkRequesSupport = "1";

    private final PropertySpecService propertySpecService;

    public WebRTUZ3Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public WebRTUZ3Properties(Properties properties, PropertySpecService propertySpecService) {
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
                UPLPropertySpecFactory.specBuilder(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY, this.propertySpecService::hexStringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY, this.propertySpecService::hexStringSpec).finish());
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DefaultZ3BulkRequesSupport);
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

}