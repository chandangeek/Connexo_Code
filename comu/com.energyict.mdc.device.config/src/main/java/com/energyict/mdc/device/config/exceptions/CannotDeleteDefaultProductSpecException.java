package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ProductSpec;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete the default {@link ProductSpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:31)
 */
public class CannotDeleteDefaultProductSpecException extends LocalizedException {

    public CannotDeleteDefaultProductSpecException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DEFAULT_PRODUCT_SPEC_CANNOT_BE_DELETED);
    }

}