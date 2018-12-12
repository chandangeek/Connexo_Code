/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        return abstractUserDirectory == null || checkValidity(abstractUserDirectory,constraintValidatorContext);
    }

    private boolean checkValidity(AbstractUserDirectoryImpl abstractUserDirectory , ConstraintValidatorContext constraintValidatorContext) {

        Optional<? extends UserDirectory> alreadyExisting = userService.findUserDirectoryIgnoreCase(abstractUserDirectory.getDomain());
        if(alreadyExisting.isPresent()) {
            if (alreadyExisting.get().getType().equals("INT")) {
                if((!abstractUserDirectory.getType().equals("INT")) &&(abstractUserDirectory.getDomain().equals("Local"))){
                    return false;
                }else{
                    return true;
                }
            }else {
                return !alreadyExisting.isPresent() || !checkExisting(abstractUserDirectory, (AbstractLdapDirectoryImpl) alreadyExisting.get(), constraintValidatorContext);
            }
        }else{
            return !alreadyExisting.isPresent() || !checkExisting(abstractUserDirectory, (AbstractLdapDirectoryImpl) alreadyExisting.get(), constraintValidatorContext);
        }

    }

    private boolean checkExisting(AbstractUserDirectoryImpl abstractUserDirectory, AbstractUserDirectoryImpl alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(abstractUserDirectory, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }
    private boolean areNotTheSame(AbstractUserDirectoryImpl abstractUserDirectory, AbstractUserDirectoryImpl alreadyExisting) {
        return ((abstractUserDirectory.getId() != alreadyExisting.getId())&(abstractUserDirectory.getDomain().toLowerCase().equals(alreadyExisting.getDomain().toLowerCase())));
    }



}
