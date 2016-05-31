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
        Condition condition = Operator.EQUAL.compare("name", calendar.getName());
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
