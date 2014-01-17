package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Should be used in any Security-related issue during a communication session with a Device
 *
 * @author gna
 * @since 28/03/12 - 16:40
 */
public class DeviceSecurityException extends CommunicationException {

    /**
     * TODO define different security Levels
     */
    public DeviceSecurityException(){
        super(generateExceptionCodeByReference(CommonExceptionReferences.SECURITY));
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, reference);
    }

}