package com.energyict.mdc.tasks;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for the CTR protocol
*
* @author: sva
* @since: 16/10/12 (113:25)
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
        return PropertySpecFactory.bigDecimalPropertySpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES_PROPERTY_NAME);
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DELAY_AFTER_ERROR_PROPERTY_NAME);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(FORCED_DELAY_PROPERTY_NAME);
    }

    private PropertySpec addressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec sendEndOfSessionPropertySpec() {
        return PropertySpecFactory.booleanPropertySpec(SEND_END_OF_SESSION_PROPERTY_NAME);
    }

    private PropertySpec maxAllowedInvalidProfileResponsesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (TIMEOUT_PROPERTY_NAME.equals(name)) {
            return this.timeoutPropertySpec();
        } else if (RETRIES_PROPERTY_NAME.equals(name)) {
            return this.retriesPropertySpec();
        } else if (DELAY_AFTER_ERROR_PROPERTY_NAME.equals(name)) {
            return this.delayAfterErrorPropertySpec();
        } else if (FORCED_DELAY_PROPERTY_NAME.equals(name)) {
            return this.forcedDelayPropertySpec();
        } else if (ADDRESS_PROPERTY_NAME.equals(name)) {
            return this.addressPropertySpec();
        } else if (SEND_END_OF_SESSION_PROPERTY_NAME.equals(name)) {
            return this.sendEndOfSessionPropertySpec();
        } else if (MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME.equals(name)) {
            return this.maxAllowedInvalidProfileResponsesPropertySpec();
        } else {
            return null;
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.delayAfterErrorPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.addressPropertySpec(),
                this.sendEndOfSessionPropertySpec(),
                this.maxAllowedInvalidProfileResponsesPropertySpec());
    }
}