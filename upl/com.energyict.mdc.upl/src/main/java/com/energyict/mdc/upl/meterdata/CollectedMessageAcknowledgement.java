package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.time.Instant;

/**
 * Models acknowledgement information about {@link DeviceMessage DeviceMessages} that was collected from the device.<br></br>
 * E.g.: information telling a certain deviceMessage executed successfully or failed with a certain error.
 *
 * @author sva
 * @since 3/07/13 - 14:24
 */
public interface CollectedMessageAcknowledgement extends CollectedData {

    MessageIdentifier getMessageIdentifier();

    DeviceMessageStatus getDeviceMessageStatus();

    void setDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus);

    Instant getSentDate();

    void setSentDate(Instant sentDate);

    String getProtocolInfo();

    void setProtocolInfo(String newProtocolInfo);
}
