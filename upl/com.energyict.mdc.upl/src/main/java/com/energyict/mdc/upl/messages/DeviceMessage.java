package com.energyict.mdc.upl.messages;

import java.util.List;

/**
 * Models a message that is sent to a device.
 * This interface only contains the strictly necessary methods for the protocol implementations.
 */
public interface DeviceMessage {

    /**
     * Gets the globally unique identifier of the related DeviceMessageSpec.
     */
    long getMessageId();

    /**
     * Gets the {@link DeviceMessageAttribute}s of this DeviceMessage.
     * Note that the number of attributes returned will be at least
     * equal to the number of required attributes
     * defined by the specification.
     *
     * @return The attributes
     */
    List<? extends DeviceMessageAttribute> getAttributes();

}