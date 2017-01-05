package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, true),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.integer(INFORMATION_FIELD_SIZE, false),
                UPLPropertySpecFactory.integer(MAX_REC_PDU_SIZE, false),
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(ROUND_TRIP_CORRECTION, false),
                UPLPropertySpecFactory.integer(BULK_REQUEST, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false));
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