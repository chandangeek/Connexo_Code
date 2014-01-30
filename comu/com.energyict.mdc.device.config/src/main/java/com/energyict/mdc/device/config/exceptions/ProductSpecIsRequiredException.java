package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.RegisterMapping}
 * without a {@link com.energyict.mdc.device.config.ProductSpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (11:11)
 */
public class ProductSpecIsRequiredException extends LocalizedException {

    public ProductSpecIsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.PRODUCT_SPEC_IS_REQUIRED);
    }

}