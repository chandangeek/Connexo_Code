package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.OptionalPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * Models a GPRS {@link DeviceProtocolDialect} for the ACE4000 protocol
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class ACE4000DeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    // Optional properties
    public static final String TIMEOUT_PROPERTY_NAME = "Timeout";
    public static final String RETRIES_PROPERTY_NAME = "Retries";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ACE4000_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "ACE 4000";
    }

    private PropertySpec timeoutPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(TIMEOUT_PROPERTY_NAME);
    }

    private PropertySpec retriesPropertySpec() {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(RETRIES_PROPERTY_NAME);
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        return Arrays.asList(this.timeoutPropertySpec(), this.retriesPropertySpec());
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

}