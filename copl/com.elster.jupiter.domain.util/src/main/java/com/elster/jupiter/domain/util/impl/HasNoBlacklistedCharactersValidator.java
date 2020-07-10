/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.util.PathVerification;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class HasNoBlacklistedCharactersValidator implements ConstraintValidator<HasNoBlacklistedCharacters, CharSequence> {

    private Set<Integer> blacklisted;
    private String balcklistedCharRegEx;

    @Override
    public void initialize(HasNoBlacklistedCharacters hasNoBlacklistedCharacters) {
        balcklistedCharRegEx = hasNoBlacklistedCharacters.balcklistedCharRegEx();
        if(balcklistedCharRegEx.isEmpty()) {
            blacklisted = new HashSet<>(hasNoBlacklistedCharacters.blacklisted().length, 1);
            for (char bad : hasNoBlacklistedCharacters.blacklisted()) {
                blacklisted.add((int) bad);
            }
        }
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext context) {
        if(!balcklistedCharRegEx.isEmpty()) {
            return (charSequence != null && PathVerification.validateInputPattern(charSequence,balcklistedCharRegEx)) ;
        } else if(null != blacklisted && !blacklisted.isEmpty()) {
            return charSequence == null || charSequence.chars().noneMatch(blacklisted::contains);
        } else {
            return false;
        }
    }
}
