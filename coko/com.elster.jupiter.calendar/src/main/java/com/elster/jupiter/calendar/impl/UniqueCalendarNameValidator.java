package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueCalendarNameValidator implements ConstraintValidator<UniqueCalendarName, Calendar> {

    private String message;
    private CalendarService calendarService;

    @Inject
    public UniqueCalendarNameValidator(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void initialize(UniqueCalendarName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Calendar calendar, ConstraintValidatorContext context) {
        return calendar == null || !checkExisting(calendar, context);
    }

    private boolean checkExisting(Calendar calendar, ConstraintValidatorContext context) {
        Optional<Calendar> found = calendarService.findCalendarByName(calendar.getName());
        if (found.isPresent() && areDifferentWithSameName(calendar, found.get())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areDifferentWithSameName(Calendar calendar, Calendar existingCalendar) {
        return existingCalendar.getName().equals(calendar.getName()) && (existingCalendar.getId() != calendar.getId());
    }

}
