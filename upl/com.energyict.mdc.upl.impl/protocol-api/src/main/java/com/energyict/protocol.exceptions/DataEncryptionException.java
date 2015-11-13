package com.energyict.protocol.exceptions;

/**
 * Models the exceptional situation that occurs when data received
 * from an device could not be correctly decrypted, most likely due to the fact
 * that the wrong password or decryption key(s) were used.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (12:02)
 */
public class DataEncryptionException extends CommunicationException {

    protected DataEncryptionException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected DataEncryptionException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private DataEncryptionException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    /**
     * Throws a new DataEncryptionException that indicates
     * that encrypted data received could not be correctly decrypted.
     */
    public static DataEncryptionException dataEncryptionException() {
        return new DataEncryptionException(ProtocolExceptionReference.DATA_ENCRYPTION_EXCEPTION);
    }

    public static DataEncryptionException dataEncryptionException(Exception cause) {
        return new DataEncryptionException(cause, ProtocolExceptionReference.DATA_ENCRYPTION_EXCEPTION_WITH_CAUSE, cause.getMessage());
    }

}