package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

public interface EndDeviceCommand {
    EndDeviceControlType getEndDeviceControlType();
    List<PropertySpec> getCommandArgumentSpecs();
    void setPropertyValue(PropertySpec propertySpec, Object value);
    public void removeProperty(PropertySpec propertySpec);
}
