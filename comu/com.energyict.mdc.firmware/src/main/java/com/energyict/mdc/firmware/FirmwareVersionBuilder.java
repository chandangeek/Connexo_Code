/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-11 (10:57)
 */
public interface FirmwareVersionBuilder {

    FirmwareVersionBuilder initFirmwareFile(byte[] firmwareFile);

    FirmwareVersionBuilder setExpectedFirmwareSize(Integer fileSize);

    FirmwareVersion create();

    /**
     * Validate will not create anything, just running the validation
     */
    void validate();

}