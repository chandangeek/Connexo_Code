package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link DeviceMessageSpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (16:32)
 */
public interface DeviceMessageSpecFactory {

    public DeviceMessageSpec findDeviceMessageSpecFromPrimaryKey (String primaryKey);

}