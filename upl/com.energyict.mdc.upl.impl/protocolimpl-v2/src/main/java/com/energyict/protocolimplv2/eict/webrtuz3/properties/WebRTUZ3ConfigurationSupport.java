package com.energyict.protocolimplv2.eict.webrtuz3.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

public class WebRTUZ3ConfigurationSupport implements HasDynamicProperties {

    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;

    private final PropertySpecService propertySpecService;
    private TypedProperties typedProperties;

    public WebRTUZ3ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.bulkRequestPropertySpec(),
                this.timeZonePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.validateInvokeIdPropertySpec());
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE, PropertyTranslationKeys.V2_EICT_SERVER_UPPER_MAC_ADDRESS);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, BigDecimal.ZERO, PropertyTranslationKeys.V2_EICT_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec timeZonePropertySpec() {
        return this.timeZoneSpec(TIMEZONE, PropertyTranslationKeys.V2_EICT_TIMEZONE);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return this.booleanSpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID, PropertyTranslationKeys.V2_EICT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(FORCED_DELAY, DEFAULT_FORCED_DELAY, PropertyTranslationKeys.V2_EICT_FORCED_DELAY);
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_EICT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return this.booleanSpec(BULK_REQUEST, true, PropertyTranslationKeys.V2_EICT_BULK_REQUEST);
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec timeZoneSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::timeZoneSpec);
    }

    private PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec booleanSpec(String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec(String name, Duration defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.typedProperties = TypedProperties.copyOf(properties);
    }

}