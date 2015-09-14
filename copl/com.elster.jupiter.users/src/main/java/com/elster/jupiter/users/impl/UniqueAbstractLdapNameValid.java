package com.elster.jupiter.users.impl;


import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueAbstractLdapNameValid implements ConstraintValidator<UniqueName,AbstractUserDirectoryImpl> {

    private String message;
    private UserService userService;

    @Inject
    public UniqueAbstractLdapNameValid(UserService userService) {
        this.userService = userService;
    }
    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(AbstractUserDirectoryImpl abstractUserDirectory, ConstraintValidatorContext constraintValidatorContext) {
        return true;
    }

    private boolean checkValidity(AbstractUserDirectoryImpl abstractUserDirectory , ConstraintValidatorContext constraintValidatorContext) {

        Optional<? extends UserDirectory> alreadyExisting = userService.findUserDirectory(abstractUserDirectory.getDomain());

        return !alreadyExisting.isPresent();

    }


}
