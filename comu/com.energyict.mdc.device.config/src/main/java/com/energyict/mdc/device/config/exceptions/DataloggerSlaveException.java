/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.MessageSeeds;

public class DataloggerSlaveException extends LocalizedException {

    private DataloggerSlaveException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static DataloggerSlaveException logbookTypesAreNotSupported(Thesaurus thesaurus, DeviceType deviceType) {
        DataloggerSlaveException dataloggerSlaveException = new DataloggerSlaveException(thesaurus, MessageSeeds.DATALOGGER_SLAVE_NO_LOGBOOKTYPE_SUPPORT);
        dataloggerSlaveException.set("deviceType", deviceType);
        return dataloggerSlaveException;
    }

    public static DataloggerSlaveException cannotChangeLogBookTypeWhenConfigsExistWithLogBookSpecs(Thesaurus thesaurus, DeviceType deviceType) {
        DataloggerSlaveException dataloggerSlaveException = new DataloggerSlaveException(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_TO_DATALOGGER_SLAVE_WHEN_LOGBOOK_SPECS_EXIST);
        dataloggerSlaveException.set("deviceType", deviceType);
        return dataloggerSlaveException;
    }

    public static DataloggerSlaveException deviceProtocolPluggableClassIsNoSupported(Thesaurus thesaurus, DeviceType deviceType) {
        DataloggerSlaveException dataloggerSlaveException = new DataloggerSlaveException(thesaurus, MessageSeeds.DATALOGGER_SLAVE_NO_PROTOCOL_PLUGGABLE_CLASS);
        dataloggerSlaveException.set("deviceType", deviceType);
        return dataloggerSlaveException;
    }

    public static DataloggerSlaveException logbookSpecsAreNotSupported(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration) {
        DataloggerSlaveException dataloggerSlaveException = new DataloggerSlaveException(thesaurus, MessageSeeds.DATALOGGER_SLAVE_NO_LOGBOOKSPEC_SUPPORT);
        dataloggerSlaveException.set("deviceConfiguration", deviceConfiguration);
        return dataloggerSlaveException;
    }

    public static DataloggerSlaveException cannotChangeDataloggerFunctionalityEnabledOnceTheConfigIsActive(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration) {
        DataloggerSlaveException dataloggerSlaveException = new DataloggerSlaveException(thesaurus, MessageSeeds.DATALOGGER_ENABLED_CANNOT_CHANGE_ON_ACTIVE_CONFIG);
        dataloggerSlaveException.set("deviceConfiguration", deviceConfiguration);
        return dataloggerSlaveException;
    }
}
