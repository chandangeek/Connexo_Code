package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

/**
 * A collectedMessages identifies a DeviceMessage executed by the device and the
 * result from the execution.
 *
 * @author gna
 * @since 5/04/12 - 11:19
 */
public interface CollectedMessage extends CollectedData {

    /**
     * Should provide an identifier to uniquely identify the requested DeviceMessage.
     *
     * @return the {@link MessageIdentifier messageIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    MessageIdentifier getMessageIdentifier();

    /**
     * Provides the {@link DeviceMessageStatus status} the
     * DeviceMessage should have after
     * the message has been forwarded to the device.
     *
     * @return the new DeviceMessageStatus
     */
    DeviceMessageStatus getNewDeviceMessageStatus();

    /**
     * Set the {@link DeviceMessageStatus status} the DeviceMessage
     * should have after it has been forwarded to the device
     */
    void setNewDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus);

    /**
     * Additional information from the DeviceProtocol regarding this
     * DeviceMessage
     *
     * @return the deviceProtocolInformation
     */
    String getDeviceProtocolInformation();

    /**
     * Set the additional information from the DeviceProtocol regarding this
     * DeviceMessage
     *
     * @param deviceProtocolInformation the additional information text
     */
    void setDeviceProtocolInformation(String deviceProtocolInformation);

    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

}
