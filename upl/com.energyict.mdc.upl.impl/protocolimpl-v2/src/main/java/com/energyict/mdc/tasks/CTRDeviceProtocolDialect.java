package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
* Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for the CTR protocol.
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

    private final PropertySpecService propertySpecService;

    public CTRDeviceProtocolDialect(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.CTR_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "CTR";
    }

    private PropertySpec timeoutPropertySpec() {
        return this.bigDecimalSpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(RETRIES_PROPERTY_NAME);
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return this.bigDecimalSpec(DELAY_AFTER_ERROR_PROPERTY_NAME);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return this.bigDecimalSpec(FORCED_DELAY_PROPERTY_NAME);
    }

    private PropertySpec addressPropertySpec() {
        return this.bigDecimalSpec(ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec sendEndOfSessionPropertySpec() {
        return this.booleanSpec(SEND_END_OF_SESSION_PROPERTY_NAME);
    }

    private PropertySpec maxAllowedInvalidProfileResponsesPropertySpec() {
        return this.bigDecimalSpec(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME);
    }

    private PropertySpec bigDecimalSpec (String name) {
        return UPLPropertySpecFactory
                    .specBuilder(name, false, this.propertySpecService::bigDecimalSpec)
                    .finish();
    }

    private PropertySpec booleanSpec (String name) {
        return UPLPropertySpecFactory
                    .specBuilder(name, false, this.propertySpecService::booleanSpec)
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