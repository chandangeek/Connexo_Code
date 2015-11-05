package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of properties related to the TimeSetting.
 * <p>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:51
 */
public class SDKTimeDeviceProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String clockOffsetToWritePropertyName = "ClockOffsetWhenReading";
    public static final String clockOffsetToReadPropertyName = "ClockOffsetWhenWriting";

    private final PropertySpecService propertySpecService;

    public SDKTimeDeviceProtocolDialectProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKTimeDialect";
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
        return this.propertySpecService.basicPropertySpec(clockOffsetToWritePropertyName, true, new TimeDurationValueFactory());
    }

    private PropertySpec getClockOffsetToReadPropertySpec() {
        return this.propertySpecService.basicPropertySpec(clockOffsetToReadPropertyName, true, new TimeDurationValueFactory());
    }

}