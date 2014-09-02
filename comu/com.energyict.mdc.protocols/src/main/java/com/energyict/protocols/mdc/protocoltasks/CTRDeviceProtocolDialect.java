package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.Arrays;
import java.util.List;

/**
* Models a {@link DeviceProtocolDialect} for the CTR protocol
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


    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.CTR_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "CTR";
    }

    private PropertySpec timeoutPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(TIMEOUT_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec retriesPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(RETRIES_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec forcedDelayPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(FORCED_DELAY_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec addressPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(ADDRESS_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec sendEndOfSessionPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(SEND_END_OF_SESSION_PROPERTY_NAME, false, new BooleanFactory());
    }

    private PropertySpec maxAllowedInvalidProfileResponsesPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case TIMEOUT_PROPERTY_NAME:
                return this.timeoutPropertySpec();
            case RETRIES_PROPERTY_NAME:
                return this.retriesPropertySpec();
            case DELAY_AFTER_ERROR_PROPERTY_NAME:
                return this.delayAfterErrorPropertySpec();
            case FORCED_DELAY_PROPERTY_NAME:
                return this.forcedDelayPropertySpec();
            case ADDRESS_PROPERTY_NAME:
                return this.addressPropertySpec();
            case SEND_END_OF_SESSION_PROPERTY_NAME:
                return this.sendEndOfSessionPropertySpec();
            case MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME:
                return this.maxAllowedInvalidProfileResponsesPropertySpec();
            default:
                return null;
        }
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