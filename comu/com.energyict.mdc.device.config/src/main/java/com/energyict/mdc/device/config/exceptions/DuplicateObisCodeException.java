package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a {@link RegisterMapping}
 * with an {@link ObisCode} that is already used
 * by another RegisterMapping.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (10:59)
 */
public class DuplicateObisCodeException extends LocalizedException {

    public DuplicateObisCodeException(Thesaurus thesaurus, ObisCode obisCode, RegisterMapping registerMapping) {
        super(thesaurus, MessageSeeds.REGISTER_MAPPING_OBIS_CODE_ALREADY_EXISTS, obisCode.toString(), registerMapping.getName());
        this.set("obisCode", obisCode.toString());
        this.set("registerMapping", registerMapping.getName());
    }

}