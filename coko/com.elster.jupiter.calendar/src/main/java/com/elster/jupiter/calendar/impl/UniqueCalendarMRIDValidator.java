package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;

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
