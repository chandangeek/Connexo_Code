/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.impl.MessageSeeds;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a {@link com.energyict.mdc.device.config.impl.DeviceIcon}
 * that is too big in file size.
 */

public class DeviceIconTooBigException extends LocalizedException {
    public DeviceIconTooBigException(int fileSize, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.MAX_FILE_SIZE_EXCEEDED_KB, fileSize);
    }
}
