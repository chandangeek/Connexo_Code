package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional behaviors which can occur during a change in DeviceConfiguration
 */
public class DeviceConfigurationChangeException extends LocalizedException {

    private DeviceConfigurationChangeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    private DeviceConfigurationChangeException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }

    public static DeviceConfigurationChangeException catchedExceptionDuringConfigChangeOfSingleDevice(Thesaurus thesaurus, Throwable cause){
        // TODO add messageSeed
        return new DeviceConfigurationChangeException(thesaurus, null, cause);
    }
}
