package com.elster.jupiter.pki;

import java.time.Instant;

/**
 * This class wraps an actual Key with the information required to read it from db or renew it.
 * Through offering PropertySpecs & properties, a generic interface is offered for the UI
 */
public interface SymmetricKeyWrapper extends HasDynamicPropertiesWithUpdatableValues, Renewable {

    /**
     * Defines the method used to store keys by this implementation.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * The exact date when the value of this element will expire. The value should be renewed by thia date.
     * @return date until which this element is valid
     */
    Instant getExpirationTime();

    /**
     * This function is used to do the encrypted export of stored PSK for the data concentrator. The import of key into
     * the smart meter is done during the personalisation by the manufacturer or in case of renewal and done by the
     * renewal function by using key wrapping.
     * This method should be called on the DC MasterKey which is stored inside the Connexo DB. This key wraps the smart meter
     * key by using NIST wrapping.
     * @param smartMeterWorkingKey - Smart Meter Working key which is stored as PSK inside Connexo DB. It will be wrapped
     * by the DC Master Key (data concentrator).
     * @return the byte array that contains the cryptogram (AES-wrapped) of the exported Device-WK that can be imported
     * into a data concentrator.
     * @see com.atos.worldline.jss.api.custom.energy.Energy.cosemPskExportDataConcentrator
     */
    byte[] wrapMeterKeyForConcentrator(SymmetricKeyWrapper smartMeterWorkingKey) throws HsmException, DataEncryptionException;
}
