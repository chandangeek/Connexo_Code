package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Models the exceptional situation that occurs when client code
 * ignored the failure of a {@link DeviceCommandExecutor}
 * to prepare the execution of {@link DeviceCommand}s.
 * Since this is an indication of a coding error and can therefore
 * only occur during development when the code is not yet complete
 * this exceptional situation have been modeled
 * as a runtime exception to avoid exposing this exception
 * on all or almost all methods in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-17 (16:24)
 */
public class NoResourcesAcquiredException extends ComServerRuntimeException {

    public NoResourcesAcquiredException () {
        super(exceptionCode());
    }

    private static ExceptionCode exceptionCode () {
        return new ExceptionCode(new ComServerModelReferenceScope(), ExceptionType.CODING, ComServerModelExceptionReferences.NO_RESOURCES_ACQUIRED);
    }

}