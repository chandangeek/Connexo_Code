package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.common.Offline;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Represents an Offline version of a DeviceMessage
 * which should contain all necessary information needed to perform the actual DeviceMessage.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/02/13
 * Time: 16:27
 */
public interface OfflineDeviceMessage extends Offline {

    /**
     * Gets the {@link DeviceMessageSpec} of this offline version
     * of a DeviceMessage.
     *
     * @return The DeviceMessageSpec
     */
    public DeviceMessageSpec getSpecification ();

    public MessageIdentifier getIdentifier();

    /**
     * @return the ID of the DeviceMessage
     */
    public DeviceMessageId getDeviceMessageId();

    /**
     * The ID of the Device owning this DeviceMessage
     *
     * @return the id of the Device
     */
    public long getDeviceId();

    /**
     * The configured date of when this message <i>could</i> be executed
     *
     * @return the release date of this message
     */
    public Instant getReleaseDate();

    /**
     * An ID which can be used to keep track of this message
     *
     * @return the trackingId
     */
    public String getTrackingId();

    /**
     * The protocol info returned from the device protocol.

     * @return the protocolInfo
     */
    public String getProtocolInfo();

    /**
     * The current {@link DeviceMessageStatus status} of this DeviceMessage
     *
     * @return the DeviceMessageStatus
     */
    public DeviceMessageStatus getDeviceMessageStatus();

    /**
     * The date on which this DeviceMessage was created
     *
     * @return the creationDate of this message
     */
    public Instant getCreationDate();

    /**
     * The list of {@link OfflineDeviceMessageAttribute}s which are owned by this DeviceMessage.
     * The information contained in these attributes should be sufficient to execute the DeviceMessage.
     *
     * @return the list of OfflineDeviceMessageAttribute
     */
    public List<OfflineDeviceMessageAttribute> getDeviceMessageAttributes();

    /**
     * @return the serialNumber of the device
     */
    public String getDeviceSerialNumber();

    /**
     * @return the identifier of the device which owns this devicemessage
     */
    public DeviceIdentifier getDeviceIdentifier();
}
