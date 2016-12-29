package com.energyict.protocolimplv2.eict.webrtuz3.properties;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.google.common.base.Supplier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

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
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
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
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, BigDecimal.ZERO);
    }

    protected PropertySpec timeZonePropertySpec() {
        return this.timeZoneSpec(TIMEZONE);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return this.booleanSpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return this.booleanSpec(BULK_REQUEST, true);
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec timeZoneSpec(String name) {
        return this.spec(name, this.propertySpecService::timeZoneSpec);
    }

    private PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec booleanSpec(String name, boolean defaultValue) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec(String name, Duration defaultValue) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    @Override
    public void setProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.typedProperties = TypedProperties.copyOf(properties);
    }

}