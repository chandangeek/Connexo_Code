/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.MessageSeeds;
import com.energyict.mdc.masterdata.LogBookType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link LogBookType} to a {@link DeviceType}
 * but that LogBookType was already added to the DeviceType before.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:49)
 */
public class LogBookTypeAlreadyInDeviceTypeException extends LocalizedException {

    public LogBookTypeAlreadyInDeviceTypeException(DeviceType deviceType, LogBookType logBookType, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DUPLICATE_LOG_BOOK_TYPE_IN_DEVICE_TYPE, logBookType.getName(), deviceType.getName());
        this.set("deviceType", deviceType);
        this.set("logBookType", logBookType);
    }

}