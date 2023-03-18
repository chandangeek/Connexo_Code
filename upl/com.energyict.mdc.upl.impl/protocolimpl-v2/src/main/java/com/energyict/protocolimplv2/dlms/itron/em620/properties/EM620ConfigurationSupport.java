package com.energyict.protocolimplv2.dlms.itron.em620.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;

public class EM620ConfigurationSupport implements HasDynamicProperties {

    public static final String STATUS_FLAG_CHANNEL = "StatusFlagChannel";
    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep";
    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String CALLING_AP_TITLE = "CallingAPTitle";

    private final PropertySpecService propertySpecService;

    public static final Duration DEFAULT_NOT_USED_AARQ_TIMEOUT = Duration.ofSeconds(0);
    public static final String CALLING_AP_TITLE_DEFAULT = "0000000000000000";

    public EM620ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.getAddressingMode(),
                this.forcedDelayPropertySpec(),
                this.retriesPropertySpec(),
                this.readCachePropertySpec(),
                this.timeoutPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.useCachedFrameCounter(),
                this.validateCachedFrameCounter(),
                this.frameCounterRecoveryRetries(),
                this.frameCounterRecoveryStep(),
                this.statusFlagChannelSpec(),
                this.apTitleSpec());
    }

    protected PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.RETRIES, DEFAULT_RETRIES, PropertyTranslationKeys.V2_ELSTER_RETRIES);
    }

    protected PropertySpec readCachePropertySpec() {
        return this.booleanSpec(DlmsProtocolProperties.READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.TIMEOUT, false, Duration.ofMillis(DEFAULT_TIMEOUT.intValue()),
                PropertyTranslationKeys.V2_ELSTER_TIMEOUT);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
    }

    protected PropertySpec bigDecimalSpec(String name, boolean required, TranslationKey translationKey, BigDecimal defaultValue,
                                          BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey,
                getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    private PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return this.bigDecimalSpec(SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE, PropertyTranslationKeys.V2_DLMS_SERVER_UPPER_MAC_ADDRESS);
    }

    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(SERVER_LOWER_MAC_ADDRESS, BigDecimal.valueOf(16), PropertyTranslationKeys.V2_DLMS_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec getAddressingMode() {
        return UPLPropertySpecFactory
                .specBuilder(ADDRESSING_MODE, true, com.energyict.protocolimpl.nls.PropertyTranslationKeys.EICT_ADDRESSING_MODE,
                        this.propertySpecService::bigDecimalSpec)
                .finish();
    }

    protected PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEZONE, false, PropertyTranslationKeys.V2_DLMS_TIMEZONE, this.propertySpecService::timeZoneSpec)
                .finish();
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(FORCED_DELAY, false, PropertyTranslationKeys.V2_DLMS_FORCED_DELAY, this.propertySpecService::durationSpec)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_DLMS_MAX_REC_PDU_SIZE);
    }

    private PropertySpec statusFlagChannelSpec() {
        return UPLPropertySpecFactory
                .specBuilder(STATUS_FLAG_CHANNEL, false, com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_STATUS_FLAG_CHANNEL, this.propertySpecService::integerSpec)
                .finish();
    }

    protected PropertySpec aarqTimeoutPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(AARQ_TIMEOUT_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_AARQ_TIMEOUT, this.propertySpecService::durationSpec)
                .finish();

    }

    protected PropertySpec aarqRetriesPropertySpec() {
        return this.bigDecimalSpec(AARQ_RETRIES_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_AARQ_RETRIES, BigDecimal.valueOf(2));
    }

    protected PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_DLMS_USE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    protected PropertySpec frameCounterRecoveryRetries() {
        return this.bigDecimalSpec(FRAME_COUNTER_RECOVERY_RETRIES, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100));
    }

    protected PropertySpec frameCounterRecoveryStep() {
        return this.bigDecimalSpec(FRAME_COUNTER_RECOVERY_STEP, false, PropertyTranslationKeys.V2_DLMS_FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE);
    }

    protected PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(VALIDATE_CACHED_FRAMECOUNTER, false, PropertyTranslationKeys.V2_DLMS_VALIDATE_CACHED_FRAMECOUNTER, getPropertySpecService()::booleanSpec)
                .finish();
    }

    protected PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, required, translationKey, getPropertySpecService()::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec booleanSpec(String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec apTitleSpec() {
        return UPLPropertySpecFactory.specBuilder(CALLING_AP_TITLE, false, com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_CALLING_AP_TITLE, getPropertySpecService()::stringSpec)
                .setDefaultValue(CALLING_AP_TITLE_DEFAULT)
                .finish();
    }
}
