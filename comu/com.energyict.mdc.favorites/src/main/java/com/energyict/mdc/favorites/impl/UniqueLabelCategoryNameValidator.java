package com.energyict.mdc.favorites.impl;

import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.favorites.LabelCategory;

public class UniqueLabelCategoryNameValidator implements ConstraintValidator<UniqueName, LabelCategory> {

    private final FavoritesService favoritesService;
    private String message;

    @Inject
    public UniqueLabelCategoryNameValidator(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(LabelCategory category, ConstraintValidatorContext context) {
        return category == null || !(hasEquallyNamedCategory(category, context));
    }

    private boolean hasEquallyNamedCategory(LabelCategory category, ConstraintValidatorContext context) {
        Optional<LabelCategory> foundCategory = favoritesService.findLabelCategory(category.getName());
        if (foundCategory.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }
}