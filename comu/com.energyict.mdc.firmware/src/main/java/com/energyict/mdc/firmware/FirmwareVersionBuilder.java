package com.energyict.mdc.firmware;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-11 (10:57)
 */
public interface FirmwareVersionBuilder {

    FirmwareVersionBuilder setExpectedFirmwareSize(Integer fileSize);

    FirmwareVersion create();

    /**
     * Validate will not create anything, just running the validation
     */
    void validate();

}