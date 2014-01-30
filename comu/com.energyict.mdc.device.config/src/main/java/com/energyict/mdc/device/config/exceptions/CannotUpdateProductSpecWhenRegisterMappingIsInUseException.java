package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.energyict.mdc.device.config.ProductSpec}
 * of a {@link RegisterMapping} that is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (11:26)
 */
public class CannotUpdateProductSpecWhenRegisterMappingIsInUseException extends LocalizedException {

    public CannotUpdateProductSpecWhenRegisterMappingIsInUseException(Thesaurus thesaurus, RegisterMapping registerMapping) {
        super(thesaurus, MessageSeeds.REGISTER_MAPPING_PRODUCT_SPEC_CANNOT_BE_UPDATED, registerMapping.getName());
        this.set("registerMapping", registerMapping);
    }

}