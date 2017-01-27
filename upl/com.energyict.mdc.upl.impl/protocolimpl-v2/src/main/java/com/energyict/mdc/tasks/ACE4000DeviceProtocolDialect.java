package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Models a GPRS {@link com.energyict.mdc.upl.DeviceProtocolDialect} for the ACE4000 protocol.
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class ACE4000DeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    // Optional properties
    private static final String TIMEOUT_PROPERTY_NAME = DeviceProtocol.Property.TIMEOUT.getName();
    private static final String RETRIES_PROPERTY_NAME = DeviceProtocol.Property.RETRIES.getName();

    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal("30000");
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal("3");

    public ACE4000DeviceProtocolDialect(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ACE4000_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "ACE 4000";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec());
    }

    private PropertySpec timeoutPropertySpec() {
        return this.bigDecimalSpec(TIMEOUT_PROPERTY_NAME, false, DEFAULT_TIMEOUT);
    }

    private PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(RETRIES_PROPERTY_NAME, false, DEFAULT_RETRIES);
    }

    protected PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

}