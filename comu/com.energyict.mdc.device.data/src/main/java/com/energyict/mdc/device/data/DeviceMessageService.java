/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface DeviceMessageService {
    String BULK_DEVICE_MESSAGE_QUEUE_DESTINATION = "BulkDevMsgQD";
    String BULK_DEVICE_MESSAGE_QUEUE_SUBSCRIBER = "BulkDevMsgQS";
    String BULK_DEVICE_MESSAGE_QUEUE_DISPLAYNAME = "Handle creation of device messages on a device group in bulk";
    String DEVICE_MESSAGE_QUEUE_DESTINATION = "DevMsgQD";
    String DEVICE_MESSAGE_QUEUE_SUBSCRIBER = "DevMsgQS";
    String DEVICE_MESSAGE_QUEUE_DISPLAYNAME = "Create a device message for a single device";

    Optional<DeviceMessage> findDeviceMessageById(long id);

    Optional<DeviceMessage> findDeviceMessageByIdentifier(MessageIdentifier identifier);

    Optional<DeviceMessage> findAndLockDeviceMessageByIdAndVersion(long id, long version);

    Optional<DeviceMessage> findAndLockDeviceMessageById(long id);

    /**
     * Checks if a DeviceMessage will be picked up by a planned ComTask.
     *
     * @param device        The device for which to check the planned ComTasks
     * @param deviceMessage The DeviceMessage that needs to be checked
     * @return true if it will be picked up, false if it will not be picked up by a planned ComTask
     */
    boolean willDeviceMessageBePickedUpByPlannedComTask(Device device, DeviceMessage deviceMessage);

    /**
     * Checks if a given DeviceMessage is part of a ComTask on the DeviceConfiguration.
     *
     * @param device        The device for which to check the ComTasks
     * @param deviceMessage The DeviceMessage that needs to be checked
     * @return true if it will be picked up, false if it will not be picked up by a ComTask
     */
    boolean willDeviceMessageBePickedUpByComTask(Device device, DeviceMessage deviceMessage);

    /**
     * Gets the preferred ComTask for a DeviceMessage
     *
     * @param device        The device for wich to check the preferred ComTask
     * @param deviceMessage The deviceMessage that needs to be checked<
     * @return A ComTask if one is found, else null
     */
    ComTask getPreferredComTask(Device device, DeviceMessage deviceMessage);

    /**
     * Checks if the logged in user can create or update a given message
     *
     * @param deviceConfiguration The deviceconfiguration of the device the user wants to administrate messages for
     * @param deviceMessageId     The id of the devicemessage
     * @return
     */
    boolean canUserAdministrateDeviceMessage(DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId);

    /**
     * Perform a search for device messages by respecting filter parameters
     *
     * @return Found DeviceMessages
     */
    Finder<DeviceMessage> findDeviceMessagesByFilter(DeviceMessageQueryFilter deviceMessageQueryFilter);

    List<DeviceMessage> findDeviceFirmwareMessages(Device device);

    List<DeviceMessageId> findKeyRenewalMessages();

    List<DeviceMessage> findAndLockPendingMessagesForDevices(Collection<Device> devices);
}
