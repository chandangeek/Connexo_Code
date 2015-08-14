package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Created by mandr on 8/10/2015.
 */
public class UniqueGroupNameValidator implements ConstraintValidator<UniqueName, GroupImpl> {

    private String message;
    private UserService userService;

    @Inject
    public UniqueGroupNameValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(GroupImpl group, ConstraintValidatorContext context) {
        return group == null || checkValidity(group, context);
    }

    private boolean checkValidity(GroupImpl group , ConstraintValidatorContext context) {
        Optional<? extends Group > alreadyExisting = userService.getGroup(group.getName());
        return !alreadyExisting.isPresent() || !checkExisting(group, alreadyExisting.get(), context);
    }

    private boolean checkExisting(Group group, Group alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(group, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }
    private boolean areNotTheSame(Group group, Group alreadyExisting) {
        return group.getId() != alreadyExisting.getId();
    }
}
