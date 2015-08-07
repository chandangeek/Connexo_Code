package com.energyict.mdc.engine.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

import java.util.concurrent.ExecutionException;

/**
 * Models the exceptions related to {@link com.energyict.mdc.engine.impl.commands.store.DeviceCommand}s.
 */
public class DeviceCommandException extends ComServerRuntimeException {

    private DeviceCommandException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public static DeviceCommandException errorDuringFutureGetCall(ExecutionException e) {
        return new DeviceCommandException(MessageSeeds.INBOUND_DATA_PROCESSOR_ERROR, e);
    }
}
