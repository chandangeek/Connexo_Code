package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.LogBookType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link com.energyict.mdc.device.config.LogBookSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * which is modeled by a {@link LogBookType} which is not part of the {@link com.energyict.mdc.device.config.DeviceType}
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 13:33
 */
public class LogbookTypeIsNotConfiguredOnDeviceTypeException extends LocalizedException {

    public LogbookTypeIsNotConfiguredOnDeviceTypeException(LogBookType logBookType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, logBookType);
        set("logBookType", logBookType);
    }

}