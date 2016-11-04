package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:47
 */
public class PrimeProperties extends DlmsProtocolProperties {

    /**
     * Name of the property containing the load profile OBIS code to fetch.
     */
    private static final String PROPNAME_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";
    private static final String PROPNAME_EVENT_LOGBOOK_OBIS_CODE = "EventLogBookObisCode";

    private static final String FW_UPGRADE_POLLING_DELAY = "FWUpgradePollingDelay";
    private static final String FW_UPGRADE_POLLING_RETRIES = "FWUpgradePollingRetries";
    private static final String FW_IMAGE_NAME = "FirmwareImageName";
    private static final String READ_SERIAL_NUMBER = "ReadSerialNumber";
    public static final String EVENTS_ONLY = "EventsOnly";

    private static final String DEFAULT_EVENTS_ONLY = "0";
    private static final int FIRMWARE_CLIENT_ADDRESS = 3;
    private static final String DOT = ".";

    PrimeProperties() {
        this(new Properties());
    }

    private PrimeProperties(Properties properties) {
        super(properties);
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false),
                new ObisCodePropertySpec(PROPNAME_LOAD_PROFILE_OBIS_CODE, false),
                new ObisCodePropertySpec(PROPNAME_EVENT_LOGBOOK_OBIS_CODE, false),
                UPLPropertySpecFactory.string(FW_IMAGE_NAME, false),
                UPLPropertySpecFactory.integer(FW_UPGRADE_POLLING_DELAY, false),
                UPLPropertySpecFactory.integer(FW_UPGRADE_POLLING_RETRIES, false),
                UPLPropertySpecFactory.integer(READ_SERIAL_NUMBER, false));
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, "1");
    }

    @Override
    public String getServerMacAddress() {
        final String oldMacAddress = getStringValue(SERVER_MAC_ADDRESS, "1:16");
        return oldMacAddress.replaceAll("x", getNodeAddress());
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "1:0");
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public final ObisCode getLoadProfileObiscode() {
        final String obisString = getStringValue(PROPNAME_LOAD_PROFILE_OBIS_CODE, "");
        try {
            return ObisCode.fromString(obisString);
        } catch (IllegalArgumentException e) {
            return PrimeProfile.BASIC_PROFILE;
        }
    }

    public ObisCode getEventLogBookObiscode() {
        String obiscodeString = getStringValue(PROPNAME_EVENT_LOGBOOK_OBIS_CODE, null);
        if (obiscodeString != null) {
            return ObisCode.fromString(obiscodeString);
        } else {
            return null;    //Read out all logbooks if no specific logbook obiscode has been configured
        }
    }

    @ProtocolProperty
    public final boolean isEventsOnly() {
        return getBooleanProperty(EVENTS_ONLY, DEFAULT_EVENTS_ONLY);
    }

    public final boolean isFirmwareClient() {
        return getClientMacAddress() == FIRMWARE_CLIENT_ADDRESS;
    }

    @ProtocolProperty
    public final int getPollingDelay() {
        return getIntProperty(FW_UPGRADE_POLLING_DELAY, "10000");
    }

    @ProtocolProperty
    public final int getPollingRetries() {
        return getIntProperty(FW_UPGRADE_POLLING_RETRIES, "20");
    }

    @ProtocolProperty
    public final String getFWImageName() {
        return getStringValue(FW_IMAGE_NAME, "NewImage");
    }

    @ProtocolProperty
    public final boolean isReadSerialNumber() {
        return getBooleanProperty(READ_SERIAL_NUMBER, "0");
    }

    public final String getFWImageNameWithoutExtension() {
        String fullFileName = getFWImageName();
        if (!fullFileName.contains(DOT)) {
            return fullFileName;
        }
        int extensionIndex = fullFileName.lastIndexOf(".");
        return fullFileName.substring(0, extensionIndex);
    }

}