package com.energyict.protocolimplv2.dlms.hon.as300n.properties;

import com.energyict.dlms.*;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.exception.DeviceConfigurationException;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FRAME_COUNTER_LIMIT;
import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;

public class AS300NProperties implements DlmsSessionProperties {
    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    public static final Boolean DEFAULT_BULK_REQUEST = true;
    private static final boolean PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED_DEFAULT = false;

    private final com.energyict.mdc.upl.TypedProperties properties;
    protected SecurityProvider securityProvider;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private String serialNumber = "";
    private Integer dataTransportSecurityLevel = null;

    public AS300NProperties() {
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
        String namedCipheringType = getProperties().getTypedProperty(CIPHERING_TYPE, CipheringType.GLOBAL.getDescription());
        return CipheringType.fromDescription(namedCipheringType);
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
    public ConformanceBlock getConformanceBlock() {
        BigDecimal defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_LN;
        if (getReference().equals(DLMSReference.SN)) {
            defaultValue = DEFAULT_CONFORMANCE_BLOCK_VALUE_SN;
        }
        return new ConformanceBlock(parseBigDecimalProperty(CONFORMANCE_BLOCK_VALUE, defaultValue));
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
            //TODO port security provider
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
            return this.properties.getTypedProperty(FRAME_COUNTER_LIMIT, 0);
        } catch (Exception ex){
            // catch any obsolete value (i.e. string)
            return 0;
        }
    }



    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    @Override
    public byte[] getSystemIdentifier() {
        //Property CallingAPTitle is used as system identifier in the AARQ
        final boolean ignoreCallingAPTitle = getProperties().getTypedProperty(AS300NConfigurationSupport.IGNORE_CALLING_AP_TITLE, false);
        if (!ignoreCallingAPTitle) {
            final String callingAPTitle = getProperties().getTypedProperty(AS300NConfigurationSupport.CALLING_AP_TITLE, AS300NConfigurationSupport.CALLING_AP_TITLE_DEFAULT).trim();
            try {
                return ProtocolTools.getBytesFromHexString(callingAPTitle, "");
            } catch (Throwable e) {
                throw DeviceConfigurationException.invalidPropertyFormat(AS300NConfigurationSupport.CALLING_AP_TITLE, callingAPTitle, "Should be a hex string of 16 characters");
            }
        }
        return null;
    }

    @Override
    public boolean isSwitchAddresses() {
        return getProperties().<Boolean>getTypedProperty(AS300NConfigurationSupport.SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY, true);
    }

    public long getLimitMaxNrOfDays() {
        return getProperties().getTypedProperty(
                AS300NConfigurationSupport.LIMIT_MAX_NR_OF_DAYS_PROPERTY,
                BigDecimal.valueOf(0)   // Do not limit, but use as-is
        ).longValue();
    }

    @Override
    public boolean timeoutMeansBrokenConnection() {
        return useBeaconMirrorDeviceDialect() || useSerialDialect();
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useSerialDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.SERIAL_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useLogicalDeviceNameAsSerialNumber() {
        return getProperties().<Boolean>getTypedProperty(AS300NConfigurationSupport.USE_LOGICAL_DEVICE_NAME_AS_SERIAL, false);
    }

    public boolean useUndefinedAsTimeDeviation() {
        return getProperties().<Boolean>getTypedProperty(AS300NConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false);
    }

    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @Override
    public boolean incrementFrameCounterForRetries() {
        return true;
    }

    public boolean usesPublicClient() {
        return getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS;
    }


    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public long getInitialFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(100)).longValue();
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.AARQ_TIMEOUT_PROPERTY, AS300NConfigurationSupport.DEFAULT_NOT_USED_AARQ_TIMEOUT).toMillis();
    }




}