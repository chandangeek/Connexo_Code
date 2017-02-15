package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 9:22
 */
class ZMDProperties extends DlmsProtocolProperties {

    private static final String DEFAULT_MAX_REC_PDU_SIZE = "-1";
    private static final String DEFAULT_ADDRESSING_MODE = "-1";
    private static final String DEFAULT_CLIENT_MAC_ADDRESS = "32";

    private SecurityProvider securityProvider;

    private final PropertySpecService propertySpecService;

    public ZMDProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public ZMDProperties(Properties properties, PropertySpecService propertySpecService) {
        super(properties);
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(SECURITY_LEVEL, false, PropertyTranslationKeys.EICT_SECURITY_LEVEL, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ADDRESSING_MODE, false, PropertyTranslationKeys.EICT_ADDRESSING_MODE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_CLIENT_MAC_ADDRESS, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_DELAY_AFTER_ERROR, false, PropertyTranslationKeys.EICT_DELAY_AFTER_ERROR, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CIPHERING_TYPE, false, PropertyTranslationKeys.EICT_CIPHERING_TYPE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INVOKE_ID_AND_PRIORITY, false, PropertyTranslationKeys.EICT_INVOKE_ID_AND_PRIORITY, this.propertySpecService::integerSpec).finish());
    }

    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    @Override
    public int getAddressingMode() {
        return getIntProperty(ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @Override
    public int getClientMacAddress() {
         return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    public int getRequestTimeZone() {
        return getIntProperty("RequestTimeZone","0");
    }

    @ProtocolProperty
    public int getEventIdIndex() {
        return getIntProperty("EventIdIndex","-1");
    }

    public DLMSReference getReference() {
        return DLMSReference.SN;
    }

    @Override
    public long getConformance() {
        if (isSNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, Long.toString(1573408L));
        } else if (isLNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, DEFAULT_CONFORMANCE_BLOCK_VALUE_LN);
        } else {
            return 0;
        }
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new ZMDSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

    @ProtocolProperty
    public String getManufacturer() {
        return getStringValue(MANUFACTURER, "LGZ");
    }

    @Override
    public byte[] getSystemIdentifier() {
        return "".getBytes();
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return ConnectionMode.HDLC;
    }

}