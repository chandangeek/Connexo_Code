package com.energyict.mdc.protocol.api.crypto;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Models the behavior of a component that will encrypt/decrypt
 * data sent/returned by an {@link com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol}.
 * The different cryptographic methods that are supported
 * are exposed through different methods, i.e. every cryptographic
 * method will have a separate api-method with the related parameters.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:06)
 */
public interface Cryptographer {

    /**
     * Calculates an appropriate MD5Seed for the device
     * that is uniquely identified by the specified {@link DeviceIdentifier}.
     *
     * @param deviceIdentifier The DeviceIdentifier
     * @param source Information provided by the source of the encrypted information
     * @return The MD5Seed
     */
    MD5Seed buildMD5Seed(DeviceIdentifier deviceIdentifier, String source);

    /**
     * Tests if this Cryptographer was used or not,
     * i.e. if at least one of the methods was
     * used to handle encryption.
     *
     * @return The flag that indi
     */
    boolean wasUsed();

}