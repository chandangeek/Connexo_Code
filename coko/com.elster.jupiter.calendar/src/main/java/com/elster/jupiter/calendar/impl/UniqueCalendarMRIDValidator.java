package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

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
        Condition condition = Operator.EQUAL.compare("mRID", calendar.getMRID());
        Optional<Calendar> found = calendarService.findCalendarByMRID(calendar.getMRID());
        if (found.isPresent() && areDifferentWithSameMRID(calendar, found.get())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("mRID").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areDifferentWithSameMRID(Calendar calendar, Calendar existingCalendar) {
        return existingCalendar.getMRID().equals(calendar.getMRID()) && (existingCalendar.getId() != calendar.getId());
    }

}
