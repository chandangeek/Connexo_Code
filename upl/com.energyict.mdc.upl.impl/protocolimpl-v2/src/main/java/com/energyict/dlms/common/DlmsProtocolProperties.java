package com.energyict.dlms.common;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.aso.ConformanceBlock;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * @author: sva
 * @since: 29/10/12 (10:10)
 */
public class DlmsProtocolProperties {

    public static final String TIMEOUT = "Timeout";
    public static final String TIMEZONE = "TimeZone";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";
    public static final String PROFILE_INTERVAL = com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName();
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
    public static final String DEVICE_ID = "DeviceId";
    public static final String USE_GBT = "Use-GBT";
    public static final String GBT_WINDOW_SIZE = "GBT-windowSize";
    public static final String DEVICE_SYSTEM_TITLE = "DeviceSystemTitle";
    public static final String VALIDATE_LOAD_PROFILE_CHANNELS = "ValidateLoadProfileChannels";
    public static final String PROPERTY_IGNORE_DST_STATUS_CODE = "IgnoreDstStatusCode";
    public static final String MASTER_KEY = "MasterKey";
    public static final String FRAME_COUNTER_LIMIT = "FrameCounterLimit";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String CONNECTION_MODE = "ConnectionMode";
    public static final String DEFAULT_CONNECTION_MODE = "WRAPPER";

    /** Property name of the property that indicates whether or not to bump the FC when invoking reply_to_hls. The f(StoC) uses FC, the action request carrying it FC + 1. */
    public static final String INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS = "IncrementFrameCounterForReplyToHLS";
    public static final String RTU_TYPE = "RTU_TYPE";
    public static final String FOLDER_EXT_NAME = "FolderExtName";

    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal(10000);
    public static final String DEFAULT_TIMEZONE = "GMT";
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(0);
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
    public static final BigDecimal DEFAULT_ROUND_TRIP_CORRECTION = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_ISKRA_WRAPPER = new BigDecimal(1);
    public static final BigDecimal DEFAULT_DEVICE_BUFFER_SIZE = new BigDecimal(-1);
    public static final boolean DEFAULT_FIX_MBUS_HEX_SHORT_ID = false;
    public static final String DEFAULT_DEVICE_ID = "";
    public static final boolean DEFAULT_ENABLE_GBT = false;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(-1);
    private static final int DEFAULT_DEST_WPDU_PORT = 1;
    /** The default for the {@value #INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS} is false. */
    private static final boolean DEFAULT_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS = false;

}