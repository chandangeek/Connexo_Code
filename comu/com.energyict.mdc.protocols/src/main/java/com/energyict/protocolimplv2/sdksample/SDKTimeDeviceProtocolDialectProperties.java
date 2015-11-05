package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of properties related to the TimeSetting.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:51
 */
public class SDKTimeDeviceProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String clockOffsetToWritePropertyName = "ClockOffsetWhenReading";

    public static final String clockOffsetToReadPropertyName = "ClockOffsetWhenWriting";

    public SDKTimeDeviceProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_TIME_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect for time testing";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        requiredProperties.add(getClockOffsetToReadPropertySpec());
        requiredProperties.add(getClockOffsetToWritePropertySpec());
        return requiredProperties;
    }

    private PropertySpec getClockOffsetToWritePropertySpec() {
        return this.getPropertySpecService().timeDurationPropertySpec(clockOffsetToWritePropertyName, true, null);
    }

    private PropertySpec getClockOffsetToReadPropertySpec() {
        return this.getPropertySpecService().timeDurationPropertySpec(clockOffsetToReadPropertyName, true, null);
    }

}
