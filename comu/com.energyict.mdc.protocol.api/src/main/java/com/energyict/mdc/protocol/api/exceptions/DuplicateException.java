package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Provides 'Exception' functionality which can be thrown when Duplicate objects are
 * found when a unique result was expected.
 *
 * Copyrights EnergyICT
 * Date: 9/25/13
 * Time: 10:06 AM
 */
public final class DuplicateException extends ComServerRuntimeException {

    private DuplicateException(ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    public static DuplicateException duplicateFoundFor(Class duplicateClassType, String identifier){
//        A duplicate '{0}' was found when a unique result was expected for '{1}'
        return new DuplicateException(generateExceptionCodeByReference(CommonExceptionReferences.DUPLICATE_FOUND), duplicateClassType.getSimpleName(), identifier);
    }


    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CONFIGURATION, reference);
    }
}
