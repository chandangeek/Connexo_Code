package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_LOAD_PROFILE_CHANNELS;

/**
 * @author sva
 * @since 11/08/2015 - 15:15
 */
public class AM540ConfigurationSupport extends AM130ConfigurationSupport {

    /**
     * Use service specific global ciphering by default.
     */
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GLOBAL;

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";
    public static final String USE_METER_IN_TRANSPARENT_MODE = "UseMeterInTransparentMode";
    public static final String TRANSP_CONNECT_TIME = "TransparentConnectTime";
    public static final String PASSWORD = "IEC1107Password";
    public static final String METER_SECURITY_LEVEL = "SecurityLevel";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";
    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep";
    public static final String IP_V4_ADDRESS = "IPv4Address";
    public static final String IP_V6_ADDRESS = "IPv6Address";
    public static final String SHORT_ADDRESS_PAN = "ShortAddressPAN";
    public static final String DEFAULT_TRANSPARENT_PASSWORD = "00000000";
    public static final String DEFAULT_TRANSPARENT_SECURITY_LEVEL = "1:0";

    /**
     * Indicates whether the meter supports hundreths or not.
     * <p/>
     * For example SAG meters will generate an other-reason if this field is included.
     */
    public static final String SUPPORTS_HUNDRETHS_TIMEFIELD = "SupportsHundredthsTimeField";

    /**
     * Indicates whether the meter does not accept a time deviation other than undefined. (SAG again).
     */
    public static final String USE_UNDEFINED_AS_TIME_DEVIATION = "UseUndefinedAsTimeDeviation";

    /**
     * Indicates whether the meter will accept anything else but undefined as clock status.
     */
    public static final String USE_UNDEFINED_AS_CLOCK_STATUS = "UseUndefinedAsClockStatus";

    /**
     * Indicates whether or not to skip the authentication tag validation.
     */
    public static final String SKIP_FC_AUTH_TAG_VALIDATION = "SkipFrameCounterAuthenticationTag";

    /**
     * Indicates whether or not to use a static object list.
     */
    public static final String USE_FIXED_OBJECT_LIST = "UseFixedObjectList";

    /**
     * Skips slave devices.
     */
    public static final String SKIP_SLAVE_DEVICES = "SkipSlaveDevices";

    /**
     * The default max-apdu-size when using G3.
     */
    private static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(1224);

    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(17);
    public static final Duration DEFAULT_NOT_USED_AARQ_TIMEOUT = Duration.ofSeconds(0);

    public AM540ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.bulkRequestPropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.pskPropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.lastSeenDatePropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.pollingDelayPropertySpec(),
                this.initialFrameCounter(),
                this.useMeterInTransparentMode(),
                this.transparentConnectTime(),
                this.transparentSecurityLevel(),
                this.transparentPassword(),
                this.requestAuthenticatedFrameCounter(),
                this.useCachedFrameCounter(),
                this.validateCachedFrameCounter(),
                this.frameCounterRecoveryRetries(),
                this.frameCounterRecoveryStep(),
                this.deviceSystemTitlePropertySpec(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.supportsHundrethsTimeField(),
                this.useUndefinedForClockStatus(),
                this.useUndefinedForTimeDeviation(),
                this.skipFramecounterAuthenticationTagValidation(),
                this.useFixedObjectList(),
                this.skipSlaveDevices(),
                this.validateLoadProfileChannelsPropertySpec(),
                this.cipheringTypePropertySpec(),
                this.ipV4Address(),
                this.ipV6Address(),
                this.shortAddressPan(),
                this.increaseFrameCounterOnHLSReply(),
                this.masterKeyPropertySpec()
        );
    }

    /**
     * Property spec indicating whether or not to increment the FC for the reply to HLS.
     *
     * @return	The corresponding PropertySpec.
     */
    private final PropertySpec increaseFrameCounterOnHLSReply() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, false, PropertyTranslationKeys.V2_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, getPropertySpecService()::booleanSpec).finish();
    }
    private PropertySpec shortAddressPan() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.SHORT_ADDRESS_PAN, false, PropertyTranslationKeys.V2_SHORT_ADDRESS_PAN, getPropertySpecService()::bigDecimalSpec).finish();
    }

    protected PropertySpec ipV6Address() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.IP_V6_ADDRESS, false, PropertyTranslationKeys.V2_IP_V6_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    protected PropertySpec ipV4Address() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.IP_V4_ADDRESS, false, PropertyTranslationKeys.V2_IP_V4_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec frameCounterRecoveryRetries() {
        return this.bigDecimalSpec(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100));
    }
    private PropertySpec frameCounterRecoveryStep() {
        return this.bigDecimalSpec(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE);
    }

    private PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, false, PropertyTranslationKeys.V2_DLMS_VALIDATE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    /**
     * Returns the "SupportsHundrethsTimefield" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec supportsHundrethsTimeField() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.SUPPORTS_HUNDRETHS_TIMEFIELD, false, PropertyTranslationKeys.V2_SUPPORTS_HUNDRETHS_TIMEFIELD, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Returns the "SkipFrameCounterAuthenticationTag" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec skipFramecounterAuthenticationTagValidation() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.SKIP_FC_AUTH_TAG_VALIDATION, false, PropertyTranslationKeys.V2_SKIP_FC_AUTH_TAG_VALIDATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Returns the "SkipSlaveDecives" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec skipSlaveDevices() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.SKIP_SLAVE_DEVICES, false, PropertyTranslationKeys.V2_SKIP_SLAVE_DEVICES, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Returns the "UseUndefinedAsClockStatus" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec useUndefinedForClockStatus() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_UNDEFINED_AS_CLOCK_STATUS, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_CLOCK_STATUS, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Returns the "UseUndefinedAsTimeDeviation" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec useUndefinedForTimeDeviation() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_TIME_DEVIATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Indicates whether or not to use a fixed object list.
     *
     * @return Whether or not to use a fixed object list.
     */
    private final PropertySpec useFixedObjectList() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_FIXED_OBJECT_LIST, false, PropertyTranslationKeys.V2_USE_FIXED_OBJECT_LIST, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Indicates whether or not to use a cached frame counter.
     *
     * @return <code>true</code> for a cached frame counter, <code>false</code> if not.
     */
    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_USE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_REQUEST_AUTHENTICATE_FRAME_COUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(G3Properties.PROP_LASTSEENDATE, false, PropertyTranslationKeys.V2_DLMS_LAST_SEENDATE, getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ofSeconds(0), PropertyTranslationKeys.V2_DLMS_POLLING_DELAY);
    }

    private PropertySpec pskPropertySpec() {
        return keyAccessorTypeReferencePropertySpec(G3Properties.PSK, PropertyTranslationKeys.V2_DLMS_PSK);
    }

    protected PropertySpec nodeAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), false, PropertyTranslationKeys.V2_DLMS_NODEID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    @Override
    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_DLMS_SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_MIRROR_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    protected PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_GATEWAY_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, false, PropertyTranslationKeys.V2_DLMS_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE)
                .finish();
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return this.durationSpec(AARQ_TIMEOUT_PROPERTY, false, DEFAULT_NOT_USED_AARQ_TIMEOUT, PropertyTranslationKeys.V2_DLMS_AARQ_TIMEOUT);
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return this.bigDecimalSpec(AARQ_RETRIES_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_AARQ_RETRIES, BigDecimal.valueOf(2));
    }

    private PropertySpec initialFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(INITIAL_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_INITIAL_FRAME_COUNTER, this.getPropertySpecService()::positiveBigDecimalSpec).finish();
    }

    private PropertySpec useMeterInTransparentMode() {
        return UPLPropertySpecFactory.specBuilder(USE_METER_IN_TRANSPARENT_MODE, false, PropertyTranslationKeys.V2_DLMS_USE_METER_IN_TRANSPARENT_MODE, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec transparentConnectTime() {
        return this.bigDecimalSpec(TRANSP_CONNECT_TIME, false, PropertyTranslationKeys.V2_DLMS_TRANSP_CONNECT_TIME, BigDecimal.valueOf(10));
    }

    private PropertySpec transparentPassword() {
        return UPLPropertySpecFactory.specBuilder(PASSWORD, false, PropertyTranslationKeys.V2_DLMS_PASSWORD, this.getPropertySpecService()::stringSpec)
                .setDefaultValue(DEFAULT_TRANSPARENT_PASSWORD)
                .finish();
    }

    private PropertySpec transparentSecurityLevel() {
        return UPLPropertySpecFactory.specBuilder(METER_SECURITY_LEVEL, false, PropertyTranslationKeys.V2_DLMS_METER_SECURITY_LEVEL, this.getPropertySpecService()::stringSpec)
                .setDefaultValue(DEFAULT_TRANSPARENT_SECURITY_LEVEL)
                .finish();
    }

    protected PropertySpec bigDecimalSpec(String name, boolean required, TranslationKey translationKey, BigDecimal defaultValue, BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.getPropertySpecService()::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }

    public PropertySpec deviceSystemTitlePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.DEVICE_SYSTEM_TITLE, false, PropertyTranslationKeys.V2_DEVICE_SYSTEM_TITLE, this.getPropertySpecService()::stringSpec)
                .finish();
    }

    /**
     * Overriding this one for the max-apdu-size.
     * <p/>
     * {@inheritDoc}
     */
    protected final PropertySpec maxRecPduSizePropertySpec() {
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_DLMS_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec validateLoadProfileChannelsPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(VALIDATE_LOAD_PROFILE_CHANNELS, false, PropertyTranslationKeys.V2_VALIDATE_LOAD_PROFILE_CHANNELS, getPropertySpecService()::booleanSpec).finish();
    }

    /**
     * Returns the default ciphering type.
     *
     * @return The default ciphering type.
     */
    protected final CipheringType getDefaultCipheringType() {
        return DEFAULT_CIPHERING_TYPE;
    }
}