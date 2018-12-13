/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when the current user attempts
 * to edit the values of a {@link com.elster.jupiter.cps.CustomPropertySet}
 * while he does not have the appropriate privileges to do so.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-17 (09:42)
 */
public class CurrentUserIsNotAllowedToEditValuesOfCustomPropertySetException extends LocalizedException {
    public CurrentUserIsNotAllowedToEditValuesOfCustomPropertySetException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT);
    }
}