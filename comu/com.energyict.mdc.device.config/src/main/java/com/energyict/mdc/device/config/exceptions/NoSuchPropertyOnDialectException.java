/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

public class NoSuchPropertyOnDialectException extends LocalizedException {

    public NoSuchPropertyOnDialectException(DeviceProtocolDialect dialect, String propertyName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, dialect.getDeviceProtocolDialectName(), propertyName);
        set("dialect", dialect);
        set("propertyName", propertyName);
    }

}