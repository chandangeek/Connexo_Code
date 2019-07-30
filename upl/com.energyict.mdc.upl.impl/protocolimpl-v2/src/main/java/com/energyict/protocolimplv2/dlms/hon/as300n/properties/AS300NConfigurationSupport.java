package com.energyict.protocolimplv2.dlms.hon.as300n.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.*;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;


public class AS300NConfigurationSupport implements HasDynamicProperties {


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
    public static final String REPLAY_ATTACK_PREVENTION = "ReplayAttackPrevention";
    public static final String SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY = "SwapServerAndClientAddress";
    public static final String IGNORE_CALLING_AP_TITLE = "IgnoreCallingAPTitle";
    public static final String USE_LOGICAL_DEVICE_NAME_AS_SERIAL = "UseLogicalDeviceNameAsSerialNumber";
    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;

    public static final String CALLING_AP_TITLE = "CallingAPTitle";
    public static final String CALLING_AP_TITLE_DEFAULT = "0000000000000000";

    public static final String PSK = "PSK";
    public static final int DEFAULT_AARQ_TIMEOUT = 0;      //Means: not used
    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final String PROP_LASTSEENDATE = "LastSeenDate";

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


    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String MIRROR_LOGICAL_DEVICE_ID = "MirrorLogicalDeviceId";
    public static final String GATEWAY_LOGICAL_DEVICE_ID = "GatewayLogicalDeviceId";



    private static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(207);

    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(256);
    public static final BigDecimal DEFAULT_SERVER_UPPER_MAC_ADDRESS = BigDecimal.valueOf(1);
    public static final Duration DEFAULT_NOT_USED_AARQ_TIMEOUT = Duration.ofSeconds(0);
    private static final String DEFAULT_DEVICE_ID = "";
    private static final String DEFAULT_MANUFACTURER = "ELS";
    private final PropertySpecService propertySpecService;


    public AS300NConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.ignoreDstStatusCode(),
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
                this.masterKeyPropertySpec(),
                this.conformanceBlockValuePropertySpec(),
                this.requestTimeZonePropertySpec(),
                this.manufacturerPropertySpec(),
                this.replayAttackPreventionPropertySpec(),
                this.ntaSimulationToolPropertySpec(),
                this.fixMbusHexShortIdPropertySpec(),
                this.publicClientPreEstablishedPropertySpec(),
                this.serverUpperMacAddressPropertySpec()
        );

    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // not set
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    private PropertySpecBuilder<Boolean> booleanSpecBuilder(String name, TranslationKey translationKey) {
        return getPropertySpecService()
                .booleanSpec()
                .named(name, translationKey).describedAs(new DescriptionTranslationKey(translationKey));
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

    protected PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec stringWithDefaultSpec(String name, boolean required, TranslationKey translationKey, String defaultValue, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }


    private PropertySpec booleanSpec(String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec keyAccessorTypeReferencePropertySpec(String name, TranslationKey translationKey) {
        return getPropertySpecService()
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }




    // ----- actual properties

    protected PropertySpec ignoreDstStatusCode() {
        return booleanSpecBuilder(
                DlmsProtocolProperties.PROPERTY_IGNORE_DST_STATUS_CODE, PropertyTranslationKeys.V2_NTA_IGNORE_DST_STATUS_CODE)
                .setDefaultValue(false)
                .finish();
    }

    protected PropertySpec deviceId() {
        return this.stringWithDefaultSpec(
                DlmsProtocolProperties.DEVICE_ID, false, PropertyTranslationKeys.V2_ELSTER_DEVICE_ID,
                DEFAULT_DEVICE_ID);
    }

    protected PropertySpec requestTimeZonePropertySpec() {
        return this.booleanSpecBuilder(
                DlmsProtocolProperties.REQUEST_TIMEZONE, PropertyTranslationKeys.V2_ELSTER_REQUEST_TIMEZONE).finish();
    }


    protected PropertySpec conformanceBlockValuePropertySpec() {
        return bigDecimalSpec(
                DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE, false, PropertyTranslationKeys.V2_ELSTER_CONFORMANCE_BLOCK_VALUE,
                BigDecimal.valueOf(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK));
    }

    protected PropertySpec manufacturerPropertySpec() {
        return stringWithDefaultSpec(
                DlmsProtocolProperties.MANUFACTURER, false, PropertyTranslationKeys.V2_ELSTER_MANUFACTURER, DEFAULT_MANUFACTURER, "WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000");
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return this.booleanSpecBuilder(DlmsProtocolProperties.BULK_REQUEST, PropertyTranslationKeys.V2_ELSTER_BULK_REQUEST).finish();
    }


    protected PropertySpec replayAttackPreventionPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(REPLAY_ATTACK_PREVENTION, false, PropertyTranslationKeys.V2_NTA_REPLAY_ATTACK_PREVENTION, getPropertySpecService()::booleanSpec).finish();
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(DlmsProtocolProperties.CIPHERING_TYPE, false, PropertyTranslationKeys.V2_ELSTER_CIPHERING_TYPE, getPropertySpecService()::stringSpec)
                .setDefaultValue(DEFAULT_CIPHERING_TYPE.getDescription())
                .addValues(
                        CipheringType.GLOBAL.getDescription(),
                        CipheringType.DEDICATED.getDescription(),
                        CipheringType.GENERAL_GLOBAL.getDescription(),
                        CipheringType.GENERAL_DEDICATED.getDescription(),
                        CipheringType.GENERAL_CIPHERING.getDescription(),
                        CipheringType.INVALID.getDescription()
                ).markExhaustive()
                .finish();
    }

    protected PropertySpec ntaSimulationToolPropertySpec() {
        return this.booleanSpecBuilder(DlmsProtocolProperties.NTA_SIMULATION_TOOL, PropertyTranslationKeys.V2_ELSTER_NTA_SIMULATION_TOOL).finish();
    }

    protected PropertySpec fixMbusHexShortIdPropertySpec() {
        return this.booleanSpecBuilder(DlmsProtocolProperties.FIX_MBUS_HEX_SHORT_ID, PropertyTranslationKeys.V2_ELSTER_FIX_MBUS_HEX_SHORT_ID).finish();
    }

    /**
     * Property that can be used to indicate whether or not the public client has a pre-established association.
     *
     * @return The property specification.
     */
    protected final PropertySpec publicClientPreEstablishedPropertySpec() {
        return this.booleanSpecBuilder(com.energyict.dlms.protocolimplv2.DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, PropertyTranslationKeys.V2_ELSTER_PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED).finish();
    }

    protected PropertySpec validateLoadProfileChannelsPropertySpec() {
        return this.booleanSpecBuilder(DlmsProtocolProperties.VALIDATE_LOAD_PROFILE_CHANNELS, PropertyTranslationKeys.V2_ELSTER_VALIDATE_LOAD_PROFILE_CHANNELS).finish();
    }
    /**
     * Property spec indicating whether or not to increment the FC for the reply to HLS.
     *
     * @return	The corresponding PropertySpec.
     */
    private final PropertySpec increaseFrameCounterOnHLSReply() {
        return UPLPropertySpecFactory.specBuilder(INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, false, PropertyTranslationKeys.V2_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, getPropertySpecService()::booleanSpec).finish();
    }
    private PropertySpec shortAddressPan() {
        return UPLPropertySpecFactory.specBuilder(SHORT_ADDRESS_PAN, false, PropertyTranslationKeys.V2_SHORT_ADDRESS_PAN, getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec ipV6Address() {
        return UPLPropertySpecFactory.specBuilder(IP_V6_ADDRESS, false, PropertyTranslationKeys.V2_IP_V6_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec ipV4Address() {
        return UPLPropertySpecFactory.specBuilder(IP_V4_ADDRESS, false, PropertyTranslationKeys.V2_IP_V4_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec frameCounterRecoveryRetries() {
        return bigDecimalSpec(FRAME_COUNTER_RECOVERY_RETRIES, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100));
    }
    private PropertySpec frameCounterRecoveryStep() {
        return bigDecimalSpec(FRAME_COUNTER_RECOVERY_STEP, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE);
    }

    private PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(VALIDATE_CACHED_FRAMECOUNTER, false, PropertyTranslationKeys.V2_DLMS_VALIDATE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    /**
     * Returns the "SupportsHundrethsTimefield" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec supportsHundrethsTimeField() {
        return UPLPropertySpecFactory.specBuilder(SUPPORTS_HUNDRETHS_TIMEFIELD, false, PropertyTranslationKeys.V2_SUPPORTS_HUNDRETHS_TIMEFIELD, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Returns the "SkipFrameCounterAuthenticationTag" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec skipFramecounterAuthenticationTagValidation() {
        return UPLPropertySpecFactory.specBuilder(SKIP_FC_AUTH_TAG_VALIDATION, false, PropertyTranslationKeys.V2_SKIP_FC_AUTH_TAG_VALIDATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Returns the "SkipSlaveDecives" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec skipSlaveDevices() {
        return UPLPropertySpecFactory.specBuilder(SKIP_SLAVE_DEVICES, false, PropertyTranslationKeys.V2_SKIP_SLAVE_DEVICES, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Returns the "UseUndefinedAsClockStatus" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec useUndefinedForClockStatus() {
        return UPLPropertySpecFactory.specBuilder(USE_UNDEFINED_AS_CLOCK_STATUS, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_CLOCK_STATUS, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Returns the "UseUndefinedAsTimeDeviation" property spec.
     *
     * @return The property specification.
     */
    private final PropertySpec useUndefinedForTimeDeviation() {
        return UPLPropertySpecFactory.specBuilder(USE_UNDEFINED_AS_TIME_DEVIATION, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_TIME_DEVIATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Indicates whether or not to use a fixed object list.
     *
     * @return Whether or not to use a fixed object list.
     */
    private final PropertySpec useFixedObjectList() {
        return UPLPropertySpecFactory.specBuilder(USE_FIXED_OBJECT_LIST, false, PropertyTranslationKeys.V2_USE_FIXED_OBJECT_LIST, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Indicates whether or not to use a cached frame counter.
     *
     * @return <code>true</code> for a cached frame counter, <code>false</code> if not.
     */
    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_USE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(REQUEST_AUTHENTICATED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_REQUEST_AUTHENTICATE_FRAME_COUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROP_LASTSEENDATE, false, PropertyTranslationKeys.V2_DLMS_LAST_SEENDATE, getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ofSeconds(0), PropertyTranslationKeys.V2_DLMS_POLLING_DELAY);
    }

    private PropertySpec pskPropertySpec() {
        return keyAccessorTypeReferencePropertySpec(PSK, PropertyTranslationKeys.V2_DLMS_PSK);
    }

    protected PropertySpec nodeAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), false, PropertyTranslationKeys.V2_DLMS_NODEID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_DLMS_SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(MIRROR_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_MIRROR_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    protected PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(GATEWAY_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_GATEWAY_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
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
        return bigDecimalSpec(DlmsProtocolProperties.MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_DLMS_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, DEFAULT_SERVER_UPPER_MAC_ADDRESS, PropertyTranslationKeys.V2_DLMS_SERVER_UPPER_MAC_ADDRESS);
    }

    protected PropertySpec useGeneralBlockTransferPropertySpec() {
        return this.booleanSpec(USE_GBT, USE_GBT_DEFAULT_VALUE, PropertyTranslationKeys.V2_DLMS_USE_GBT);
    }

    protected PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return this.bigDecimalSpec(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE, PropertyTranslationKeys.V2_DLMS_GBT_WINDOW_SIZE);
    }

    protected PropertySpec masterKeyPropertySpec() {
        return keyAccessorTypeReferencePropertySpec(MASTER_KEY, PropertyTranslationKeys.V2_NTA_MASTERKEY);
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(CALLING_AP_TITLE, false, PropertyTranslationKeys.V2_DLMS_IDIS_CALLING_AP_TITLE, this.propertySpecService::stringSpec)
                .setDefaultValue(CALLING_AP_TITLE_DEFAULT)
                .finish();
    }

    protected PropertySpec limitMaxNrOfDaysPropertySpec() {
        return this.bigDecimalSpec(LIMIT_MAX_NR_OF_DAYS_PROPERTY, BigDecimal.ZERO, PropertyTranslationKeys.V2_DLMS_LIMIT_MAX_NR_OF_DAYS);
    }

    protected PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEZONE, false, PropertyTranslationKeys.V2_DLMS_TIMEZONE, this.propertySpecService::timeZoneSpec)
                .finish();
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return this.booleanSpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID, PropertyTranslationKeys.V2_DLMS_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec readCachePropertySpec() {
        return this.booleanSpec(READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(FORCED_DELAY, false, PropertyTranslationKeys.V2_DLMS_FORCED_DELAY, this.propertySpecService::durationSpec)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return stringWithDefaultSpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false, PropertyTranslationKeys.V2_DLMS_CALL_HOME_ID, "");
    }

}