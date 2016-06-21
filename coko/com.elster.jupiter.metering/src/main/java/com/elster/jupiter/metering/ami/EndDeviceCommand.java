package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Map;

public interface EndDeviceCommand {
    EndDeviceControlType getEndDeviceControlType();
    List<PropertySpec> getCommandArgumentSpecs();
    void setPropertyValue(PropertySpec propertySpec, Object value);
    EndDevice getEndDevice();
    List<Long> getDeviceMessageIds();
    String getName();
}
