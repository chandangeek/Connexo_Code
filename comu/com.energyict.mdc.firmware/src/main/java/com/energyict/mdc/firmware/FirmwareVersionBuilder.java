/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import aQute.bnd.annotation.ProviderType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-11 (10:57)
 */
@ProviderType
public interface FirmwareVersionBuilder {

    FirmwareVersionBuilder initFirmwareFile(byte[] firmwareFile);

    FirmwareVersionBuilder setExpectedFirmwareSize(Integer fileSize);

    FirmwareVersionBuilder setMeterFirmwareDependency(FirmwareVersion meterFirmwareDependency);

    FirmwareVersionBuilder setCommunicationFirmwareDependency(FirmwareVersion communicationFirmwareDependency);

    FirmwareVersionBuilder setAuxiliaryFirmwareDependency(FirmwareVersion auxiliaryFirmwareDependency);

    FirmwareVersion create();

    /**
     * Validate will not create anything, just running the validation
     */
    void validate();

}
