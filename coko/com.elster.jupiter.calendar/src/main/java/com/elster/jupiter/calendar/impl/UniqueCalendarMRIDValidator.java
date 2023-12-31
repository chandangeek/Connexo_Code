/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueCalendarMRIDValidator implements ConstraintValidator<UniqueMRID, Calendar> {

    private String message;
    private CalendarService calendarService;

    @Inject
    public UniqueCalendarMRIDValidator(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void initialize(UniqueMRID constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Calendar calendar, ConstraintValidatorContext context) {
        return calendar == null || !checkExisting(calendar, context);
    }

    private boolean checkExisting(Calendar calendar, ConstraintValidatorContext context) {
        String mRID = calendar.getMRID();
        // Null or empty mRID is never a duplicate as it is an optional field
        if (!Checks.is(mRID).empty()) {
            Optional<Calendar> found = calendarService.findCalendarByMRID(mRID);
            if (found.isPresent() && areDifferentWithSameMRID(calendar, found.get())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("mRID").addConstraintViolation();
                return true;
            }
        }
        return false;
    }

    private boolean areDifferentWithSameMRID(Calendar calendar, Calendar existingCalendar) {
        return (existingCalendar.getId() != calendar.getId())
            && (Checks.is(existingCalendar.getMRID()).equalTo(calendar.getMRID()));
    }

}