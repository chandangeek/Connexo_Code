package com.energyict.protocolimplv2.dlms.landisAndGyr.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.CONNECTION_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_CONNECTION_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.HDLC_STR;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.WRAPPER_STR;

/**
 * A collection of general DLMS properties that are relevant for the EDP DLMS meters.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 */
public class ZMYDlmsConfigurationSupport implements HasDynamicProperties {

    private final PropertySpecService propertySpecService;

    public ZMYDlmsConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.getConnectionMode(),
                this.getAddressingMode(),
                this.forcedDelayPropertySpec(),
                this.retriesPropertySpec(),
                this.readCachePropertySpec(),
                this.timeoutPropertySpec());
    }

    protected PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.RETRIES, DEFAULT_RETRIES, PropertyTranslationKeys.V2_ELSTER_RETRIES);
    }

    protected PropertySpec readCachePropertySpec() {
        return this.booleanSpec(DlmsProtocolProperties.READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.TIMEOUT, Duration.ofMillis(DEFAULT_TIMEOUT.intValue()),
                PropertyTranslationKeys.V2_ELSTER_TIMEOUT);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException { }

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

    protected PropertySpec getConnectionMode() {
        return UPLPropertySpecFactory
                .specBuilder(CONNECTION_MODE, false, PropertyTranslationKeys.V2_DLMS_CONNECTION_MODE,
                        this.propertySpecService::stringSpec)
                .setDefaultValue(DEFAULT_CONNECTION_MODE)
                .addValues(
                        WRAPPER_STR,
                        HDLC_STR)
                .markExhaustive()
                .finish();
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

    protected PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec(String name, Duration defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, getPropertySpecService()::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec booleanSpec(String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }
}