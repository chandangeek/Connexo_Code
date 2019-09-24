/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasNoBlacklistedCharactersValidator implements ConstraintValidator<HasNoBlacklistedCharacters, CharSequence> {

    private char[] blacklisted;

    @Override
    public void initialize(HasNoBlacklistedCharacters hasNoBlacklistedCharacters) {
        blacklisted = hasNoBlacklistedCharacters.blacklisted();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext context) {
        for (char badChar : blacklisted) {
            if (charSequence.chars().anyMatch(chr -> chr == badChar)) {
                return false;
            }
        }
        return true;
    }
}
