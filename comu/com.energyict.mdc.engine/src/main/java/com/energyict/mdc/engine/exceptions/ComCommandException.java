package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.common.ComServerRuntimeException;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptions related to {@link ComCommand}s.
 *
 * @author gna
 * @since 10/05/12 - 13:33
 */
public final class ComCommandException extends ComServerRuntimeException {

    /**
     * Creates an Exception indicating that the given argument already exists in the CommandRoot.
     *
     * @param comCommand the {@link ComCommand} violating the uniqueness
     * @return the newly created exception
     */
    public static ComCommandException uniqueCommandViolation(ComCommand comCommand) {
        return new ComCommandException(MessageSeeds.COMMAND_NOT_UNIQUE, comCommand);
    }

    /**
     * Creates an Exception indicating that the given command is not allowed for the given DeviceProtocol.
     *
     * @param comCommand     the violating {@link ComCommand}
     * @param deviceProtocol the deviceProtocol which is executing
     * @return the newly created exception
     */
    public static ComCommandException illegalCommand(final ComCommand comCommand, final DeviceProtocol deviceProtocol) {
        return new ComCommandException(MessageSeeds.ILLEGAL_COMMAND, comCommand,deviceProtocol);
    }

    private ComCommandException(MessageSeed messageSeed, Object... arguments) {
        super(messageSeed, arguments);
    }

}
