package com.energyict.protocolimpl.dlms.g3;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;
import static com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties.DSMR_40_HEX_PASSWORD;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 10:34
 */
public class G3Properties extends DlmsProtocolProperties {

    private static final String PROFILE_TYPE = "ProfileType";
    private static final String DEFAULT_G3_MAX_REC_PDU_SIZE = "512";
    private static final String HLS_SECRET = "HlsSecret";
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

    @ProtocolProperty
    public String getPassword() {
        String hlsSecret = getStringValue(HLS_SECRET, "");
        if (hlsSecret == null || hlsSecret.isEmpty() || "".equals(hlsSecret)) {
            return getStringValue(PK_PASSWORD, "");     //Is limited to 20 chars in EiServer!
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.string(HLS_SECRET, false),
                UPLPropertySpecFactory.string(DSMR_40_HEX_PASSWORD, false),
                UPLPropertySpecFactory.integer(PROFILE_TYPE, false),
                UPLPropertySpecFactory.string(PROP_LASTSEENDATE, false),
                UPLPropertySpecFactory.integer(AARQ_RETRIES, false),
                UPLPropertySpecFactory.integer(AARQ_TIMEOUT, false),
                UPLPropertySpecFactory.integer(VALIDATE_INVOKE_ID, false),
                UPLPropertySpecFactory.string(PSK, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false));
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, "2");
    }

    @Override
    public String getServerMacAddress() {
        return getNodeAddress();
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
        return ProtocolTools.getBytesFromHexString(serial, "");
    }

    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_G3_MAX_REC_PDU_SIZE);
    }

    @Override
    protected boolean validateInvokeId() {
        return getBooleanProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

}