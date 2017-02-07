/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.protocol.api.DeviceFunction;

public class DeviceFunctionAdapter extends MapBasedXmlAdapter<DeviceFunction> {

    public DeviceFunctionAdapter() {
        register("", DeviceFunction.NONE);
        register("No function", DeviceFunction.NONE);
        register("Gateway", DeviceFunction.GATEWAY);
        register("Concentrator", DeviceFunction.CONCENTRATOR);
        register("In home display", DeviceFunction.INHOMEDISPLAY);
        register("Meter", DeviceFunction.METER);
        register("Repeater", DeviceFunction.REPEATER);
    }
}
