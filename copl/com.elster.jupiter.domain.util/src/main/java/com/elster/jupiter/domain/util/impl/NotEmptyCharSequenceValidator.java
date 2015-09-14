package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.NotEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.Checks.is;

public class NotEmptyCharSequenceValidator implements ConstraintValidator<NotEmpty, CharSequence> {

    @Override
    public void initialize(NotEmpty unique) {

    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        return charSequence != null && !is(charSequence.toString().trim()).empty();
    }
}
