package com.energyict.mdc.upl.crypto;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Models the behavior of a component that will encrypt/decrypt
 * data sent/returned by a device with the MD5 algorithm.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (09:27)
 */
public interface MD5Cryptographer {

    /**
     * Represents a seed for the MD5 cryptographic algorithm.
     */
    interface MD5Seed {

        /**
         * Gets the raw bytes that constitute this seed.
         *
         * @return The raw bytes
         */
        byte[] getBytes ();

    }

    /**
     * Calculates an appropriate MD5Seed for the device
     * that is uniquely identified by the specified {@link DeviceIdentifier}.
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param source Information provided by the source of the encrypted information
     * @return The MD5Seed
     */
    MD5Seed build(DeviceIdentifier deviceIdentifier, String source);

}