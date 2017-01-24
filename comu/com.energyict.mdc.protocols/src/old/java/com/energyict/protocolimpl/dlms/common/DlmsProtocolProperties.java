package com.energyict.protocolimpl.dlms.common;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.DlmsSessionProperties;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 11-feb-2011
 * Time: 13:27:58
 */
public abstract class DlmsProtocolProperties extends AbstractProtocolProperties implements DlmsSessionProperties {

    public static final int INVALID = -1;
    public static final int DEFAULT_LOWER_HDLC_ADDRESS = 0;
    public static final int DEFAULT_AUTHENTICATION_SECURITY_LEVEL = 0;
    public static final int DEFAULT_DATA_TRANSPORT_SECURITY_LEVEL = 0;
    private static final int DEFAULT_DEST_WPDU_PORT = 1;

    public static final String CONNECTION = "Connection";
    public static final String SECURITY_LEVEL = "SecurityLevel";
    public static final String CLIENT_MAC_ADDRESS = "ClientMacAddress";
    public static final String SERVER_MAC_ADDRESS = "ServerMacAddress";
    public static final String ADDRESSING_MODE = "AddressingMode";
    public static final String MANUFACTURER = "Manufacturer";
    public static final String INFORMATION_FIELD_SIZE = "InformationFieldSize";
    public static final String WAKE_UP = "WakeUp";
    public static final String IP_PORT_NUMBER = "IpPortNumber";
    public static final String CIPHERING_TYPE = "CipheringType";
    public static final String NTA_SIMULATION_TOOL = "NTASimulationTool";
    public static final String BULK_REQUEST = "BulkRequest";
    public static final String CONFORMANCE_BLOCK_VALUE = "ConformanceBlockValue";
    public static final String SYSTEM_IDENTIFIER = "SystemIdentifier";
    public static final String INVOKE_ID_AND_PRIORITY = "InvokeIdAndPriority";
    public static final String VALIDATE_INVOKE_ID = "ValidateInvokeId";
    public static final String MAX_REC_PDU_SIZE = "MaxRecPDUSize";
    public static final String PROPOSED_DLMS_VERSION = "ProposedDlmsVersion";
    public static final String PROPOSED_QOS = "ProposedQOS";
    public static final String REQUEST_TIMEZONE = "RequestTimeZone";
    public static final String ROUND_TRIP_CORRECTION = "RoundTripCorrection";
    public static final String ISKRA_WRAPPER = "IskraWrapper";
    public static final String DEVICE_BUFFER_SIZE = "DeviceBufferSize";

    public static final String DEFAULT_CONNECTION = ConnectionMode.TCPIP.getModeAsString();
    public static final String DEFAULT_SECURITY_LEVEL = DEFAULT_AUTHENTICATION_SECURITY_LEVEL + ":" + DEFAULT_DATA_TRANSPORT_SECURITY_LEVEL;
    public static final String DEFAULT_CLIENT_MAC_ADDRESS = "16";
    public static final String DEFAULT_SERVER_MAC_ADDRESS = "1";
    public static final String DEFAULT_ADDRESSING_MODE = "2";
    public static final String DEFAULT_MANUFACTURER = "WKP";
    public static final String DEFAULT_INFORMATION_FIELD_SIZE = "-1";
    public static final String DEFAULT_WAKE_UP = "0";
    public static final String DEFAULT_IP_PORT_NUMBER = "4059";
    public static final String DEFAULT_CIPHERING_TYPE = CipheringType.GLOBAL.getTypeString();
    public static final String DEFAULT_NTA_SIMULATION_TOOL = "0";
    public static final String DEFAULT_BULK_REQUEST = "0";
    public static final String DEFAULT_CONFORMANCE_BLOCK_VALUE_LN = "" + ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK;
    public static final String DEFAULT_CONFORMANCE_BLOCK_VALUE_SN = "" + ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK;
    public static final String DEFAULT_SYSTEM_IDENTIFIER = "EICTCOMM";
    public static final String DEFAULT_INVOKE_ID_AND_PRIORITY = "66"; // 0x41, 0b01000001 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]
    public static final String DEFAULT_VALIDATE_INVOKE_ID = "0";
    public static final String DEFAULT_MAX_REC_PDU_SIZE = "4096";
    public static final String DEFAULT_PROPOSED_DLMS_VERSION = "6";
    public static final String DEFAULT_PROPOSED_QOS = "-1";
    public static final String DEFAULT_REQUEST_TIMEZONE = "0";
    public static final String DEFAULT_ROUND_TRIP_CORRECTION = "0";
    public static final String DEFAULT_ISKRA_WRAPPER = "1";
    public static final String DEFAULT_DEVICE_BUFFER_SIZE = "-1";

    protected SecurityProvider securityProvider;

    public DlmsProtocolProperties() {
        super(new Properties());
    }

    public DlmsProtocolProperties(Properties properties) {
        super(properties);
    }

    @ProtocolProperty
    public abstract DLMSReference getReference();

    @ProtocolProperty
    public ConnectionMode getConnectionMode() {
        return ConnectionMode.fromValue(getIntProperty(CONNECTION, DEFAULT_CONNECTION));
    }

    @ProtocolProperty
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, DEFAULT_SECURITY_LEVEL);
    }

    @ProtocolProperty
    public int getAuthenticationSecurityLevel() {
        String[] secLevel = getSecurityLevel().split(":");
        if (secLevel.length >= 1) {
            try {
                return Integer.parseInt(secLevel[0]);
            } catch (NumberFormatException e) {
            }
        }
        return DEFAULT_AUTHENTICATION_SECURITY_LEVEL;
    }

    @ProtocolProperty
    public int getDataTransportSecurityLevel() {
        String[] secLevel = getSecurityLevel().split(":");
        if (secLevel.length >= 2) {
            try {
                return Integer.parseInt(secLevel[1]);
            } catch (NumberFormatException e) {
            }
        }
        return DEFAULT_DATA_TRANSPORT_SECURITY_LEVEL;
    }

    @ProtocolProperty
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_SERVER_MAC_ADDRESS);
    }

    @ProtocolProperty
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
    public int getDestinationWPortNumber() {
        String[] macAddress = getServerMacAddress().split(":");
        if (macAddress.length >= 1) {
            try {
                return Integer.parseInt(macAddress[0]);
            } catch (NumberFormatException e) {
            }
        }
        return DEFAULT_DEST_WPDU_PORT;
    }

    @ProtocolProperty
    public int getAddressingMode() {
        return getIntProperty(ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @ProtocolProperty
    public String getManufacturer() {
        return getStringValue(MANUFACTURER, DEFAULT_MANUFACTURER);
    }

    @ProtocolProperty
    public int getInformationFieldSize() {
        return getIntProperty(INFORMATION_FIELD_SIZE, DEFAULT_INFORMATION_FIELD_SIZE);
    }

    @ProtocolProperty
    public boolean isWakeUp() {
        return getBooleanProperty(WAKE_UP, DEFAULT_WAKE_UP);
    }

    @ProtocolProperty
    public int getIpPortNumber() {
        return getIntProperty(IP_PORT_NUMBER, DEFAULT_IP_PORT_NUMBER);
    }

    @ProtocolProperty
    public CipheringType getCipheringType() {
        return CipheringType.fromValue(getIntProperty(CIPHERING_TYPE, DEFAULT_CIPHERING_TYPE));
    }

    @ProtocolProperty
    public boolean isNtaSimulationTool() {
        return getBooleanProperty(NTA_SIMULATION_TOOL, DEFAULT_NTA_SIMULATION_TOOL);
    }

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @ProtocolProperty
    public long getConformance() {
        if (isSNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, DEFAULT_CONFORMANCE_BLOCK_VALUE_SN);
        } else if (isLNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, DEFAULT_CONFORMANCE_BLOCK_VALUE_LN);
        } else {
            return 0;
        }
    }

    @ProtocolProperty
    public ConformanceBlock getConformanceBlock() {
        return new ConformanceBlock(getConformance());
    }

    @ProtocolProperty
    public boolean isSNReference() {
        return getReference() == DLMSReference.SN;
    }

    @ProtocolProperty
    public boolean isLNReference() {
        return getReference() == DLMSReference.LN;
    }

    @ProtocolProperty
    public byte[] getSystemIdentifier() {
        return getStringValue(SYSTEM_IDENTIFIER, DEFAULT_SYSTEM_IDENTIFIER).getBytes();
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        byte invokeIdAndPriority = (byte) (getIntProperty(INVOKE_ID_AND_PRIORITY, DEFAULT_INVOKE_ID_AND_PRIORITY) & 0x0FF);
        if (validateInvokeId()) {
            return new IncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        } else {
            return new NonIncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        }
    }

    protected boolean validateInvokeId() {
        return getBooleanProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    @ProtocolProperty
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    @ProtocolProperty
    public int getProposedDLMSVersion() {
        return getIntProperty(PROPOSED_DLMS_VERSION, DEFAULT_PROPOSED_DLMS_VERSION);
    }

    @ProtocolProperty
    public int getProposedQOS() {
        return getIntProperty(PROPOSED_QOS, DEFAULT_PROPOSED_QOS);
    }

    @ProtocolProperty
    public boolean isRequestTimeZone() {
        return getBooleanProperty(REQUEST_TIMEZONE, DEFAULT_REQUEST_TIMEZONE);
    }

    @ProtocolProperty
    public int getRoundTripCorrection() {
        return getIntProperty(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    @ProtocolProperty
    public int getIskraWrapper() {
        return getIntProperty(ISKRA_WRAPPER, DEFAULT_ISKRA_WRAPPER);
    }

    @ProtocolProperty
    public int getDeviceBufferSize() {
        return getIntProperty(DEVICE_BUFFER_SIZE, DEFAULT_DEVICE_BUFFER_SIZE);
    }

    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new LocalSecurityProvider(this);
        }
        return securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

}
