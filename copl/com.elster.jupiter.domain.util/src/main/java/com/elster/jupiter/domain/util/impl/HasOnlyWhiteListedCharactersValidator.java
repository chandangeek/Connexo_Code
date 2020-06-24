/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.util.PathVerification;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasOnlyWhiteListedCharactersValidator implements ConstraintValidator<HasOnlyWhiteListedCharacters, CharSequence> {

    private String regex;

    @Override
    public void initialize(HasOnlyWhiteListedCharacters hasOnlyWhiteListedCharacters) {
        regex = hasOnlyWhiteListedCharacters.whitelistRegex();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        return (charSequence != null && PathVerification.validateInputPattern(charSequence,regex)) ;

    }
}