package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.time.Instant;

/**
 * Models acknowledgement information about DeviceMessages that were collected from a physical device.<br>
 * E.g.: successfully or failure with a certain error.
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