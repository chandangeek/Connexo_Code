package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.protocol.api.device.Device;

import java.util.List;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link LoadProfile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-08 (16:34)
 */
public interface LoadProfileFactory {

    public List<LoadProfile> findLoadProfilesByDevice(Device device);

}