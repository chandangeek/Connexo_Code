package com.energyict.mdc.protocol.api.device.messages;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.Device;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a message that is sent to a {@link com.energyict.mdc.upl.meterdata.Device device}.
 * The lifecycle of a DeviceMessage is as following:
 * <ol>
 * <li>{@link DeviceMessageStatus#WAITING}</li>
 * <li>{@link DeviceMessageStatus#PENDING} or {@link DeviceMessageStatus#CANCELED}</li>
 * <li>{@link DeviceMessageStatus#SENT}</li>
 * <li>{@link DeviceMessageStatus#CONFIRMED}, {@link DeviceMessageStatus#FAILED} or {@link DeviceMessageStatus#INDOUBT}</li>
 * </ol>
 * The sending of a DeviceMessage can be postponed by setting a release date.
 * Sending message is done by executing a ComTask (aka ComTaskExecution) that is configured
 * to send messages (see com.energyict.mdc.protocol.tasks.MessagesTask).
 * Therefore, messages that are created against a device are not necessarily sent
 * immediately when their release date expires.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:13)
 */
@ConsumerType
public interface DeviceMessage extends HasId, com.energyict.mdc.upl.messages.DeviceMessage {

    void save();

    void delete();

    /**
     * Gets the {@link DeviceMessageSpec specification} of this DeviceMessage.
     *
     * @return The specification
     */
    DeviceMessageSpec getSpecification();

    /**
     * @return the DeviceMessageId of the spec of this DeviceMessage
     */
    DeviceMessageId getDeviceMessageId();

    /**
     * Gets the {@link Device device} to which this DeviceMessage
     * will be sent or has been sent, depending on its lifecycle.
     *
     * @return The device
     */
    Device getDevice();

    /**
     * Gets the {@link DeviceMessageStatus} of this DeviceMessage.
     *
     * @return The DeviceMessageStatus
     */
    DeviceMessageStatus getStatus();

    /**
     * Provides information regarding the state of the message,
     * coming from the DeviceProtocol.
     *
     * @return the protocolInfo
     */
    String getProtocolInfo();

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
    Instant getReleaseDate();

    /**
     * Updates the release date of this device message. Will only be allowed for messages in state WAITING. Will be persisted by save()
     */
    void setReleaseDate(Instant releaseDate);

    /**
     * Provides the date when this object was created
     *
     * @return the creationDate of this DeviceMessage
     */
    Instant getCreationDate();

    /**
     * Returns the receiver's tracking id
     *
     * @return the receiver's tracking id
     */
    String getTrackingId();

    /**
     * Returns the sender's tracking id
     *
     * @return the sender's tracking id
     */
    TrackingCategory getTrackingCategory();

    /**
     * This is the date & time when a message was actually transmitted to the device. Will be empty if the message was not sent yet.
     *
     * @return The sent-date or empty if unsent
     */
    Optional<Instant> getSentDate();

    /**
     * Sets the date & time when the message was actually transmitted to the device.
     *
     * @param sentDate the sent-date to set
     */
    void setSentDate(Instant sentDate);

    /**
     * User who created the command
     *
     * @return the name of the User who created the command
     */
    String getUser();

    /**
     * Cancels/revokes this DeviceMessage
     */
    void revoke();

    /**
     * Sets information regarding this message which was provided by the DeviceProtocol during
     * the execution of the message.
     *
     * @param protocolInformation the information from the Protocol
     */
    void setProtocolInformation(String protocolInformation);

    /**
     * Updates this messages to the new DeviceMessageStatus
     *
     * @param newDeviceMessageStatus the new DeviceMessageStatus
     */
    void updateDeviceMessageStatus(DeviceMessageStatus newDeviceMessageStatus);

    Instant getModTime();

    long getVersion();
}