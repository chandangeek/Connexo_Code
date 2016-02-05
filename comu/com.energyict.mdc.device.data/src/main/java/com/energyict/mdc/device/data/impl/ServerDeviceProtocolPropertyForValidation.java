package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.DeviceProtocolProperty;

/**
 * Server-side interface for validation purposes
 *
 * @author sva
 * @since 4/02/2016 - 9:03
 */
public interface ServerDeviceProtocolPropertyForValidation extends DeviceProtocolProperty {

    PropertySpec getPropertySpec();

}
