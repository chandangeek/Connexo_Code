package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Copyrights EnergyICT
 * Date: 7/03/14
 * Time: 13:49
 */
public class NoSuchPropertyOnDialectException extends LocalizedException {

    public NoSuchPropertyOnDialectException(DeviceProtocolDialect dialect, String propertyName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, dialect.getDeviceProtocolDialectName(), propertyName);
        set("dialect", dialect);
        set("propertyName", propertyName);
    }

}