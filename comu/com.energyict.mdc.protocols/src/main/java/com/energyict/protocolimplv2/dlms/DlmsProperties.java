/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.common.BasicDynamicPropertySupport;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class DlmsProperties extends BasicDynamicPropertySupport implements DlmsSessionProperties {

    public static final String SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    public static final String SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    public static final String DEVICE_ID = "DeviceId";
    public static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";
    public static final String USE_GBT = "Use-GBT";
    public static final String GBT_WINDOW_SIZE = "GBT-windowSize";
    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";


    public static final BigDecimal DEFAULT_UPPER_SERVER_MAC_ADDRESS = BigDecimal.ONE;
    public static final BigDecimal DEFAULT_LOWER_SERVER_MAC_ADDRESS = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_ADDRESSING_MODE = new BigDecimal(2);
    public static final String DEFAULT_MANUFACTURER = "WKP";
    public static final BigDecimal DEFAULT_INFORMATION_FIELD_SIZE = new BigDecimal(-1);
    public static final Boolean DEFAULT_WAKE_UP = false;
    public static final String DEFAULT_DEVICE_ID = "";
    public static final BigDecimal DEFAULT_CIPHERING_TYPE = new BigDecimal(CipheringType.GLOBAL.getType());
    public static final Boolean DEFAULT_NTA_SIMULATION_TOOL = false;
    public static final Boolean DEFAULT_BULK_REQUEST = true;
    public static final BigDecimal DEFAULT_CONFORMANCE_BLOCK_VALUE_LN = new BigDecimal(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
    public static final BigDecimal DEFAULT_CONFORMANCE_BLOCK_VALUE_SN = new BigDecimal(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
    public static final String DEFAULT_SYSTEM_IDENTIFIER = "EICTCOMM";
    public static final BigDecimal DEFAULT_INVOKE_ID_AND_PRIORITY = new BigDecimal(66); // 0x42, 0b01000010 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]
    public static final Boolean DEFAULT_VALIDATE_INVOKE_ID = false;
    public static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(4096);
    public static final BigDecimal DEFAULT_PROPOSED_DLMS_VERSION = new BigDecimal(6);
    public static final BigDecimal DEFAULT_PROPOSED_QOS = new BigDecimal(-1);
    public static final Boolean DEFAULT_REQUEST_TIMEZONE = false;
    public static final BigDecimal DEFAULT_ROUND_TRIP_CORRECTION = BigDecimal.ZERO;
    public static final Boolean DEFAULT_FIX_MBUS_HEX_SHORT_ID = false;
    public static final boolean DEFAULT_ENABLE_GBT = false;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(-1);

    public enum TranslationKeys implements TranslationKey {
        SERVER_UPPER_MAC_ADDRESS_TK(SERVER_UPPER_MAC_ADDRESS, "Server upper mac address"),
        SERVER_LOWER_MAC_ADDRESS_TK(SERVER_LOWER_MAC_ADDRESS, "Server lower mac address"),
        ADDRESSING_MODE_TK(DlmsProtocolProperties.ADDRESSING_MODE, "Addressing mode"),
        MANUFACTURER_TK(DlmsProtocolProperties.MANUFACTURER, "Manufacturer"),
        INFORMATION_FIELD_SIZE_TK(DlmsProtocolProperties.INFORMATION_FIELD_SIZE, "Information field size"),
        WAKE_UP_TK(DlmsProtocolProperties.WAKE_UP, "WakeUp"),
        DEVICE_ID_TK(DEVICE_ID, "DeviceId"),
        CIPHERING_TYPE_TK(DlmsProtocolProperties.CIPHERING_TYPE, "Ciphering type"),
        NTA_SIMULATION_TOOL_TK(DlmsProtocolProperties.NTA_SIMULATION_TOOL, "NTA simulation tool"),
        BULK_REQUEST_TK(DlmsProtocolProperties.BULK_REQUEST, "Bulk request"),
        CONFORMANCE_BLOCK_VALUE_TK(DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE, "Conformance block value"),
        VALIDATE_INVOKE_ID_TK(DlmsProtocolProperties.VALIDATE_INVOKE_ID, "Validate invokeId"),
        MAX_REC_PDU_SIZE_TK(DlmsProtocolProperties.MAX_REC_PDU_SIZE, "Max rec PDU size"),
        REQUEST_TIMEZONE_TK(DlmsProtocolProperties.REQUEST_TIMEZONE, "Request timezone"),
        ROUND_TRIP_CORRECTION_TK(DlmsProtocolProperties.ROUND_TRIP_CORRECTION, "Roundtrip correction"),
        FIX_MBUS_HEX_SHORT_ID_TK(FIX_MBUS_HEX_SHORT_ID, "Fix Mbus hex shortId"),
        NODEID_TK(MeterProtocol.NODEID, "Node id"),
        CALLING_AP_TITLE_TK(IDIS.CALLING_AP_TITLE, "Calling AP title"),
        G3_MAC_ADDRESS_PROP_NAME_TK("MAC_address", "MAC address"),
        G3_SHORT_ADDRESS_PROP_NAME_TK("Short_MAC_address", "Short MAC address"),
        G3_LOGICAL_DEVICE_ID_PROP_NAME_TK("Logical_device_id", "Logical device id"),
        AARQ_TIMEOUT_PROPERTY("AARQTimeout", "AARQ Timeout"),
        AARQ_RETRIES_PROPERTY("AARQRetries", "AARQ Retries"),
        READCACHE_PROPERTY("ReadCache", "Read cache"),
        CumulativeCaptureTimeChannel("CumulativeCaptureTimeChannel", "Cumulative capture time channel"),
        PSK_PROPERTY("PSK", "PSK"),
        CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME("CheckNumberOfBlocksDuringFirmwareResume", "Check number of blocks during firmware resume"),
        USE_EQUIPMENT_IDENTIFIER_AS_SERIAL("UseEquipmentIdentifierAsSerialNumber", "Use equipment identifier as serial number"),
        IGNORE_DST_STATUS_CODE("IgnoreDstStatusCode", "Ignore DST status code");

        private final String propertySpecName;
        private final String defaultFormat;

        TranslationKeys(String propertySpecName, String defaultFormat) {
            this.propertySpecName = propertySpecName;
            this.defaultFormat = defaultFormat;
        }

        public String getPropertySpecName() {
            return propertySpecName;
        }

        @Override
        public String getKey() {
            return "DlmsProperties." + this.propertySpecName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    protected SecurityProvider securityProvider;
    private String serialNumber = "";

    public DlmsProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.properties = TypedProperties.empty();
    }

    public void addProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties);
    }

    public TypedProperties getProperties() {
        return properties;
    }

    /**
     * The device timezone
     */
    @Override
    public TimeZone getTimeZone() {
        TimeZone timeZone = properties.<TimeZone>getTypedProperty(TIMEZONE);
        if (timeZone == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return timeZone;
        }
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet securityPropertySet) {
        this.securityPropertySet = securityPropertySet;
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet;
    }

    @Override
    public int getAuthenticationSecurityLevel() {
        return securityPropertySet.getAuthenticationDeviceAccessLevel();
    }

    @Override
    public int getDataTransportSecurityLevel() {
        return securityPropertySet.getEncryptionDeviceAccessLevel();
    }

    @Override
    public int getClientMacAddress() {
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE);
    }

    @Override
    public int getServerUpperMacAddress() {
        return parseBigDecimalProperty(SERVER_UPPER_MAC_ADDRESS, DEFAULT_UPPER_SERVER_MAC_ADDRESS);
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, DEFAULT_LOWER_SERVER_MAC_ADDRESS);
    }

    @Override
    public int getAddressingMode() {
        return parseBigDecimalProperty(DlmsProtocolProperties.ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @Override
    public String getManufacturer() {
        return properties.getTypedProperty(DlmsProtocolProperties.MANUFACTURER, DEFAULT_MANUFACTURER);
    }

    @Override
    public int getInformationFieldSize() {
        return parseBigDecimalProperty(DlmsProtocolProperties.INFORMATION_FIELD_SIZE, DEFAULT_INFORMATION_FIELD_SIZE);
    }

    @Override
    public boolean isWakeUp() {
        return properties.getTypedProperty(DlmsProtocolProperties.WAKE_UP, DEFAULT_WAKE_UP);
    }

    @Override
    public String getDeviceId() {
        return properties.getTypedProperty(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    @Override
    public CipheringType getCipheringType() {
        return CipheringType.fromValue(parseBigDecimalProperty(DlmsProtocolProperties.CIPHERING_TYPE, DEFAULT_CIPHERING_TYPE));
    }

    @Override
    public boolean isNtaSimulationTool() {
        return properties.getTypedProperty(DlmsProtocolProperties.NTA_SIMULATION_TOOL, DEFAULT_NTA_SIMULATION_TOOL);
    }

    @Override
    public boolean isBulkRequest() {
        return getProperties().getTypedProperty(DlmsProtocolProperties.BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        BigDecimal defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_LN;
        if (getReference().equals(DLMSReference.SN)) {
            defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_SN;
        }
        return new ConformanceBlock(parseBigDecimalProperty(DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE, defaultValue));
    }

    @Override
    public byte[] getSystemIdentifier() {
        return DEFAULT_SYSTEM_IDENTIFIER.getBytes();
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        byte invokeIdAndPriority = (byte) (DEFAULT_INVOKE_ID_AND_PRIORITY.intValue());
        if (properties.<Boolean>getTypedProperty(DlmsProtocolProperties.VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID)) {
            return new IncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        } else {
            return new NonIncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        }
    }

    @Override
    public int getMaxRecPDUSize() {
        return parseBigDecimalProperty(DlmsProtocolProperties.MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    @Override
    public int getProposedDLMSVersion() {
        return DEFAULT_PROPOSED_DLMS_VERSION.intValue();
    }

    @Override
    public int getProposedQOS() {
        return DEFAULT_PROPOSED_QOS.intValue();
    }

    @Override
    public boolean isRequestTimeZone() {
        return properties.<Boolean>getTypedProperty(DlmsProtocolProperties.REQUEST_TIMEZONE, DEFAULT_REQUEST_TIMEZONE);
    }

    @Override
    public int getRoundTripCorrection() {
        return parseBigDecimalProperty(DlmsProtocolProperties.ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    /**
     * Property that swaps the client and server address in the TCP header.
     * Other protocols can override this value if necessary.
     */
    @Override
    public boolean isSwitchAddresses() {
        return true;
    }

    /**
     * Sets the type of SNRM frame in the HDLC connection.
     * Other protocols can override this value if necessary.
     */
    @Override
    public int getSNRMType() {
        return 0;
    }

    /**
     * Default 5 = 9600 baud
     * Other protocols can override this value if necessary.
     */
    @Override
    public int getHHUSignonBaudRateCode() {
        return 5;
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        return properties.<Boolean>getTypedProperty(USE_GBT, DEFAULT_ENABLE_GBT);
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return properties.getTypedProperty(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE).intValue();
    }

    @Override
    public TimeDuration getPollingDelay() {
        return new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean timeoutMeansBrokenConnection() {
        return true;
    }

    @Override
    public boolean incrementFrameCounterForRetries() {
        return true;    // Protocols who don't want the frame counter to be increased for retries, can override this method
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null && securityPropertySet != null) {
            securityProvider = new NTASecurityProvider(properties, securityPropertySet.getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    @Override
    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public long getTimeout() {
        return properties.getTypedProperty(TIMEOUT, DEFAULT_TIMEOUT).getMilliSeconds();
    }

    @Override
    public int getRetries() {
        return parseBigDecimalProperty(RETRIES, DEFAULT_RETRIES);
    }

    @Override
    public long getForcedDelay() {
        return properties.getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY).getMilliSeconds();
    }

    @Override
    public boolean getFixMbusHexShortId() {
        return properties.<Boolean>getTypedProperty(FIX_MBUS_HEX_SHORT_ID, DEFAULT_FIX_MBUS_HEX_SHORT_ID);
    }

    @Override
    public boolean isIgnoreDSTStatusCode() {
        return false;   //Sub classes can override
    }

    protected int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        return properties.getTypedProperty(key, defaultValue).intValue();
    }

    /**
     * Parse a BigDecimal property that has no default value.
     * Throw an error if no value was configured for this property.
     */
    protected int parseBigDecimalProperty(String key) {
        final BigDecimal value = properties.getTypedProperty(key);
        if (value != null) {
            return value.intValue();
        } else {
            throw DeviceConfigurationException.missingProperty(key);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        Collections.addAll(
                propertySpecs,
                this.bigDecimalSpec(TranslationKeys.SERVER_UPPER_MAC_ADDRESS_TK, DEFAULT_UPPER_SERVER_MAC_ADDRESS),
                this.bigDecimalSpec(TranslationKeys.SERVER_LOWER_MAC_ADDRESS_TK, DEFAULT_LOWER_SERVER_MAC_ADDRESS),
                this.bigDecimalSpec(TranslationKeys.ADDRESSING_MODE_TK, DEFAULT_ADDRESSING_MODE),
                this.stringSpec(TranslationKeys.MANUFACTURER_TK, DEFAULT_MANUFACTURER, "WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000"),
                this.bigDecimalSpec(TranslationKeys.INFORMATION_FIELD_SIZE_TK, DEFAULT_INFORMATION_FIELD_SIZE),
                this.booleanSpec(TranslationKeys.WAKE_UP_TK, DEFAULT_WAKE_UP),
                this.stringSpec(TranslationKeys.DEVICE_ID_TK, DEFAULT_DEVICE_ID),
                this.bigDecimalSpec(TranslationKeys.CIPHERING_TYPE_TK, DEFAULT_CIPHERING_TYPE),
                this.booleanSpec(TranslationKeys.NTA_SIMULATION_TOOL_TK, DEFAULT_NTA_SIMULATION_TOOL),
                this.booleanSpec(TranslationKeys.BULK_REQUEST_TK, DEFAULT_BULK_REQUEST),
                this.bigDecimalSpec(TranslationKeys.CONFORMANCE_BLOCK_VALUE_TK, DEFAULT_CONFORMANCE_BLOCK_VALUE_LN),
                this.booleanSpec(TranslationKeys.VALIDATE_INVOKE_ID_TK, DEFAULT_VALIDATE_INVOKE_ID),
                this.bigDecimalSpec(TranslationKeys.MAX_REC_PDU_SIZE_TK, DEFAULT_MAX_REC_PDU_SIZE),
                this.booleanSpec(TranslationKeys.REQUEST_TIMEZONE_TK, DEFAULT_REQUEST_TIMEZONE),
                this.bigDecimalSpec(TranslationKeys.ROUND_TRIP_CORRECTION_TK, DEFAULT_ROUND_TRIP_CORRECTION),
                this.booleanSpec(TranslationKeys.FIX_MBUS_HEX_SHORT_ID_TK, DEFAULT_FIX_MBUS_HEX_SHORT_ID));
        return propertySpecs;
    }


    protected PropertySpec bigDecimalSpec(TranslationKeys translationKeys, BigDecimal defaultValue) {
        return getPropertySpecService()
                .bigDecimalSpec()
                .named(translationKeys.getPropertySpecName(), translationKeys)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec stringSpec(TranslationKeys translationKeys, String defaultValue, String... possibleValues) {
        PropertySpecBuilder<String> propertySpecBuilder = getPropertySpecService()
                .stringSpec()
                .named(translationKeys.getPropertySpecName(), translationKeys)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(defaultValue)
                .addValues(possibleValues);
        if (possibleValues.length > 0) {
            propertySpecBuilder.markExhaustive();
        }
        return propertySpecBuilder.finish();
    }

    protected PropertySpec booleanSpec(TranslationKeys translationKey, Boolean defaultValue) {
        return getPropertySpecService()
                .booleanSpec()
                .named(translationKey.getPropertySpecName(), translationKey)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(defaultValue)
                .finish();
    }
    @Override
    public boolean isGeneralSigning() {
        return false;
    }

    public GeneralCipheringKeyType getGeneralCipheringKeyType() {
        String keyTypeDescription = properties.getStringProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);

        if (keyTypeDescription == null && getCipheringType().equals(CipheringType.GENERAL_CIPHERING)) {
            //In the case of general-ciphering, the key type is a required property
            throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);
        } else {
            return keyTypeDescription == null ? null : GeneralCipheringKeyType.fromDescription(keyTypeDescription);
        }
    }

}