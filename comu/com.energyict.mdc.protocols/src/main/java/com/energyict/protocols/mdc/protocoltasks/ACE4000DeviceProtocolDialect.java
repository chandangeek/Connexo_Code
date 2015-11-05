package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
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

    public ACE4000DeviceProtocolDialect(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ACE4000_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "ACE 4000";
    }

    private PropertySpec timeoutPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(TIMEOUT_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    private PropertySpec retriesPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(RETRIES_PROPERTY_NAME, false, new BigDecimalFactory());
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        return Arrays.asList(this.timeoutPropertySpec(), this.retriesPropertySpec());
    }

}