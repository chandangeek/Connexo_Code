package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.BusinessException;

/**
 * Models the temporary exceptional situation that occurs
 * when legacy code that has not been ported to the new ORM framework yet
 * is still throwing {@link BusinessException} and SQLException.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (14:25)
 */
public class LegacyException extends LocalizedException {

    public LegacyException(Thesaurus thesaurus, Throwable t) {
        super(thesaurus, MessageSeeds.LEGACY, t);
    }

}