package com.elster.jupiter.cps.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to register the same {@link com.elster.jupiter.cps.CustomPropertySet}
 * a second time causing a duplicate.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-17 (09:42)
 */
public class DuplicateCustomPropertySetException extends LocalizedException {
    public DuplicateCustomPropertySetException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DUPLICATE_CUSTOM_PROPERTY_SET);
    }
}