/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.TrustStore;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Created by bvn on 2/22/17.
 */
public class UniqueTrustStoreNameValidator implements ConstraintValidator<UniqueName,TrustStore> {

    private final DataModel dataModel;
    private String message;

    @Inject
    public UniqueTrustStoreNameValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(UniqueName uniqueName) {
        this.message = uniqueName.message();
    }

    @Override
    public boolean isValid(TrustStore trustStore, ConstraintValidatorContext constraintValidatorContext) {
        Optional<TrustStore> namesake = dataModel.mapper(TrustStore.class).getUnique("name", trustStore.getName());
        if (namesake.isPresent()) {
            if (namesake.get().getId() != trustStore.getId()) {
                fail(constraintValidatorContext);
                return false;
            }

        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("name")
                .addConstraintViolation();
    }

}
