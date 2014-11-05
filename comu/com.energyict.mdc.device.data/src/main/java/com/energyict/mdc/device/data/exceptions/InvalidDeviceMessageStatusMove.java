package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

/**
 * Models the exceptional situation that occurs when a DeviceMessage is moved from
 * one status to another, while that transition is not allowed.
 *
 * Copyrights EnergyICT
 * Date: 11/5/14
 * Time: 11:40 AM
 */
public class InvalidDeviceMessageStatusMove extends LocalizedException {

    public InvalidDeviceMessageStatusMove(Thesaurus thesaurus, DeviceMessageStatus initialStatus, DeviceMessageStatus newStatus) {
        super(thesaurus, MessageSeeds.DEVICE_MESSAGE_STATUS_INVALID_MOVE, initialStatus, newStatus);
        this.set("initialStatus", initialStatus);
        this.set("newStatus", newStatus);
    }
}
