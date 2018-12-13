package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
* Models a {@link DeviceProtocolDialect} for the CTR protocol.
*
* @author sva
* @since 16/10/12 (113:25)
*/
public class CTRDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    // Optional properties
    public static final String TIMEOUT_PROPERTY_NAME = "Timeout";
    public static final String RETRIES_PROPERTY_NAME = "Retries";
    public static final String DELAY_AFTER_ERROR_PROPERTY_NAME = "DelayAfterError";
    public static final String FORCED_DELAY_PROPERTY_NAME = "ForcedDelay";
    public static final String ADDRESS_PROPERTY_NAME = com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName();

    public static final String SEND_END_OF_SESSION_PROPERTY_NAME = "SendEndOfSession";
    public static final String MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME = "MaxAllowedInvalidProfileResponses";

    public CTRDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public CTRDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.CTR_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.CTR_DEVICE_PROTOCOL_DIALECT_NAME).format();
    }

    private PropertySpec timeoutPropertySpec() {
        return this.bigDecimalSpec(TIMEOUT_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_TIMEOUT);
    }

    private PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(RETRIES_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_RETRIES);
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return this.bigDecimalSpec(DELAY_AFTER_ERROR_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_DELAY_AFTER_ERROR);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return this.bigDecimalSpec(FORCED_DELAY_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_FORCED_DELAY);
    }

    private PropertySpec addressPropertySpec() {
        return this.bigDecimalSpec(ADDRESS_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_ADDRESS);
    }

    private PropertySpec sendEndOfSessionPropertySpec() {
        return this.booleanSpec(SEND_END_OF_SESSION_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_SEND_END_OF_SESSION);
    }

    private PropertySpec maxAllowedInvalidProfileResponsesPropertySpec() {
        return this.bigDecimalSpec(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME, PropertyTranslationKeys.V2_TASKS_MAX_ALLOWED_INVALID_PROFILE_RESPONSES);
    }

    private PropertySpec bigDecimalSpec (String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                    .specBuilder(name, false, translationKey, getPropertySpecService()::bigDecimalSpec)
                    .finish();
    }

    private PropertySpec booleanSpec (String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                    .specBuilder(name, false, translationKey, getPropertySpecService()::booleanSpec)
                    .finish();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.delayAfterErrorPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.addressPropertySpec(),
                this.sendEndOfSessionPropertySpec(),
                this.maxAllowedInvalidProfileResponsesPropertySpec());
    }

}