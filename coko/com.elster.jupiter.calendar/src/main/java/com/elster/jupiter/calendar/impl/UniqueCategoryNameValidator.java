/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

public class UniqueCategoryNameValidator implements ConstraintValidator<UniqueCategoryName, Category> {

    private String message;
    private CalendarService calendarService;

    @Inject
    public UniqueCategoryNameValidator(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void initialize(UniqueCategoryName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Category category, ConstraintValidatorContext context) {
        return category == null || !checkExisting(category, context);
    }

    private boolean checkExisting(Category category, ConstraintValidatorContext context) {
        Condition condition = Operator.EQUAL.compare("name", category.getName());
        Optional<Category> found = calendarService.findCategoryByName(category.getName());
        if (found.isPresent() && areDifferentWithSameName(category, found.get())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areDifferentWithSameName(Category category, Category existingCategory) {
        return existingCategory.getName().equals(category.getName()) && (existingCategory.getId() != category.getId());
    }

}

