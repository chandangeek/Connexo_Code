package com.energyict.mdc.protocol.api.device.messages;

import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.dynamic.ReadOnlyDynamicAttributeOwner;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;

import java.util.Date;
import java.util.List;

/**
 * Models a message that is sent to a {@link Device device}.
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
public interface DeviceMessage<D extends Device> extends IdBusinessObject, ReadOnlyDynamicAttributeOwner, CanGoOffline<OfflineDeviceMessage> {

    /**
     * Gets the {@link DeviceMessageSpec specification} of this DeviceMessage.
     *
     * @return The specification
     */
    public DeviceMessageSpec getSpecification();

    /**
     * Gets the {@link DeviceMessageAttribute}s of this DeviceMessage.
     * Note that the number of attributes returned will be at least
     * equal to the number of required attributes
     * defined by the specification.
     *
     * @return The attributes
     */
    public List<DeviceMessageAttribute> getAttributes();

    /**
     * Gets the {@link Device device} to which this DeviceMessage
     * will be sent or has been sent, depending on its lifecycle.
     *
     * @return The device
     */
    public D getDevice();

    /**
     * Gets the {@link DeviceMessageStatus} of this DeviceMessage.
     *
     * @return The DeviceMessageStatus
     */
    public DeviceMessageStatus getStatus();

    /**
     * Provides information regarding the state of the message,
     * coming from the DeviceProtocol.
     *
     * @return the protocolInfo
     */
    public String getProtocolInfo();

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
    public Date getReleaseDate();

    /**
     * Gets the Date on which the last modification to
     * this DeviceMessage was effected.
     *
     * @return The last modification date
     */
    public Date getModificationDate();

    /**
     * Provides the date when this object was created
     *
     * @return the creationDate of this DeviceMessage
     */
    public Date getCreationDate();

    /**
     * Returns the receiver's tracking id
     *
     * @return the receiver's tracking id
     */
    public String getTrackingId();

}