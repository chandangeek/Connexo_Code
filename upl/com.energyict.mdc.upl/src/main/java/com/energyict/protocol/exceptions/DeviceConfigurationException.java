/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * @author Stijn Vanhoorelbeke
 * @since 29.09.17 - 17:21
 */
public class DeviceConfigurationException extends ProtocolRuntimeException {

    public DeviceConfigurationException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public DeviceConfigurationException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }
}