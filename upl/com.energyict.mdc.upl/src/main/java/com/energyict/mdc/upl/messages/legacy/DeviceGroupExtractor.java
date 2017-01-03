package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.DeviceGroup;

import java.util.List;

/**
 * Extracts information that pertains to {@link DeviceGroup}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-02 (15:46)
 */
public interface DeviceGroupExtractor {
    /**
     * Extracts the {@link Device members} from the {@link DeviceGroup}.
     *
     * @param group The DeviceGroup
     * @return The serial number
     */
    List<Device> members(DeviceGroup group);
}