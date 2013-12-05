package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.protocol.DeviceProtocolDialect;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.OptionalPropertySpecFactory;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

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
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(RETRIES_PROPERTY_NAME);
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(FORCED_DELAY_PROPERTY_NAME);
    }

    private PropertySpec addressPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec sendEndOfSessionPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(SEND_END_OF_SESSION_PROPERTY_NAME);
    }

    private PropertySpec maxAllowedInvalidProfileResponsesPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME);
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