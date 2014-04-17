package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a entity within this bundle
 * with an {@link ObisCode} that is already used another entity of the same type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (15:06)
 */
public class DuplicateObisCodeException extends LocalizedException {

    private DuplicateObisCodeException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static DuplicateObisCodeException forRegisterMapping(Thesaurus thesaurus, ObisCode obisCode, Phenomenon phenomenon, int timeOfUse, RegisterMapping registerMapping) {
        DuplicateObisCodeException duplicateObisCodeException =
                new DuplicateObisCodeException(
                        thesaurus,
                        MessageSeeds.REGISTER_MAPPING_OBIS_CODE_TOU_PHENOMENON_ALREADY_EXISTS,
                        obisCode.toString(),
                        phenomenon.toString(),
                        timeOfUse);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("phenomenon", phenomenon.toString());
        duplicateObisCodeException.set("timeOfUse", timeOfUse);
        duplicateObisCodeException.set("registerMapping", registerMapping.getName());
        return duplicateObisCodeException;
    }

}