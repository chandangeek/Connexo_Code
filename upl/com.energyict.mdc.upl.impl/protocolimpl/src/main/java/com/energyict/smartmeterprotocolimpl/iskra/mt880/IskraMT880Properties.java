package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
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
 * @author sva
 * @since 7/10/13 - 15:43
 */
class IskraMT880Properties extends DlmsProtocolProperties {

    private static final String DEFAULT_BULK_REQUEST = "1";  // By default, bulk request is enabled
    private static final String DEFAULT_CLIENT_MAC_ADDRESS = "1";    // Management client
    private static final String DEFAULT_SERVER_MAC_ADDRESS_TCPIP = "1:45"; // Address for TCP/IP connection
    private static final String DEFAULT_SERVER_MAC_ADDRESS_HDLC = "1:17"; // Address for HDLC connection
    private static final int DEFAULT_UPPER_HDLC_ADDRESS = 1;
    private static final int DEFAULT_LOWER_HDLC_ADDRESS_TCPIP = 45;
    private static final int DEFAULT_LOWER_HDLC_ADDRESS_HDLC = 17;
    public static final String DEFAULT_VALIDATE_INVOKE_ID = "1";

    private final PropertySpecService propertySpecService;

    public IskraMT880Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public IskraMT880Properties(Properties properties, PropertySpecService propertySpecService) {
        super(properties);
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(SECURITY_LEVEL, true, PropertyTranslationKeys.EICT_SECURITY_LEVEL, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_CLIENT_MAC_ADDRESS, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY, this.propertySpecService::hexStringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY, this.propertySpecService::hexStringSpec).finish());
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return super.getConnectionMode();
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, defaultServerMacAddress());
    }

    private String defaultServerMacAddress() {
        if (getConnectionMode().getMode() == ConnectionMode.TCPIP.getMode()) {
            return DEFAULT_SERVER_MAC_ADDRESS_TCPIP;
        } else {
            return DEFAULT_SERVER_MAC_ADDRESS_HDLC;
        }
    }

    @Override
    public int getUpperHDLCAddress() {
        String[] macAddress = getServerMacAddress().split(":");
        if (macAddress.length >= 1) {
            try {
                return Integer.parseInt(macAddress[0]);
            } catch (NumberFormatException e) {
            }
        }
        return DEFAULT_UPPER_HDLC_ADDRESS;
    }

    @Override
    public int getLowerHDLCAddress() {
        String[] macAddress = getServerMacAddress().split(":");
        if (macAddress.length >= 2) {
            try {
                return Integer.parseInt(macAddress[1]);
            } catch (NumberFormatException e) {
            }
        }
        // If not specified, return the default one
        return defaultLowerHdlcAddress();
    }

    private int defaultLowerHdlcAddress() {
        if (getConnectionMode().getMode() == ConnectionMode.TCPIP.getMode()) {
            return DEFAULT_LOWER_HDLC_ADDRESS_TCPIP;
        } else {
            return DEFAULT_LOWER_HDLC_ADDRESS_HDLC;
        }
    }

    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @Override
    protected boolean validateInvokeId() {
        return getBooleanProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new NTASecurityProvider(this.getProtocolProperties());
        }
        return securityProvider;
    }

}