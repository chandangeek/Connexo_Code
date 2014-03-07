package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * Copyrights EnergyICT
 * Date: 7/03/14
 * Time: 13:49
 */
public class NoSuchPropertyOnDialectException extends LocalizedException {

    public NoSuchPropertyOnDialectException(Thesaurus thesaurus, DeviceProtocolDialect dialect, String propertyName) {
        super(thesaurus, MessageSeeds.PROTOCOL_DIALECT_HAS_NO_SUCH_PROPERTY, dialect.getDeviceProtocolDialectName(), propertyName);
        set("dialect", dialect);
        set("propertyName", propertyName);
    }
}
