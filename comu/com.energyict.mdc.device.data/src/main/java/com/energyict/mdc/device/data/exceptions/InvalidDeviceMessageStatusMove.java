/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

public class InvalidDeviceMessageStatusMove extends LocalizedException {

    public InvalidDeviceMessageStatusMove(DeviceMessageStatus initialStatus, DeviceMessageStatus newStatus, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, initialStatus, newStatus);
        this.set("initialStatus", initialStatus);
        this.set("newStatus", newStatus);
    }

}