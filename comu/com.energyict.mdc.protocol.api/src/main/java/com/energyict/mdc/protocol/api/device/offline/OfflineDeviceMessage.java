/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.common.Offline;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.List;

public interface OfflineDeviceMessage extends Offline {

    /**
     * Gets the {@link DeviceMessageSpec} of this offline version
     * of a DeviceMessage.
     *
     * @return The DeviceMessageSpec
     */
    DeviceMessageSpec getSpecification();

    MessageIdentifier getIdentifier();

    /**
     * @return the ID of the DeviceMessage
     */
    DeviceMessageId getDeviceMessageId();

    /**
     * The ID of the Device owning this DeviceMessage.
     *
     * @return the id of the Device
     */
    long getDeviceId();

    /**
     * The configured date of when this message <i>could</i> be executed.
     *
     * @return the release date of this message
     */
    Instant getReleaseDate();

    /**
     * The DeviceProtocol of the device that has this message.
     */
    DeviceProtocol getDeviceProtocol();

    /**
     * An ID which can be used to keep track of this message
     *
     * @return the trackingId
     */
    String getTrackingId();

    /**
     * The protocol info returned from the device protocol.

     * @return the protocolInfo
     */
    String getProtocolInfo();

    /**
     * The current {@link DeviceMessageStatus status} of this DeviceMessage.
     *
     * @return the DeviceMessageStatus
     */
    DeviceMessageStatus getDeviceMessageStatus();

    /**
     * The date on which this DeviceMessage was created.
     *
     * @return the creationDate of this message
     */
    Instant getCreationDate();

    /**
     * The list of {@link OfflineDeviceMessageAttribute}s which are owned by this DeviceMessage.
     * The information contained in these attributes should be sufficient to execute the DeviceMessage.
     *
     * @return the list of OfflineDeviceMessageAttribute
     */
    List<OfflineDeviceMessageAttribute> getDeviceMessageAttributes();

    /**
     * @return the serialNumber of the device
     */
    String getDeviceSerialNumber();

    /**
     * @return the identifier of the device which owns this devicemessage
     */
    DeviceIdentifier getDeviceIdentifier();

}