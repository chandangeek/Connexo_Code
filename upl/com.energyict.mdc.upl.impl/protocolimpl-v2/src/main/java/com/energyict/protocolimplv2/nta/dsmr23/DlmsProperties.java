package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.dlms.*;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.*;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

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

    /**
     * The default is non-pre-established associations.
     */
    private static final boolean PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED_DEFAULT = false;

    private final com.energyict.mdc.upl.TypedProperties properties;
    protected SecurityProvider securityProvider;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private String serialNumber = "";
    private Integer dataTransportSecurityLevel = null;

    public DlmsProperties() {
        this.properties = com.energyict.mdc.upl.TypedProperties.empty();
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties, true);
    }

    @Override
    public com.energyict.mdc.upl.TypedProperties getProperties() {
        return properties;
    }

    @Override
    public TimeZone getTimeZone() {
        final Object object = properties.getTypedProperty(TIMEZONE);

        if (object == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
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
        final BigDecimal value = (BigDecimal) getSecurityPropertySet().getClient();
        if (value != null) {
            return value.intValue();
        } else {
            return BigDecimal.ONE.intValue();
        }
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
    public byte[] getDeviceSystemTitle() {
        final String deviceSystemTitle = properties.getTypedProperty(DlmsProtocolProperties.DEVICE_SYSTEM_TITLE, "").trim();
        if (!deviceSystemTitle.isEmpty()) {
            try {
                byte[] bytes = ProtocolTools.getBytesFromHexString(deviceSystemTitle, "");
                if (bytes.length != 8) {
                    throw new Exception("incorrect length");
                }
                return bytes;
            } catch (Throwable e) {
                throw DeviceConfigurationException.invalidPropertyFormat(DlmsProtocolProperties.DEVICE_SYSTEM_TITLE, deviceSystemTitle, "Should be a hex string of 16 characters");
            }
        }

        return null;
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
            int securityAuthLevel = 0;
            if(getSecurityPropertySet() != null){
                securityAuthLevel = getSecurityPropertySet().getAuthenticationDeviceAccessLevel();
            }
            securityProvider = new NTASecurityProvider(properties, securityAuthLevel);
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
    public String getConnectionMode() {
            return properties.getTypedProperty(CONNECTION_MODE, DEFAULT_CONNECTION_MODE);
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(DlmsProtocolProperties.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, false);
    }

    @Override
    public long getTimeout() {
        return properties.getTypedProperty(TIMEOUT, Duration.ofMillis(DEFAULT_TIMEOUT.intValue())).toMillis();
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

    protected boolean parseBooleanProperty(String key, boolean defaultValue) {
        try {
            return properties.getTypedProperty(key, defaultValue).booleanValue();
        } catch (Throwable e) {
            return defaultValue;
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
        return properties.getTypedProperty(DlmsSessionProperties.POLLING_DELAY, Duration.ofMillis(0));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isPublicClientPreEstablished() {
        return this.properties.getTypedProperty(PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED_DEFAULT);
    }

    @Override
    public boolean validateLoadProfileChannels() {
        return this.properties.getTypedProperty(DlmsProtocolProperties.VALIDATE_LOAD_PROFILE_CHANNELS, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean incrementFrameCounterForReplyToHLS() {
        return this.properties.getTypedProperty(DlmsProtocolProperties.INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, false);
    }

    @Override
    public boolean isIgnoreDstStatusCode() {
        return this.properties.getTypedProperty(DlmsProtocolProperties.PROPERTY_IGNORE_DST_STATUS_CODE, false);
    }

    @Override
    public long getFrameCounterLimit() {
        try {
            return this.properties.getTypedProperty(FRAME_COUNTER_LIMIT, 0L);
        } catch (Exception ex){
            // catch any obsolete value (i.e. string)
            return 0;
        }
    }

}