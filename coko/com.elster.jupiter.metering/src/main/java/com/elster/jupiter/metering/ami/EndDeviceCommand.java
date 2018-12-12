/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

public interface EndDeviceCommand {

    EndDeviceControlType getEndDeviceControlType();

    List<PropertySpec> getCommandArgumentSpecs();

    void setPropertyValue(PropertySpec propertySpec, Object value);

    EndDevice getEndDevice();
}