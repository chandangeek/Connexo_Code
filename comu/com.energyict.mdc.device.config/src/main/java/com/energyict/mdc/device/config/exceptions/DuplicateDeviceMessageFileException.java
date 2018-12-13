/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.MessageSeeds;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a {@link com.energyict.mdc.protocol.api.DeviceMessageFile}
 * with a name that is already used by another DeviceMessageFile in the same DeviceType.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-11 (14:33)
 */
public class DuplicateDeviceMessageFileException extends LocalizedException{
    public DuplicateDeviceMessageFileException(DeviceType deviceType, String name, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DUPLICATE_DEVICE_MESSAGE_FILE_IN_DEVICE_TYPE, name, deviceType.getName());
        set("deviceType", deviceType);
    }
}
