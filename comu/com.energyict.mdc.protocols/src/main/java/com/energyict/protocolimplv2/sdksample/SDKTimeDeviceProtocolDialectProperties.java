package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.elster.jupiter.time.TimeDuration;
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

    private final PropertySpecService propertySpecService;

    public SDKTimeDeviceProtocolDialectProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case clockOffsetToWritePropertyName:
                return getClockOffsetToWritePropertySpec();
            case clockOffsetToReadPropertyName:
                return getClockOffsetToReadPropertySpec();
        }
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        requiredProperties.add(getClockOffsetToReadPropertySpec());
        requiredProperties.add(getClockOffsetToWritePropertySpec());
        return requiredProperties;
    }

    private PropertySpec<TimeDuration> getClockOffsetToWritePropertySpec() {
        return this.propertySpecService.timeDurationPropertySpec(clockOffsetToWritePropertyName, true, null);
    }

    private PropertySpec<TimeDuration> getClockOffsetToReadPropertySpec() {
        return this.propertySpecService.timeDurationPropertySpec(clockOffsetToReadPropertyName, true, null);
    }

}
