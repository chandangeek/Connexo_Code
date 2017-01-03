package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;
import static com.energyict.dlms.common.DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_BULK_REQUEST;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_CIPHERING_TYPE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_CONFORMANCE_BLOCK_VALUE_LN;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_CONFORMANCE_BLOCK_VALUE_SN;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_DEVICE_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ENABLE_GBT;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FIX_MBUS_HEX_SHORT_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_INFORMATION_FIELD_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_INVOKE_ID_AND_PRIORITY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_LOWER_SERVER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MANUFACTURER;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_NTA_SIMULATION_TOOL;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_PROPOSED_DLMS_VERSION;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_PROPOSED_QOS;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_REQUEST_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_SYSTEM_IDENTIFIER;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_UPPER_SERVER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_VALIDATE_INVOKE_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_WAKE_UP;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEVICE_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.FIX_MBUS_HEX_SHORT_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.INFORMATION_FIELD_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.MANUFACTURER;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.NTA_SIMULATION_TOOL;
import static com.energyict.dlms.common.DlmsProtocolProperties.REQUEST_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.USE_GBT;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.WAKE_UP;

/**
 * Class that holds all DLMS device properties (general, dialect & security related)
 * Based on these properties, a DLMS session and its connection layer can be fully configured.
 * <p/>
 * The list of optional and required properties that is shown in EIServer is held in {@link DlmsConfigurationSupport}
 * <p/>
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class DlmsProperties implements DlmsSessionProperties {

    /** The default is non-pre-established associations. */
	private static final boolean PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED_DEFAULT = false;

    private final com.energyict.protocolimpl.properties.TypedProperties properties;
    protected SecurityProvider securityProvider;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private String serialNumber = "";
    private Integer dataTransportSecurityLevel = null;

    public DlmsProperties() {
        this.properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties);
    }

    @Override
    public com.energyict.protocolimpl.properties.TypedProperties getProperties() {
        return properties;
    }

    @Override
    public TimeZone getTimeZone() {
        final Object object = properties.getTypedProperty(TIMEZONE);

        if (object == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else if (object instanceof TimeZoneInUse) {
            TimeZoneInUse timeZoneInUse = (TimeZoneInUse) object;
            if (timeZoneInUse.getTimeZone() == null) {
                return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
            } else {
                return timeZoneInUse.getTimeZone();
            }
        } else if (object instanceof TimeZone) {
            return (TimeZone) object;
        } else {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        }
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet;
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet securityPropertySet) {
        this.securityPropertySet = securityPropertySet;
    }

    @Override
    public int getAuthenticationSecurityLevel() {
        return securityPropertySet.getAuthenticationDeviceAccessLevel();
    }

    @Override
    public int getDataTransportSecurityLevel() {
        if (dataTransportSecurityLevel == null) {
            dataTransportSecurityLevel = doGetDataTransportSecurityLevel();
        }
        return dataTransportSecurityLevel;
    }

    @Override
    public void setDataTransportSecurityLevel(int dataTransportSecurityLevel) {
        this.dataTransportSecurityLevel = dataTransportSecurityLevel;
    }

    protected int doGetDataTransportSecurityLevel() {
        return this.getSecurityPropertySet().getEncryptionDeviceAccessLevel();
    }

    @Override
    public int getSecuritySuite() {
        return 0;
    }

    @Override
    public void setSecuritySuite(int securitySuite) {
        //Not used yet for most protocols, subclasses can override
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
        return properties.<Boolean>getTypedProperty(WAKE_UP, DEFAULT_WAKE_UP);
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
        try {
            return properties.<Boolean>getTypedProperty(NTA_SIMULATION_TOOL, DEFAULT_NTA_SIMULATION_TOOL);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
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
        if (securityProvider == null) {
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
        if (serialNumber == null || serialNumber.isEmpty()) {
            serialNumber = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "");
        }
        return serialNumber;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public long getTimeout() {
        return properties.getTypedProperty(TIMEOUT, new TimeDuration(DEFAULT_TIMEOUT.intValue() / 1000)).getMilliSeconds();
    }

    @Override
    public int getRetries() {
        return parseBigDecimalProperty(RETRIES, DEFAULT_RETRIES);
    }

    @Override
    public long getForcedDelay() {
        return properties.getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY).toMillis();
    }

    @Override
    public boolean getFixMbusHexShortId() {
        return properties.<Boolean>getTypedProperty(FIX_MBUS_HEX_SHORT_ID, DEFAULT_FIX_MBUS_HEX_SHORT_ID);
    }

    protected int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        try {
            return properties.getTypedProperty(key, defaultValue).intValue();
        } catch (Throwable e) {
            return defaultValue.intValue();
        }
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
    public boolean useGeneralBlockTransfer() {
        return properties.<Boolean>getTypedProperty(USE_GBT, DEFAULT_ENABLE_GBT);
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return properties.getTypedProperty(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE).intValue();
    }

    @Override
    public Duration getPollingDelay() {
        // Return the default value, 100 ms.
        return Duration.ofMillis(100);
    }

    @Override
    public GeneralCipheringKeyType getGeneralCipheringKeyType() {
        String keyTypeDescription = properties.getTypedProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);

        if (keyTypeDescription == null && getCipheringType().equals(CipheringType.GENERAL_CIPHERING)) {
            //In the case of general-ciphering, the key type is a required property
            throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);
        } else {
            return keyTypeDescription == null ? null : GeneralCipheringKeyType.fromDescription(keyTypeDescription);
        }
    }

    @Override
    public boolean isGeneralSigning() {
        return false;
    }

    @Override
    public boolean timeoutMeansBrokenConnection() {
        /*
         * By default, for all protocols, a timeout means that the available connection to the DLMS device is broken and can no longer be used,
         * not even for other physical slave devices that share the same connection.
         */
        return true;
    }

    @Override
    public boolean incrementFrameCounterForRetries() {
        return true;    // Protocols who don't want the frame counter to be increased for retries, can override this method
    }

	@Override
	public final boolean isPublicClientPreEstablished() {
		return this.properties.getTypedProperty(PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED_DEFAULT);
	}

}