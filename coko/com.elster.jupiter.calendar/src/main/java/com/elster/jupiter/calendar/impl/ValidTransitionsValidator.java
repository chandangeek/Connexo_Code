/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.calendar.FixedPeriodTransitionSpec;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

public class ValidTransitionsValidator implements ConstraintValidator<ValidTransitions, Calendar> {

    private String message;
    private CalendarService calendarService;

    @Inject
    public ValidTransitionsValidator(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void initialize(ValidTransitions constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Calendar calendar, ConstraintValidatorContext context) {
        return calendar == null || checkIfValid(calendar, context);
    }

    private boolean checkIfValid(Calendar calendar, ConstraintValidatorContext context) {
        if (calendar.getPeriodTransitionSpecs().isEmpty()) {
            return true;
        }
        boolean recurring = calendar.getPeriodTransitionSpecs().get(0) instanceof RecurrentPeriodTransitionSpec;
        if (recurring) {
            return true;
        }

        LocalDate startOfYear = LocalDate.of(calendar.getStartYear().getValue(), 1, 1);
        Optional<? extends PeriodTransitionSpec> transition =
            calendar.getPeriodTransitionSpecs().stream().filter(t -> (!((FixedPeriodTransitionSpec) t).getOccurrence().isAfter(startOfYear))).findAny();
        if (!transition.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("periodTransitionSpecs").addConstraintViolation();
            return false;
        }
        return true;
    }

}
