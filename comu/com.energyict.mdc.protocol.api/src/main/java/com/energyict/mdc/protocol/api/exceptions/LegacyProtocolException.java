package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

import java.io.IOException;

/**
 * Should only be used by the Adapter classes if {@link IOException}s are thrown by any of their interfaces.
 *
 * @author gna
 * @since 29/03/12 - 10:05
 */
public class LegacyProtocolException extends ComServerRuntimeException {

    public LegacyProtocolException(IOException cause){
        super(cause, generateExceptionCodeByReference(CommonExceptionReferences.LEGACY_IO), cause.getMessage());
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.SYSTEM, reference);
    }

}
