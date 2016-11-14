package com.energyict.mdc.upl.messages;

import java.util.Date;

/**
 * Models a message that is sent to a device.
 * The lifecycle of a DeviceMessage is as following:
 * <ol>
 * <li>{@link DeviceMessageStatus#WAITING}</li>
 * <li>{@link DeviceMessageStatus#PENDING} or {@link DeviceMessageStatus#CANCELED}</li>
 * <li>{@link DeviceMessageStatus#SENT}</li>
 * <li>{@link DeviceMessageStatus#CONFIRMED}, {@link DeviceMessageStatus#FAILED} or {@link DeviceMessageStatus#INDOUBT}</li>
 * </ol>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (15:13)
 */
public interface DeviceMessage {

    /**
     * Gets the {@link DeviceMessageSpec specification} of this DeviceMessage.
     *
     * @return The specification
     */
    DeviceMessageSpec getSpecification();

    /**
     * Gets the {@link DeviceMessageStatus} of this DeviceMessage.
     *
     * @return The DeviceMessageStatus
     */
    DeviceMessageStatus getStatus();

    /**
     * Provides the date when this object was created
     *
     * @return the creationDate of this DeviceMessage
     */
    Date getCreationDate();

    /**
     * Gets the Date on which the last modification to
     * this DeviceMessage was effected.
     *
     * @return The last modification date
     */
    Date getModificationDate();

    /**
     * Gets the Date on which this DeviceMessage becomes eligible
     * for sending to the device.
     * Remember that the actual sending of the DeviceMessage will depend
     * on how the communication of the device is configured.
     * All DeviceMessage that eligible for sending to a device
     * will actually be sent to the device when a task to send
     * DeviceMessage is scheduled and executed against the device.
     *
     * @return The release Date
     */
    Date getReleaseDate();

    /**
     * Returns the receiver's tracking id
     *
     * @return the receiver's tracking id
     */
    String getTrackingId();

}