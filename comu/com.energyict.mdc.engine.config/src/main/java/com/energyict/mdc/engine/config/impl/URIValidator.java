/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URISyntaxException;

/**
 * Validates that a String is a valid URI.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-31 (11:37)
 */
public class URIValidator implements ConstraintValidator<URI, String> {

    @Override
    public void initialize(URI constraintAnnotation) {
        // No need to store the annotation for now.
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if ( Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return true;
        }
        else {
            try {
                new java.net.URI(value);
                return true;
            }
            catch (URISyntaxException e) {
                return false;
            }
        }
    }

}