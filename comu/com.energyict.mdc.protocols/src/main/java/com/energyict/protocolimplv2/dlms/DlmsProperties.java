package com.energyict.protocolimplv2.dlms;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimplv2.common.BasicDynamicPropertySupport;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Class that holds all DLMS device properties (general, dialect & security related)
 * Based on these properties, a DLMS session and its connection layer can be fully configured.
 *
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class DlmsProperties extends BasicDynamicPropertySupport implements DlmsSessionProperties {

    public static final String SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";
    public static final String SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";
    public static final String ADDRESSING_MODE = "AddressingMode";
    public static final String MANUFACTURER = "Manufacturer";
    public static final String INFORMATION_FIELD_SIZE = "InformationFieldSize";
    public static final String WAKE_UP = "WakeUp";
    public static final String DEVICE_ID = "DeviceId";
    public static final String CIPHERING_TYPE = "CipheringType";
    public static final String NTA_SIMULATION_TOOL = "NTASimulationTool";
    public static final String BULK_REQUEST = "BulkRequest";
    public static final String CONFORMANCE_BLOCK_VALUE = "ConformanceBlockValue";
    public static final String VALIDATE_INVOKE_ID = "ValidateInvokeId";
    public static final String MAX_REC_PDU_SIZE = "MaxRecPDUSize";
    public static final String REQUEST_TIMEZONE = "RequestTimeZone";
    public static final String ROUND_TRIP_CORRECTION = "RoundTripCorrection";
    public static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";

    public static final BigDecimal DEFAULT_UPPER_SERVER_MAC_ADDRESS = BigDecimal.ONE;
    public static final BigDecimal DEFAULT_LOWER_SERVER_MAC_ADDRESS = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_ADDRESSING_MODE = new BigDecimal(2);
    public static final String DEFAULT_MANUFACTURER = "WKP";
    public static final BigDecimal DEFAULT_INFORMATION_FIELD_SIZE = new BigDecimal(-1);
    public static final Boolean DEFAULT_WAKE_UP = false;
    public static final String DEFAULT_DEVICE_ID = "";
    public static final BigDecimal DEFAULT_CIPHERING_TYPE = new BigDecimal(CipheringType.GLOBAL.getType());
    public static final Boolean DEFAULT_NTA_SIMULATION_TOOL = false;
    public static final Boolean DEFAULT_BULK_REQUEST = false;
    public static final BigDecimal DEFAULT_CONFORMANCE_BLOCK_VALUE_LN = new BigDecimal(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
    public static final BigDecimal DEFAULT_CONFORMANCE_BLOCK_VALUE_SN = new BigDecimal(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
    public static final String DEFAULT_SYSTEM_IDENTIFIER = "EICTCOMM";
    public static final BigDecimal DEFAULT_INVOKE_ID_AND_PRIORITY = new BigDecimal(66); // 0x42, 0b01000010 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]
    public static final Boolean DEFAULT_VALIDATE_INVOKE_ID = false;
    public static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(4096);
    public static final BigDecimal DEFAULT_PROPOSED_DLMS_VERSION = new BigDecimal(6);
    public static final BigDecimal DEFAULT_PROPOSED_QOS = new BigDecimal(-1);
    public static final Boolean DEFAULT_REQUEST_TIMEZONE = false;
    public static final BigDecimal DEFAULT_ROUND_TRIP_CORRECTION = new BigDecimal(0);
    public static final Boolean DEFAULT_FIX_MBUS_HEX_SHORT_ID = false;

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private SecurityProvider securityProvider;
    private String serialNumber = "";

    public DlmsProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
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
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), BigDecimal.ONE);
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
        return parseBigDecimalProperty(ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @Override
    public String getManufacturer() {
        return properties.getTypedProperty(MANUFACTURER, DEFAULT_MANUFACTURER);
    }

    @Override
    public int getInformationFieldSize() {
        return parseBigDecimalProperty(INFORMATION_FIELD_SIZE, DEFAULT_INFORMATION_FIELD_SIZE);
    }

    @Override
    public boolean isWakeUp() {
        return properties.getTypedProperty(WAKE_UP, DEFAULT_WAKE_UP);
    }

    @Override
    public String getDeviceId() {
        return properties.getTypedProperty(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    @Override
    public CipheringType getCipheringType() {
        return CipheringType.fromValue(parseBigDecimalProperty(CIPHERING_TYPE, DEFAULT_CIPHERING_TYPE));
    }

    @Override
    public boolean isNtaSimulationTool() {
        return properties.getTypedProperty(NTA_SIMULATION_TOOL, DEFAULT_NTA_SIMULATION_TOOL);
    }

    @Override
    public boolean isBulkRequest() {
        return getProperties().getTypedProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        BigDecimal defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_LN;
        if (getReference().equals(DLMSReference.SN)) {
            defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_SN;
        }
        return new ConformanceBlock(parseBigDecimalProperty(CONFORMANCE_BLOCK_VALUE, defaultValue));
    }

    @Override
    public byte[] getSystemIdentifier() {
        return DEFAULT_SYSTEM_IDENTIFIER.getBytes();
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        byte invokeIdAndPriority = (byte) (DEFAULT_INVOKE_ID_AND_PRIORITY.intValue());
        if (properties.<Boolean>getTypedProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID)) {
            return new IncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        } else {
            return new NonIncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        }
    }

    @Override
    public int getMaxRecPDUSize() {
        return parseBigDecimalProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
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
        return properties.<Boolean>getTypedProperty(REQUEST_TIMEZONE, DEFAULT_REQUEST_TIMEZONE);
    }

    @Override
    public int getRoundTripCorrection() {
        return parseBigDecimalProperty(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
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

    protected int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        return properties.getTypedProperty(key, defaultValue).intValue();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.addAll(Arrays.asList(
                getPropertySpecService().bigDecimalPropertySpec(SERVER_UPPER_MAC_ADDRESS, false, DEFAULT_UPPER_SERVER_MAC_ADDRESS),
                getPropertySpecService().bigDecimalPropertySpec(SERVER_LOWER_MAC_ADDRESS, false, DEFAULT_LOWER_SERVER_MAC_ADDRESS),
                getPropertySpecService().bigDecimalPropertySpec(ADDRESSING_MODE, false, DEFAULT_ADDRESSING_MODE),
                getPropertySpecService().stringPropertySpecWithValuesAndDefaultValue(MANUFACTURER, false, DEFAULT_MANUFACTURER, "WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000"),
                getPropertySpecService().bigDecimalPropertySpec(INFORMATION_FIELD_SIZE, false, DEFAULT_INFORMATION_FIELD_SIZE),
                getPropertySpecService().booleanPropertySpec(WAKE_UP, false, DEFAULT_WAKE_UP),
                getPropertySpecService().stringPropertySpec(DEVICE_ID, false, DEFAULT_DEVICE_ID),
                getPropertySpecService().bigDecimalPropertySpec(CIPHERING_TYPE, false, DEFAULT_CIPHERING_TYPE),
                getPropertySpecService().booleanPropertySpec(NTA_SIMULATION_TOOL, false, DEFAULT_NTA_SIMULATION_TOOL),
                getPropertySpecService().booleanPropertySpec(BULK_REQUEST, false, DEFAULT_BULK_REQUEST),
                getPropertySpecService().bigDecimalPropertySpec(CONFORMANCE_BLOCK_VALUE, false, DEFAULT_CONFORMANCE_BLOCK_VALUE_LN),
                getPropertySpecService().booleanPropertySpec(VALIDATE_INVOKE_ID, false, DEFAULT_VALIDATE_INVOKE_ID),
                getPropertySpecService().bigDecimalPropertySpec(MAX_REC_PDU_SIZE, false, DEFAULT_MAX_REC_PDU_SIZE),
                getPropertySpecService().booleanPropertySpec(REQUEST_TIMEZONE, false, DEFAULT_REQUEST_TIMEZONE),
                getPropertySpecService().bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, false, DEFAULT_ROUND_TRIP_CORRECTION),
                getPropertySpecService().booleanPropertySpec(FIX_MBUS_HEX_SHORT_ID, false, DEFAULT_FIX_MBUS_HEX_SHORT_ID)
        ));
        return propertySpecs;
    }

}