package com.energyict.mdc.device.data;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import aQute.bnd.annotation.ProviderType;


/**
 * Provides helper methods that relate to a {@link Device}.
 */
@ProviderType
public interface DeviceHelper {

    /**
     * Checks if a DeviceMessage  will be picked up by a planned ComTask
     * @param device The device for which to check the planned ComTasks
     * @param deviceMessage The DeviceMessage that needs to be checked
     * @return true if it will be picked up, false if it will not be picked up by a planned ComTask
     */
    boolean willDeviceMessageBePickedUpByPlannedComTask(Device device, DeviceMessage deviceMessage);

    /**
     * Checks if a give DeviceMessage is part of a ComTask on the DeviceCOnfiguration
     * @param device The device for which to check the ComTasks
     * @param deviceMessage The DeviceMessageId that needs to be checked
     * @return true if it will be picked up, false if it will not be picked up by a ComTask
     */
    boolean willDeviceMessageBePickedUpByComTask(Device device, DeviceMessage deviceMessage);
}
