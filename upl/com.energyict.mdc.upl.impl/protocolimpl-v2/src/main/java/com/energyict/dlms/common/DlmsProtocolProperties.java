package com.energyict.dlms.common;

import com.energyict.cpo.TypedProperties;
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
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.math.BigDecimal;
import java.util.Properties;

/**
 * @author: sva
 * @since: 29/10/12 (10:10)
 */
public abstract class DlmsProtocolProperties implements DlmsSessionProperties {

    public static final String TIMEOUT = "Timeout";
    public static final String TIMEZONE = "TimeZone";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";
    public static final String PROFILE_INTERVAL = MeterProtocol.PROFILEINTERVAL;
    public static final String CONNECTION = "Connection";
    public static final String SECURITY_LEVEL = "SecurityLevel";
    public static final String CLIENT_MAC_ADDRESS = "ClientMacAddress";
    public static final String SERVER_MAC_ADDRESS = "ServerMacAddress";
    public static final String SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    public static final String SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    public static final String READCACHE_PROPERTY = "ReadCache";
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
    public static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";
    public static final String DEVICE_ID = "DevideId";
    public static final String USE_GBT = "Use-GBT";
    public static final String GBT_WINDOW_SIZE = "GBT-windowSize";
    public static final String DEVICE_SYSTEM_TITLE = "DeviceSystemTitle";
    public static final String VALIDATE_LOAD_PROFILE_CHANNELS = "ValidateLoadProfileChannels";

    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal(10000);
    public static final String DEFAULT_TIMEZONE = "GMT";
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final BigDecimal DEFAULT_FORCED_DELAY = new BigDecimal(0);
    public static final BigDecimal DEFAULT_DELAY_AFTER_ERROR = new BigDecimal(100);
    public static final BigDecimal DEFAULT_PROFILE_INTERVAL = new BigDecimal(900);
    public static final int INVALID = -1;
    public static final int DEFAULT_LOWER_HDLC_ADDRESS = 0;
    public static final int DEFAULT_AUTHENTICATION_SECURITY_LEVEL = 0;
    public static final int DEFAULT_DATA_TRANSPORT_SECURITY_LEVEL = 0;
    public static final BigDecimal DEFAULT_CONNECTION = new BigDecimal(ConnectionMode.TCPIP.getMode());
    public static final String DEFAULT_SECURITY_LEVEL = DEFAULT_AUTHENTICATION_SECURITY_LEVEL + ":" + DEFAULT_DATA_TRANSPORT_SECURITY_LEVEL;
    public static final BigDecimal DEFAULT_CLIENT_MAC_ADDRESS = new BigDecimal(16);
    public static final String DEFAULT_SERVER_MAC_ADDRESS = "1";
    public static final BigDecimal DEFAULT_UPPER_SERVER_MAC_ADDRESS = BigDecimal.ONE;
    public static final BigDecimal DEFAULT_LOWER_SERVER_MAC_ADDRESS = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_ADDRESSING_MODE = new BigDecimal(2);
    public static final String DEFAULT_MANUFACTURER = "WKP";
    public static final BigDecimal DEFAULT_INFORMATION_FIELD_SIZE = new BigDecimal(-1);
    public static final Boolean DEFAULT_WAKE_UP = false;
    public static final BigDecimal DEFAULT_IP_PORT_NUMBER = new BigDecimal(4059);
    public static final BigDecimal DEFAULT_CIPHERING_TYPE = new BigDecimal(CipheringType.GLOBAL.getType());
    public static final Boolean DEFAULT_NTA_SIMULATION_TOOL = false;
    public static final Boolean DEFAULT_BULK_REQUEST = false;
    public static final BigDecimal DEFAULT_CONFORMANCE_BLOCK_VALUE_LN = new BigDecimal(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
    public static final BigDecimal DEFAULT_CONFORMANCE_BLOCK_VALUE_SN = new BigDecimal(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
    public static final String DEFAULT_SYSTEM_IDENTIFIER = "EICTCOMM";
    public static final BigDecimal DEFAULT_INVOKE_ID_AND_PRIORITY = new BigDecimal(66); // 0x42, 0b01000010 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]
    public static final Boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(4096);
    public static final BigDecimal DEFAULT_PROPOSED_DLMS_VERSION = new BigDecimal(6);
    public static final BigDecimal DEFAULT_PROPOSED_QOS = new BigDecimal(-1);
    public static final Boolean DEFAULT_REQUEST_TIMEZONE = false;
    public static final BigDecimal DEFAULT_ROUND_TRIP_CORRECTION = new BigDecimal(0);
    public static final BigDecimal DEFAULT_ISKRA_WRAPPER = new BigDecimal(1);
    public static final BigDecimal DEFAULT_DEVICE_BUFFER_SIZE = new BigDecimal(-1);
    public static final boolean DEFAULT_FIX_MBUS_HEX_SHORT_ID = false;
    public static final String DEFAULT_DEVICE_ID = "";
    public static final boolean DEFAULT_ENABLE_GBT = false;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(-1);
    private static final int DEFAULT_DEST_WPDU_PORT = 1;
    protected SecurityProvider securityProvider;
    private TypedProperties typedProperties;

    public DlmsProtocolProperties() {
        this(new TypedProperties());
    }

    public DlmsProtocolProperties(TypedProperties properties) {
        this.typedProperties = properties;
    }

    @ProtocolProperty
    public String getPassword() {
        return getStringValue(MeterProtocol.PASSWORD, "");
    }

    @ProtocolProperty
    public String getDeviceId() {
        return getStringValue(MeterProtocol.ADDRESS, "");
    }

    @ProtocolProperty
    public String getNodeAddress() {
        return getStringValue(MeterProtocol.NODEID, "");
    }

    @ProtocolProperty
    public String getSerialNumber() {
        return getStringValue(MeterProtocol.SERIALNUMBER, "");
    }

    @ProtocolProperty
    public int getTimeout() {
        return getIntProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    public String getTimeZone() {
        return getStringValue(TIMEZONE, DEFAULT_TIMEZONE);
    }

    @ProtocolProperty
    public int getRetries() {
        return getIntProperty(RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getForcedDelay() {
        return getIntProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @ProtocolProperty
    public int getDelayAfterError() {
        return getIntProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    @ProtocolProperty
    public int getProfileInterval() {
        return getIntProperty(PROFILE_INTERVAL, DEFAULT_PROFILE_INTERVAL);
    }

    @Override
    public Properties getProtocolProperties() {
        return getTypedProperties().toStringProperties();
    }

    @ProtocolProperty
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

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

    @ProtocolProperty
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
            securityProvider = new LocalSecurityProvider(getProtocolProperties());
        }
        return securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    /* -------------------- Common methods --------------------  */
    protected int getIntProperty(String propertyName, BigDecimal defaultValue) {
        return ((BigDecimal) getTypedProperties().getProperty(propertyName, defaultValue)).intValue();
    }

    protected long getLongProperty(String propertyName, BigDecimal defaultValue) {
        return ((BigDecimal) getTypedProperties().getProperty(propertyName, defaultValue)).longValue();
    }

    protected boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return (Boolean) getTypedProperties().getProperty(propertyName, defaultValue);
    }

    protected String getStringValue(String propertyName, String defaultValue) {
        return (String) getTypedProperties().getProperty(propertyName, defaultValue);
    }

    public void addProperties(TypedProperties properties) {
        this.typedProperties.setAllProperties(properties); // this will add the properties to the existing properties
    }

    public TypedProperties getTypedProperties() {
        return typedProperties;
    }
}
