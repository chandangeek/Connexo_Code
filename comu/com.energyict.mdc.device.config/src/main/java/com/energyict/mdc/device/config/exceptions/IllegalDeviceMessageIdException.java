/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class IllegalDeviceMessageIdException extends LocalizedException {

    public IllegalDeviceMessageIdException(long deviceMessageId, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, deviceMessageId);
        this.set("deviceMessageId.dbValue", deviceMessageId);
    }

}
