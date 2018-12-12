package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Instant;

/**
 * A collectedMessages identifies a {@link com.energyict.mdc.upl.messages.DeviceMessage}
 * executed by the device and the result from the execution.
 *
 * @author gna
 * @since 5/04/12 - 11:19
 */
public interface CollectedMessage extends CollectedData {

    /**
     * Should provide an identifier to uniquely identify the requested DeviceMessage.
     *
     * @return the {@link MessageIdentifier identifier}
     * of the BusinessObject which is actionHolder of the request
     */
    MessageIdentifier getMessageIdentifier();

    /**
     * Provides the {@link DeviceMessageStatus status} the
     * {@link com.energyict.mdc.upl.messages.DeviceMessage DeviceMessage} should have after
     * the message has been forwarded to the device.
     *
     * @return the new DeviceMessageStatus
     */
    DeviceMessageStatus getNewDeviceMessageStatus();

    /**
     * Set the {@link DeviceMessageStatus status} the {@link com.energyict.mdc.upl.messages.DeviceMessage message}
     * should have after it has been forwarded to the device
     */
    void setNewDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus);

    /**
     * Additional information from the DeviceProtocol regarding this DeviceMessage.
     *
     * @return the deviceProtocolInformation
     */
    String getDeviceProtocolInformation();

    /**
     * Set the additional information from the DeviceProtocol regarding this DeviceMessage.
     *
     * @param deviceProtocolInformation the additional information text
     */
    void setDeviceProtocolInformation(String deviceProtocolInformation);

    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

    /**
     * Gets the date the DeviceMessage has been sent out to the device by the ComServer
     *
     * @return the sent date
     */
    Instant getSentDate();

    /**
     * Sets the time the DeviceMessage has been sent out the device by the ComServer <br/>
     * Note that the sent date is also updated as part of {@link #setNewDeviceMessageStatus(DeviceMessageStatus)}
     *
     * @param sentDate the sent date
     */
    void setSentDate(Instant sentDate);

}