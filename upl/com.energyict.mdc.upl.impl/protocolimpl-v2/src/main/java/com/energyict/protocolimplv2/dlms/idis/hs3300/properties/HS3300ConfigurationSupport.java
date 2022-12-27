package com.energyict.protocolimplv2.dlms.idis.hs3300.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.USE_GBT;

public class HS3300ConfigurationSupport extends AM130ConfigurationSupport {

    /**
     * Use service specific global ciphering by default.
     */
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GLOBAL;

    public static final String DLMS_WAN_KEK = "DlmsWanKEK";
    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";
    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep";
    public static final String IP_V4_ADDRESS = "IPv4Address";
    public static final String IP_V6_ADDRESS = "IPv6Address";
    public static final String SHORT_ADDRESS_PAN = "ShortAddressPAN";
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String MIRROR_LOGICAL_DEVICE_ID = "MirrorLogicalDeviceId";
    public static final String GATEWAY_LOGICAL_DEVICE_ID = "GatewayLogicalDeviceId";

    /**
     * Indicates whether the meter does not accept a time deviation other than undefined. (SAG again).
     */
    public static final String USE_UNDEFINED_AS_TIME_DEVIATION = "UseUndefinedAsTimeDeviation";

    /**
     * Indicates whether or not to skip the authentication tag validation.
     */
    public static final String SKIP_FC_AUTH_TAG_VALIDATION = "SkipFrameCounterAuthenticationTag";

    /**
     * The default max-apdu-size when using G3.
     */
    private static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(1224);

    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(17);
    public static final Duration DEFAULT_NOT_USED_AARQ_TIMEOUT = Duration.ofSeconds(0);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);

    private final PropertySpecService propertySpecService;

    public HS3300ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.bulkRequestPropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.dlmsWANKEKPropertySpec(),
                this.pskPropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.lastSeenDatePropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.pollingDelayPropertySpec(),
                this.initialFrameCounter(),
                this.requestAuthenticatedFrameCounter(),
                this.useCachedFrameCounter(),
                this.validateCachedFrameCounter(),
                this.frameCounterRecoveryRetries(),
                this.frameCounterRecoveryStep(),
                this.frameCounterLimit(),
                this.deviceSystemTitlePropertySpec(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.useUndefinedForTimeDeviation(),
                this.skipFramecounterAuthenticationTagValidation(),
                this.cipheringTypePropertySpec(),
                this.ipV4Address(),
                this.ipV6Address(),
                this.shortAddressPan(),
                this.increaseFrameCounterOnHLSReply(),
                this.masterKeyPropertySpec(),
                this.clientPrivateSigningKeyPropertySpec()
        );
    }

    /**
     * Property spec indicating whether or not to increment the FC for the reply to HLS.
     *
     * @return	The corresponding PropertySpec.
     */
    protected final PropertySpec increaseFrameCounterOnHLSReply() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, false, PropertyTranslationKeys.V2_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, getPropertySpecService()::booleanSpec).finish();
    }
    protected PropertySpec shortAddressPan() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.SHORT_ADDRESS_PAN, false, PropertyTranslationKeys.V2_SHORT_ADDRESS_PAN, getPropertySpecService()::bigDecimalSpec).finish();
    }

    protected PropertySpec ipV6Address() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.IP_V6_ADDRESS, false, PropertyTranslationKeys.V2_IP_V6_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    protected PropertySpec ipV4Address() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.IP_V4_ADDRESS, false, PropertyTranslationKeys.V2_IP_V4_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    protected PropertySpec frameCounterRecoveryRetries() {
        return this.bigDecimalSpec(HS3300ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100));
    }
    protected PropertySpec frameCounterRecoveryStep() {
        return this.bigDecimalSpec(HS3300ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE);
    }

    protected PropertySpec frameCounterLimit() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.FRAME_COUNTER_LIMIT, false, PropertyTranslationKeys.V2_NTA_FRAME_COUNTER_LIMIT, getPropertySpecService()::positiveBigDecimalSpec)
                .setDefaultValue(new BigDecimal(0)).finish();
    }

    protected PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, false, PropertyTranslationKeys.V2_DLMS_VALIDATE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    protected PropertySpec useGeneralBlockTransferPropertySpec() {
        return this.booleanSpec(USE_GBT, USE_GBT_DEFAULT_VALUE, PropertyTranslationKeys.V2_DLMS_USE_GBT);
    }

    protected PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return this.bigDecimalSpec(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE, PropertyTranslationKeys.V2_DLMS_GBT_WINDOW_SIZE);
    }

    /**
     * Returns the "SkipFrameCounterAuthenticationTag" property spec.
     *
     * @return The property specification.
     */
    protected final PropertySpec skipFramecounterAuthenticationTagValidation() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.SKIP_FC_AUTH_TAG_VALIDATION, false, PropertyTranslationKeys.V2_SKIP_FC_AUTH_TAG_VALIDATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Returns the "UseUndefinedAsTimeDeviation" property spec.
     *
     * @return The property specification.
     */
    protected final PropertySpec useUndefinedForTimeDeviation() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_TIME_DEVIATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(true)
                .finish();
    }

    /**
     * Indicates whether or not to use a cached frame counter.
     *
     * @return <code>true</code> for a cached frame counter, <code>false</code> if not.
     */
    protected PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_USE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    protected PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_REQUEST_AUTHENTICATE_FRAME_COUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    protected PropertySpec lastSeenDatePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(G3Properties.PROP_LASTSEENDATE, false, PropertyTranslationKeys.V2_DLMS_LAST_SEENDATE, getPropertySpecService()::bigDecimalSpec).finish();
    }

    protected PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ofSeconds(0), PropertyTranslationKeys.V2_DLMS_POLLING_DELAY);
    }

    protected PropertySpec dlmsWANKEKPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(DLMS_WAN_KEK, PropertyTranslationKeys.V2_EICT_DLMS_WAN_KEK);
    }

    protected PropertySpec pskPropertySpec() {
        return keyAccessorTypeReferencePropertySpec(G3Properties.PSK, PropertyTranslationKeys.V2_DLMS_PSK);
    }

    protected PropertySpec nodeAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), false, PropertyTranslationKeys.V2_DLMS_NODEID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    protected PropertySpec readCachePropertySpec() {
        return this.booleanSpec(Dsmr50Properties.READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE);
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(IDIS.CALLING_AP_TITLE, false, PropertyTranslationKeys.V2_DLMS_IDIS_CALLING_AP_TITLE, this.propertySpecService::stringSpec)
                .setDefaultValue(IDIS.CALLING_AP_TITLE_DEFAULT)
                .finish();
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return this.stringSpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, PropertyTranslationKeys.V2_DLMS_CALL_HOME_ID);
    }

    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_DLMS_SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_MIRROR_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    protected PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(HS3300ConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_GATEWAY_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    protected PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, false, PropertyTranslationKeys.V2_DLMS_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE)
                .finish();
    }

    protected PropertySpec aarqTimeoutPropertySpec() {
        return this.durationSpec(AARQ_TIMEOUT_PROPERTY, false, DEFAULT_NOT_USED_AARQ_TIMEOUT, PropertyTranslationKeys.V2_DLMS_AARQ_TIMEOUT);
    }

    protected PropertySpec aarqRetriesPropertySpec() {
        return this.bigDecimalSpec(AARQ_RETRIES_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_AARQ_RETRIES, BigDecimal.valueOf(2));
    }

    protected PropertySpec initialFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(INITIAL_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_INITIAL_FRAME_COUNTER, this.getPropertySpecService()::positiveBigDecimalSpec).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::stringSpec)
                .finish();
    }

    private PropertySpec booleanSpec(String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec bigDecimalSpec(String name, boolean required, TranslationKey translationKey, BigDecimal defaultValue, BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    protected PropertySpec keyAccessorTypeReferencePropertySpec(String name, TranslationKey translationKey) {
        return this.propertySpecService
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
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
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_DLMS_MAX_REC_PDU_SIZE);
    }

    /**
     * Returns the default ciphering type.
     *
     * @return The default ciphering type.
     */
    protected final CipheringType getDefaultCipheringType() {
        return DEFAULT_CIPHERING_TYPE;
    }

    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return this.keyAccessorTypeReferenceSpec(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY, PropertyTranslationKeys.V2_EICT_CLIENT_PRIVATE_SIGNING_KEY);
    }

    private PropertySpec keyAccessorTypeReferenceSpec(String name, PropertyTranslationKeys translationKey) {
        return getPropertySpecService()
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }
}
