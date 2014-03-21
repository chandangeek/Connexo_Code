package com.energyict.mdc.protocol.api.device.messages;

import com.energyict.mdc.common.ApplicationComponent;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link DeviceMessage}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-10 (10:06)
 */
public interface BaseDeviceMessageFactory {

    public DeviceMessage findDeviceMessage(int messageId);

}