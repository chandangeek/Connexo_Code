package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
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
    public static final String ADDRESS_PROPERTY_NAME = MeterProtocol.NODEID;

    public static final String SEND_END_OF_SESSION_PROPERTY_NAME = "SendEndOfSession";
    public static final String MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME = "MaxAllowedInvalidProfileResponses";

    public CTRDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.CTR_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "CTR";
    }

    private PropertySpec timeoutPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(TIMEOUT_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec retriesPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(RETRIES_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec forcedDelayPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(FORCED_DELAY_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec addressPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(ADDRESS_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec sendEndOfSessionPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(SEND_END_OF_SESSION_PROPERTY_NAME, false, new BooleanFactory());
    }

    private PropertySpec maxAllowedInvalidProfileResponsesPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
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