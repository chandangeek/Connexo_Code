package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.commands.ComCommand;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Models the exceptions related to the {@link ComCommand ComCommands}
 *
 * @author gna
 * @since 10/05/12 - 13:33
 */
public final class ComCommandException extends ComServerRuntimeException {

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link ExceptionCode}.
     *
     * @param code      The ExceptionCode
     * @param arguments values identifying the exception
     */
    private ComCommandException(ExceptionCode code, Object... arguments) {
        super(code, arguments);
    }

    /**
     * Creates an Exception indicating that the given argument already exists in the {@link com.energyict.mdc.commands.CommandRoot
     * CommandRoot}
     *
     * @param comCommand the {@link ComCommand} violating the uniqueness
     * @return the newly created exception
     */
    public static ComCommandException uniqueCommandViolation(final ComCommand comCommand) {
        return new ComCommandException(generateExceptionCodeByReference(ComServerExecutionExceptionReferences.COMMAND_NOT_UNIQUE), comCommand);
    }

    /**
     * Creates an Exception indicating that the given command is not allowed for the given DeviceProtocol.
     *
     * @param comCommand     the violating {@link ComCommand}
     * @param deviceProtocol the deviceProtocol which is executing
     * @return the newly created exception
     */
    public static ComCommandException illegalCommand(final ComCommand comCommand, final DeviceProtocol deviceProtocol) {
        return new ComCommandException(generateExceptionCodeByReference(ComServerExecutionExceptionReferences.ILLEGAL_COMMAND), comCommand, deviceProtocol);
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(ComServerExecutionExceptionReferences reference) {
        return new ExceptionCode(ComServerExecutionReferenceScope.SINGLETON, ExceptionType.CODING, reference);
    }
}
