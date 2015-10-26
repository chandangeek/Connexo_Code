package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when client code is using a property
 * that is not known by the {@link DeviceProtocolPluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (10:26)
 */
public class NoSuchPropertyException extends LocalizedException {

    public NoSuchPropertyException(DeviceProtocolPluggableClass deviceProtocol, String propertyName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, deviceProtocol.getName(), propertyName);
        set("deviceProtocolPluggableClass", deviceProtocol);
        set("propertyName", propertyName);
    }

}