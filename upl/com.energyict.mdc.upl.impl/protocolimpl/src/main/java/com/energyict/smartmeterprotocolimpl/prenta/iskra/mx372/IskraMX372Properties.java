package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/01/12
 * Time: 16:43
 */
public class IskraMX372Properties extends DlmsProtocolProperties {

    private static final String DEFAULT_TIMEOUT = "5000";
    private static final String DEFAULT_RETRIES = "10";
    private static final String DEFAULT_SECURITY_LEVEL = "1";
    private static final int DEFAULT_LOWER_HDLC_ADDRESS = 1;
    private static final String DEFAULT_IP_PORT_NUMBER = "2048";
    private static final String DEFAULT_SERVER_MAC_ADDRESS = "1:1";
    private static final String DEFAULT_CLIENT_MAC_ADDRESS = "100";
    private static final String NEW_LLS_SECRET = "NewLLSSecret";
    private static final String DEFAULT_MANUFACTURER = "ISK";

    private final PropertySpecService propertySpecService;
    private boolean bCSDCall = false;

    public IskraMX372Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        getProtocolProperties().put(DlmsProtocolProperties.SECURITY_LEVEL, getSecurityLevel());
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.spec(PK_RETRIES, PropertyTranslationKeys.PRENTA_RETRIES, this.propertySpecService::integerSpec),
                this.spec(PK_TIMEOUT, PropertyTranslationKeys.PRENTA_TIMEOUT, this.propertySpecService::integerSpec),
                this.spec(SECURITY_LEVEL, PropertyTranslationKeys.PRENTA_SECURITY_LEVEL, this.propertySpecService::integerSpec),
                this.spec(CLIENT_MAC_ADDRESS, PropertyTranslationKeys.PRENTA_CLIENT_MAC_ADDRESS, this.propertySpecService::integerSpec),
                this.spec(SERVER_MAC_ADDRESS, PropertyTranslationKeys.PRENTA_SERVER_MAC_ADDRESS, this.propertySpecService::stringSpec),
                this.spec(ADDRESSING_MODE, PropertyTranslationKeys.PRENTA_ADDRESSING_MODE, this.propertySpecService::integerSpec),
                this.spec(CONNECTION, PropertyTranslationKeys.PRENTA_CONNECTION, this.propertySpecService::integerSpec),
                this.spec("RequestTimeZone", PropertyTranslationKeys.PRENTA_REQUEST_TIME_ZONE, this.propertySpecService::integerSpec),
                this.spec("FirmwareVersion", PropertyTranslationKeys.PRENTA_FIRMWARE_VERION, this.propertySpecService::stringSpec),
                this.spec("ExtendedLogging", PropertyTranslationKeys.PRENTA_EXTENDED_LOGGING, this.propertySpecService::integerSpec),
                this.spec("Connection", PropertyTranslationKeys.PRENTA_CONNECTION, this.propertySpecService::integerSpec),
                this.spec("DeviceType", PropertyTranslationKeys.PRENTA_DEVICETYPE, this.propertySpecService::stringSpec),
                this.spec("TestLogging", PropertyTranslationKeys.PRENTA_TEST_LOGGING, this.propertySpecService::integerSpec),
                this.spec("FolderExtName", PropertyTranslationKeys.PRENTA_FOLDER_EXTERNAL_NAME, this.propertySpecService::stringSpec),
                this.spec("CsdCall", PropertyTranslationKeys.PRENTA_CSD_CALL, this.propertySpecService::integerSpec),          // enable the csd call functionality
                this.spec("IpPortNumber", PropertyTranslationKeys.PRENTA_IP_PORTNUMBER, this.propertySpecService::integerSpec),     // portnumber for iskra meter (default 2048)
                this.spec("PollTimeOut", PropertyTranslationKeys.PRENTA_POLL_TIMEOUT, this.propertySpecService::integerSpec),      // timeout for polling the radius database
                this.spec("CsdCallTimeOut", PropertyTranslationKeys.PRENTA_CSD_CALL_TIMEOUT, this.propertySpecService::integerSpec),   // timeout between triggering the csd schedule and actually doing the schedule
                this.spec("CsdPollFrequency", PropertyTranslationKeys.PRENTA_CSD_POLL_FREQUENCY, this.propertySpecService::stringSpec), // seconds between 2 request to the radius server
                this.spec("FixedIpAddress", PropertyTranslationKeys.PRENTA_FIXED_IP_ADDRES, this.propertySpecService::stringSpec),   // use the filled in ip address for csd calls
                this.spec(NEW_LLS_SECRET, PropertyTranslationKeys.PRENTA_NEW_LLS_SECRET, this.propertySpecService::stringSpec));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    @ProtocolProperty
    @Override
    public int getTimeout() {
        return getIntProperty(PK_TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    @Override
    public int getRetries() {
        return getIntProperty(PK_RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getRequestTimeZone() {
        return getIntProperty("RequestTimeZone", "0");
    }

    @ProtocolProperty
    public String getFirmwareVersion() {
        return getStringValue("FirmwareVersion", "ANY");
    }

    @ProtocolProperty
    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, DEFAULT_SECURITY_LEVEL);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new LocalSecurityProvider(getProtocolProperties());
        }
        return securityProvider;
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_SERVER_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getUpperHDLCAddress() {
        String[] macAddress = getServerMacAddress().split(":");
        if (macAddress.length >= 1) {
            try {
                return Integer.parseInt(macAddress[0]);
            } catch (NumberFormatException e) {
            }
        }
        return INVALID;
    }

    @ProtocolProperty
    @Override
    public int getLowerHDLCAddress() {
        String[] macAddress = getServerMacAddress().split(":");
        if (macAddress.length >= 2) {
            try {
                return Integer.parseInt(macAddress[1]);
            } catch (NumberFormatException e) {
            }
        }
        return DEFAULT_LOWER_HDLC_ADDRESS;
    }

    @ProtocolProperty
    public int getExtendedLogging() {
        return getIntProperty("ExtendedLogging", "0");
    }

    @ProtocolProperty
    public String getDeviceTypeName() throws IOException {
        return getStringValue("DeviceType", "");
    }

    @ProtocolProperty
    public int getTestLogging() {
        return getIntProperty("TestLogging", "0");
    }

    @ProtocolProperty
    public String getFolderExtName() {
        return getStringValue("FolderExtName", "");
    }

    @ProtocolProperty
    public int getCsdCall() {
        return getIntProperty("CsdCall", "0");
    }

    /**
     * Look if there is a portnumber given with the property IpPortNumber, else use the default 2048
     *
     * @return
     */
    @ProtocolProperty
    @Override
    public int getIpPortNumber() {
        int port = getIntProperty(IP_PORT_NUMBER, DEFAULT_IP_PORT_NUMBER);
        if (port != 0) {
            return port;
        } else {
            return 2048;    // default port number
        }
    }

    @ProtocolProperty
    public String getNewLLSSecret() {
        String value = getStringValue(NEW_LLS_SECRET, "");
        return (value == "") ? null : value;
    }

    @Override
    public byte[] getSystemIdentifier() {
        return new byte[0];
    }

    @Override
    public int getIskraWrapper() {
        return 1;
    }

    @Override
    public String getManufacturer() {
        return getStringValue(MANUFACTURER, DEFAULT_MANUFACTURER);
    }

    public boolean madeCSDCall() {
        return bCSDCall;
    }

    public void setbCSDCall(boolean b) {
        bCSDCall = b;
    }

}