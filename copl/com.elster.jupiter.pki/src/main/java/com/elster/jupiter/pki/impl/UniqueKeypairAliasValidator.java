/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.impl.wrappers.keypair.KeypairWrapperImpl;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;


/**
 * The alias of a keypair wrapper needs to be unique withing the keypair store
 */
public class UniqueKeypairAliasValidator implements ConstraintValidator<UniqueAlias,KeypairWrapper> {

    private final DataModel dataModel;
    private String message;

    @Inject
    public UniqueKeypairAliasValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(UniqueAlias uniqueName) {
        this.message = uniqueName.message();
    }

    @Override
    public boolean isValid(KeypairWrapper keypairWrapper, ConstraintValidatorContext constraintValidatorContext) {
        if (Checks.is(keypairWrapper.getAlias()).emptyOrOnlyWhiteSpace() || keypairWrapper.getAlias().length()> Table.MAX_STRING_LENGTH) {
            return true;
        }
        List<KeypairWrapper> namesakes = dataModel.mapper(KeypairWrapper.class).find(KeypairWrapperImpl.Fields.ALIAS.fieldName(), keypairWrapper.getAlias());
        for (KeypairWrapper namesake: namesakes) {
            if (namesake.getId() != keypairWrapper.getId()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(KeypairWrapperImpl.Fields.ALIAS.fieldName())
                .addConstraintViolation();
    }

}
