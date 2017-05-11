/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.config.DeviceType;

public class VetoDeviceTypeDeleteException extends LocalizedException {
    public VetoDeviceTypeDeleteException(Thesaurus thesaurus, DeviceType deviceType) {
        super(thesaurus, MessageSeeds.DEVICE_TYPE_IN_USE, deviceType.getName());
    }
}
