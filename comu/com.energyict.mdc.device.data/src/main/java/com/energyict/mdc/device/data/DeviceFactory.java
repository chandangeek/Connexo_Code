package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.protocol.api.device.Device;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link Device}s.
 * <p>
 * Todo: this interface can and must be removed as soon as JP-1122 is resolved
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:24)
 */
public interface DeviceFactory {

    public Device findDevice (long id);

}