package com.energyict.mdc.tasks;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Models a GPRS {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for the ACE4000 protocol
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class ACE4000DeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    // Optional properties
    public static final String TIMEOUT_PROPERTY_NAME = "Timeout";
    public static final String RETRIES_PROPERTY_NAME = "Retries";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ACE4000_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    private PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case TIMEOUT_PROPERTY_NAME:
                return this.timeoutPropertySpec();
            case RETRIES_PROPERTY_NAME:
                return this.retriesPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<PropertySpec>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(this.timeoutPropertySpec(),
                this.retriesPropertySpec());
    }
}