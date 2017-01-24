package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 7/10/13 - 15:43
 */
public class IskraMT880Properties extends DlmsProtocolProperties {

    public static final String DEFAULT_BULK_REQUEST = "1";  // By default, bulk request is enabled
    public static final String DEFAULT_CLIENT_MAC_ADDRESS = "1";    // Management client
    public static final String DEFAULT_SERVER_MAC_ADDRESS_TCPIP = "1:45"; // Address for TCP/IP connection
    public static final String DEFAULT_SERVER_MAC_ADDRESS_HDLC = "1:17"; // Address for HDLC connection
    public static final int DEFAULT_UPPER_HDLC_ADDRESS = 1;
    public static final int DEFAULT_LOWER_HDLC_ADDRESS_TCPIP = 45;
    public static final int DEFAULT_LOWER_HDLC_ADDRESS_HDLC = 17;
    public static final String DEFAULT_VALIDATE_INVOKE_ID = "1";

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.INFORMATION_FIELD_SIZE);
        optional.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);
        optional.add(DlmsProtocolProperties.BULK_REQUEST);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.ROUND_TRIP_CORRECTION);

        optional.add(NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
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
        return getStringValue(SERVER_MAC_ADDRESS, (getConnectionMode().getMode() == ConnectionMode.TCPIP.getMode())
                ? DEFAULT_SERVER_MAC_ADDRESS_TCPIP
                : DEFAULT_SERVER_MAC_ADDRESS_HDLC);
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
        return (getConnectionMode().getMode() == ConnectionMode.TCPIP.getMode())
                ? DEFAULT_LOWER_HDLC_ADDRESS_TCPIP
                : DEFAULT_LOWER_HDLC_ADDRESS_HDLC;
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
