package com.elster.jupiter.users.impl;

import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;

public class UniqueDefaultKeyPerLocaleValidator implements ConstraintValidator<UniqueDefaultKeyPerLocale, UserPreference> {

    private final UserPreferencesService userPreferencesService;
    private String message;

    @Inject
    public UniqueDefaultKeyPerLocaleValidator(UserService userService) {
        this.userPreferencesService = userService.getUserPreferencesService();
    }

    @Override
    public void initialize(UniqueDefaultKeyPerLocale constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UserPreference preference, ConstraintValidatorContext context) {
        if (preference.getLocale() == null || preference.getKey() == null || !preference.isDefault()) {
            return true;
        }
        Optional<UserPreference> existingPreference = userPreferencesService.getPreferenceByKey(preference.getLocale(), preference.getKey());
        if (!existingPreference.isPresent()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}