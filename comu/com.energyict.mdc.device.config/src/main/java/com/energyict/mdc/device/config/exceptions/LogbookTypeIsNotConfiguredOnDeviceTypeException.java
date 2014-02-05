package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.LogBookType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link com.energyict.mdc.device.config.LogBookSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * which is modeled by a {@link com.energyict.mdc.device.config.LogBookType} which is not part of the {@link com.energyict.mdc.device.config.DeviceType}
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 13:33
 */
public class LogbookTypeIsNotConfiguredOnDeviceTypeException extends LocalizedException {

    public LogbookTypeIsNotConfiguredOnDeviceTypeException(Thesaurus thesaurus, LogBookType logBookType) {
        super(thesaurus, MessageSeeds.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_NOT_ON_DEVICE_TYPE, logBookType);
        set("logBookType", logBookType);
    }
}
