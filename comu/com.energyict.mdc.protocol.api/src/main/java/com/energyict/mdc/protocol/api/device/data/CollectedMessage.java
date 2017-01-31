/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.time.Instant;

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
     * Set the {@link DeviceMessageStatus status} the DeviceMessage should have after it has been forwarded to the device
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

    /**
     * Gets the date the DeviceMessage has been send out to the device by the ComServer
     *
     * @return the sent date
     */
    public Instant getSentDate();

    /**
     * Sets the time the DeviceMessage has been send out the device by the ComServer <br/>
     * Note that the sent date is also updated as part of {@link #setNewDeviceMessageStatus(DeviceMessageStatus)}
     *
     * @param sentDate the sent date
     */
    public void setSentDate(Instant sentDate);

}
