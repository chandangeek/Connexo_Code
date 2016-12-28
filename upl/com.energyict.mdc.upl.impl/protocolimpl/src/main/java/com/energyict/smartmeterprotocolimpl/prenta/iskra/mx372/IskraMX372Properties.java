package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

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
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.google.common.base.Supplier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        getProtocolProperties().put(DlmsProtocolProperties.SECURITY_LEVEL, getSecurityLevel());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.spec(PK_RETRIES, this.propertySpecService::integerSpec),
                this.spec(PK_TIMEOUT, this.propertySpecService::integerSpec),
                this.spec(SECURITY_LEVEL, this.propertySpecService::integerSpec),
                this.spec(CLIENT_MAC_ADDRESS, this.propertySpecService::integerSpec),
                this.spec(SERVER_MAC_ADDRESS, this.propertySpecService::stringSpec),
                this.spec(ADDRESSING_MODE, this.propertySpecService::integerSpec),
                this.spec(CONNECTION, this.propertySpecService::integerSpec),
                this.spec("RequestTimeZone", this.propertySpecService::integerSpec),
                this.spec("FirmwareVersion", this.propertySpecService::stringSpec),
                this.spec("ExtendedLogging", this.propertySpecService::integerSpec),
                this.spec("Connection", this.propertySpecService::integerSpec),
                this.spec("DeviceType", this.propertySpecService::stringSpec),
                this.spec("TestLogging", this.propertySpecService::integerSpec),
                this.spec("FolderExtName", this.propertySpecService::stringSpec),
                this.spec("CsdCall", this.propertySpecService::integerSpec),          // enable the csd call functionality
                this.spec("IpPortNumber", this.propertySpecService::integerSpec),     // portnumber for iskra meter (default 2048)
                this.spec("PollTimeOut", this.propertySpecService::integerSpec),      // timeout for polling the radius database
                this.spec("CsdCallTimeOut", this.propertySpecService::integerSpec),   // timeout between triggering the csd schedule and actually doing the schedule
                this.spec("CsdPollFrequency", this.propertySpecService::stringSpec), // seconds between 2 request to the radius server
                this.spec("FixedIpAddress", this.propertySpecService::stringSpec),   // use the filled in ip address for csd calls
                this.spec(NEW_LLS_SECRET, this.propertySpecService::stringSpec));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
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