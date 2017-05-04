/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.mbus.core.DeviceType;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IskraMX372Properties extends DlmsProtocolProperties {

    private static final String DEFAULT_TIMEOUT = "5000";
    private static final String DEFAULT_RETRIES = "10";
    public static final String DEFAULT_SECURITY_LEVEL = "1";
    public static final int DEFAULT_LOWER_HDLC_ADDRESS = 1;
    public static final String DEFAULT_IP_PORT_NUMBER = "2048";
    public static final String DEFAULT_SERVER_MAC_ADDRESS = "1:1";
    public static final String DEFAULT_CLIENT_MAC_ADDRESS = "100";
    public static final String NEW_LLS_SECRET = "NewLLSSecret";
    public static final String DEFAULT_MANUFACTURER = "ISK";

    private DeviceType rtuType;
    private boolean bCSDCall = false;

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        getProtocolProperties().put(DlmsProtocolProperties.SECURITY_LEVEL, getSecurityLevel());
    }

    public List<String> getOptionalKeys() {
        List result = new ArrayList();
        result.add(TIMEOUT);
        result.add(RETRIES);
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("Connection");
        result.add("DeviceType");
        result.add("TestLogging");
        result.add("FolderExtName");
        result.add("CsdCall");          // enable the csd call functionality
        result.add("IpPortNumber");     // portnumber for iskra meter (default 2048)
        result.add(NEW_LLS_SECRET);

        result.add("PollTimeOut");      // timeout for polling the radius database
        result.add("CsdCallTimeOut");   // timeout between triggering the csd schedule and actually doing the schedule
        result.add("CsdPollFrequency"); // seconds between 2 request to the radius server
        result.add("FixedIpAddress");   // use the filled in ip address for csd calls

        return result;
    }

    public List<String> getRequiredKeys() {
        List result = new ArrayList();
        return result;
    }

    @ProtocolProperty
    @Override
    public int getTimeout() {
        return getIntProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    @Override
    public int getRetries() {
        return getIntProperty(RETRIES, DEFAULT_RETRIES);
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

            if (Strings.isNullOrEmpty(type)) {
                // No automatic meter creation: no property DeviceType defined.
                rtuType = null;
            } else {
                findDeviceType(type);
            }
        }
        return rtuType;
    }

    private void findDeviceType(String type) throws IOException {
        throw new UnsupportedOperationException("Don't support device type fetching from the protocol");
//        rtuType = mw().getDeviceTypeFactory().find(type);
//        if (rtuType == null) {
//            throw new IOException("Iskra Mx37x, No rtutype defined with name '" + type + "'");
//        }
//        if (rtuType.getConfigurations().get(0).getPrototypeDevice() == null) {
//            throw new IOException("Iskra Mx37x, rtutype '" + type + "' has no prototype rtu");
//        }
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