/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.time.Instant;

/**
 * Models acknowledgement information about DeviceMessages that were collected from a physical device.<br>
 * E.g.: successfully or failure with a certain error.
 *
 * @author sva
 * @since 3/07/13 - 14:24
 */
public interface CollectedMessageAcknowledgement extends CollectedData {

    public MessageIdentifier getMessageIdentifier();

    public DeviceMessageStatus getDeviceMessageStatus();

    public void setDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus);

    public Instant getSentDate();

    public void setSentDate(Instant sentDate);

    public String getProtocolInfo();

    public void setProtocolInfo(String newProtocolInfo);

}