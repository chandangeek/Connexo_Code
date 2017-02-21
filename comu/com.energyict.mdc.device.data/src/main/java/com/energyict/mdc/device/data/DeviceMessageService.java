/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface DeviceMessageService {

    Optional<DeviceMessage> findDeviceMessageById(long id);

    Optional<DeviceMessage> findAndLockDeviceMessageByIdAndVersion(long id, long version);

    /**
     * Checks if a DeviceMessage will be picked up by a planned ComTask.
     *
     * @param device The device for which to check the planned ComTasks
     * @param deviceMessage The DeviceMessage that needs to be checked
     * @return true if it will be picked up, false if it will not be picked up by a planned ComTask
     */
    boolean willDeviceMessageBePickedUpByPlannedComTask(Device device, DeviceMessage deviceMessage);

    /**
     * Checks if a given DeviceMessage is part of a ComTask on the DeviceConfiguration.
     *
     * @param device The device for which to check the ComTasks
     * @param deviceMessage The DeviceMessage that needs to be checked
     * @return true if it will be picked up, false if it will not be picked up by a ComTask
     */
    boolean willDeviceMessageBePickedUpByComTask(Device device, DeviceMessage deviceMessage);

    /**
     * Gets the preferred ComTask for a DeviceMessage
     *
     * @param device The device for wich to check the preferred ComTask
     * @param deviceMessage The deviceMessage that needs to be checked<
     * @return A ComTask if one is found, else null
     */
    ComTask getPreferredComTask(Device device, DeviceMessage<?> deviceMessage);

    /**
     * Checks if the logged in user can create or update a given message
     *
     * @param deviceConfiguration The deviceconfiguration of the device the user wants to administrate messages for
     * @param deviceMessageId The id of the devicemessage
     * @return
     */
    boolean canUserAdministrateDeviceMessage(DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId);
}