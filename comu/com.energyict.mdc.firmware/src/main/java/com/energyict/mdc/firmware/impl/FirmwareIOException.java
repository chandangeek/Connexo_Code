/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.io.IOException;

/**
 * RuntimeException to wrap IOException that occur when reading/writing the {@link FirmwareVersion}s firmware blob.
 *
 * @author sva
 * @since 20/05/2016 - 10:47
 */
public class FirmwareIOException extends LocalizedException {

    public FirmwareIOException(Thesaurus thesaurus, IOException cause) {
        super(thesaurus, MessageSeeds.FIRMWARE_FILE_IO, cause, cause.toString());
    }
}