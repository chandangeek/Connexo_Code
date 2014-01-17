package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.security.NoSuchAlgorithmException;

/**
 * Models the exceptional situation that occurs when data received
 * from an Device  could not be correctly decrypted, most likely due to the fact
 * that the wrong password or decryption key(s) were used.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (12:02)
 */
public class DataEncryptionException extends CommunicationException {

    /**
     * Throws a new DataEncryptionException that indicates
     * that encrypted data received from the @link com.energyict.mdw.core.Device device}
     * that is uniquely identified by the specified {@link DeviceIdentifier}
     * could not be correctly decrypted.
     *
     * @param deviceIdentifier The DeviceIdentifier
     */
    public DataEncryptionException (DeviceIdentifier deviceIdentifier) {
        super(new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.SECURITY));
    }

    public DataEncryptionException (NoSuchAlgorithmException e) {
        super(e, new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.SECURITY));
    }

}