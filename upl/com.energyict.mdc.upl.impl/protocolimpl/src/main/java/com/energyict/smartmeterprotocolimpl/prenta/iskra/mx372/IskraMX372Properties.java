package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.Utils;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdw.core.DeviceType;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

    private DeviceType rtuType;
    private boolean bCSDCall = false;

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public void setProperties(Properties properties) throws PropertyValidationException {
        super.setProperties(properties);
        getProtocolProperties().put(DlmsProtocolProperties.SECURITY_LEVEL, getSecurityLevel());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(ADDRESSING_MODE, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.integer("RequestTimeZone", false),
                UPLPropertySpecFactory.string("FirmwareVersion", false),
                UPLPropertySpecFactory.integer("ExtendedLogging", false),
                UPLPropertySpecFactory.integer("Connection", false),
                UPLPropertySpecFactory.string("DeviceType", false),
                UPLPropertySpecFactory.integer("TestLogging", false),
                UPLPropertySpecFactory.string("FolderExtName", false),
                UPLPropertySpecFactory.integer("CsdCall", false),          // enable the csd call functionality
                UPLPropertySpecFactory.integer("IpPortNumber", false),     // portnumber for iskra meter (default 2048)
                UPLPropertySpecFactory.integer("PollTimeOut", false),      // timeout for polling the radius database
                UPLPropertySpecFactory.integer("CsdCallTimeOut", false),   // timeout between triggering the csd schedule and actually doing the schedule
                UPLPropertySpecFactory.string("CsdPollFrequency", false), // seconds between 2 request to the radius server
                UPLPropertySpecFactory.string("FixedIpAddress", false),   // use the filled in ip address for csd calls
                UPLPropertySpecFactory.string(NEW_LLS_SECRET, false));
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
    public DeviceType getRtuType() throws IOException {
        if (rtuType == null) {
            String type = getStringValue("DeviceType", "");

            if (Utils.isNull(type)) {
                // No automatic meter creation: no property DeviceType defined.
                rtuType = null;
            } else {
                rtuType = mw().getDeviceTypeFactory().find(type);
                if (rtuType == null) {
                    throw new IOException("Iskra Mx37x, No rtutype defined with name '" + type + "'");
                }
                if (rtuType.getConfigurations().get(0).getPrototypeDevice() == null) {
                    throw new IOException("Iskra Mx37x, rtutype '" + type + "' has no prototype rtu");
                }
            }
        }
        return rtuType;
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