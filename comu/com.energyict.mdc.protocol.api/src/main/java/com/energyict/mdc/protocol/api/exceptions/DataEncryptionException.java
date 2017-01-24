package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

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
    public DataEncryptionException (MessageSeed messageSeed, DeviceIdentifier deviceIdentifier) {
        super(messageSeed, deviceIdentifier.toString());
    }

    public DataEncryptionException (MessageSeed messageSeed, NoSuchAlgorithmException e) {
        super(messageSeed, e);
    }

    public DataEncryptionException (MessageSeed messageSeed, Object... arguments) {
        super(messageSeed, arguments);
    }

    /**
     * Throws a new DataEncryptionException that indicates
     * that encrypted data received could not be correctly decrypted.
     */
    public static DataEncryptionException dataEncryptionException() {
        return new DataEncryptionException(MessageSeeds.DATA_ENCRYPTION_EXCEPTION);
    }

    public static DataEncryptionException dataEncryptionException(Exception cause) {
        return new DataEncryptionException(MessageSeeds.DATA_ENCRYPTION_EXCEPTION_WITH_CAUSE, cause.getMessage());
    }

}