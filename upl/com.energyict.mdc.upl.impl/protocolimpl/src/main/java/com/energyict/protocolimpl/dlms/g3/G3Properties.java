package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.DLMSReference;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 10:34
 */
public class G3Properties extends DlmsProtocolProperties {

    public static final String PROFILE_TYPE = "ProfileType";
    public static final String DEFAULT_G3_MAX_REC_PDU_SIZE = "512";
    public static final String HLS_SECRET = "HlsSecret";
    public static final String AARQ_RETRIES = "AARQRetries";
    public static final String DEFAULT_AARQ_RETRIES = "2";
    public static final String AARQ_TIMEOUT = "AARQTimeout";
    public static final String PSK = "PSK";
    public static final String DEFAULT_AARQ_TIMEOUT = "0";      //Means: not used
    public static final String DEFAULT_VALIDATE_INVOKE_ID = "1";
    public static final String PROP_LASTSEENDATE = "LastSeenDate";

    private G3SecurityProvider g3SecurityProvider;

    public G3Properties() {
        this(new Properties());
    }

    public G3Properties(Properties properties) {
        super(properties);
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    @ProtocolProperty
    public String getPassword() {
        String hlsSecret = getStringValue(HLS_SECRET, "");
        if (hlsSecret == null || hlsSecret.length() == 0 || "".equals(hlsSecret)) {
            return getStringValue(MeterProtocol.PASSWORD, "");     //Is limited to 20 chars in EiServer!
        } else {
            return hlsSecret;
        }
    }

    public int getAARQRetries() {
        return getIntProperty(AARQ_RETRIES, DEFAULT_AARQ_RETRIES);
    }

    public int getAARQTimeout() {
        return getIntProperty(AARQ_TIMEOUT, DEFAULT_AARQ_TIMEOUT);
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(CLIENT_MAC_ADDRESS);
        optional.add(HLS_SECRET);
        optional.add(SERVER_MAC_ADDRESS);
        optional.add(SECURITY_LEVEL);
        optional.add(Dsmr40Properties.DSMR_40_HEX_PASSWORD);
        optional.add(CONNECTION);
        optional.add(NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        optional.add(NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(PROFILE_TYPE);
        optional.add(PROP_LASTSEENDATE);
        optional.add(AARQ_RETRIES);
        optional.add(VALIDATE_INVOKE_ID);
        optional.add(AARQ_TIMEOUT);
        optional.add(PSK);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();

        return required;
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, "2");
    }

    @Override
    public String getServerMacAddress() {
        return getNodeAddress();
        //final String oldMacAddress = getStringValue(SERVER_MAC_ADDRESS, "1:16");
        //return oldMacAddress.replaceAll("x", getNodeAddress());
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "1:0");
    }

    @Override
    public G3SecurityProvider getSecurityProvider() {
        if (g3SecurityProvider == null) {
            g3SecurityProvider = new G3SecurityProvider(this.getProtocolProperties());
        }
        return g3SecurityProvider;
    }

    @ProtocolProperty
    public G3ProfileType getProfileType() {
        final int profileTypeId = getIntProperty(PROFILE_TYPE, "1");
        return G3ProfileType.fromProfileId(profileTypeId);
    }

    @Override
    public byte[] getSystemIdentifier() {
        if (getSerialNumber() == null) {
            return new byte[6];
        }

        final String serial = ProtocolTools.addPaddingAndClip(getSerialNumber(), '0', 12, true);
        byte[] systemTitle = ProtocolTools.getBytesFromHexString(serial, "");

        return systemTitle;
    }

    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_G3_MAX_REC_PDU_SIZE);
    }

    @Override
    protected boolean validateInvokeId() {
        return getBooleanProperty(super.VALIDATE_INVOKE_ID, this.DEFAULT_VALIDATE_INVOKE_ID);
    }
}
