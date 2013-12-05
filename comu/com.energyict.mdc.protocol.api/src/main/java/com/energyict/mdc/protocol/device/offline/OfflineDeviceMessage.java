package com.energyict.mdc.protocol.device.offline;

import com.energyict.mdc.common.Offline;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.device.messages.DeviceMessageStatus;

import java.util.Date;
import java.util.List;

/**
 * Represents an Offline version of a DeviceMessage
 * which should contain all necessary information needed to perform the actual DeviceMessage
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

    /**
     * @return the ID of the DeviceMessage
     */
    public int getDeviceMessageId();

    /**
     * The {@link DeviceMessageSpecPrimaryKey} to uniquely identify
     * the DeviceMessageSpec of this message.
     *
     * @return the DeviceMessageSpecPrimaryKey
     */
    public DeviceMessageSpecPrimaryKey getDeviceMessageSpecPrimaryKey();

    /**
     * The ID of the Device owning this DeviceMessage
     *
     * @return the id of the Device
     */
    public int getDeviceId();

    /**
     * The configured date of when this message <i>could</i> be executed
     *
     * @return the release date of this message
     */
    public Date getReleaseDate();

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
    public Date getCreationDate();

    /**
     * The list of {@link OfflineDeviceMessageAttribute DeviceMessageAttributes} which are owned
     * by this DeviceMessage. The information contained in these attributes should be sufficient
     * to perform the DeviceMessage.
     *
     * @return the list of OfflineDeviceMessageAttribute
     */
    public List<OfflineDeviceMessageAttribute> getDeviceMessageAttributes();
}
