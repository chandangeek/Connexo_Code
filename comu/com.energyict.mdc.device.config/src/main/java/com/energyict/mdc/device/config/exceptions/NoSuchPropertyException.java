package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when client code is using a property
 * that is not known by the {@link DeviceProtocolPluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (10:26)
 */
public class NoSuchPropertyException extends LocalizedException {

    public NoSuchPropertyException(Thesaurus thesaurus, DeviceProtocolPluggableClass deviceProtocol, String propertyName) {
        super(thesaurus, MessageSeeds.PROTOCOL_HAS_NO_SUCH_PROPERTY, deviceProtocol.getName(), propertyName);
        set("deviceProtocolPluggableClass", deviceProtocol);
        set("propertyName", propertyName);
    }

}