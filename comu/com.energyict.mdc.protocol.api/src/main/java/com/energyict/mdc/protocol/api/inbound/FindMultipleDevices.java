package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import java.util.List;

/**
 * Provides additional identification services for {@link com.energyict.mdc.protocol.api.device.BaseDevice}s
 * for identifier types that are not guaranteed to be unique
 * and may therefore return multiple Devices.
 *
 * Copyrights EnergyICT
 * Date: 9/25/13
 * Time: 11:05 AM
 */
public interface FindMultipleDevices<T extends BaseDevice< ? extends BaseChannel, ? extends BaseLoadProfile<?  extends BaseChannel>, ? extends  BaseRegister>> extends DeviceIdentifier {

    /**
     * <b>ALL</b> Devices which satisfy the criteria of the DeviceIdentifier.
     * This should only be used in exceptional situations when we know
     * duplicates exist.
     *
     * @return <b>ALL</b> Devices which satisfy the criteria of the DeviceIdentifier
     */
    public List<T> getAllDevices();

}