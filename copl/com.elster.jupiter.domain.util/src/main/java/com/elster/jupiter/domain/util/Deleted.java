/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataModel;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class Deleted {

    private static <T extends Deletable> void validate(Validator validator, T object) {
   		Set<ConstraintViolation<T>> failures = validator.validate(object, Delete.class);
   		if (!failures.isEmpty()) {
   			throw new ConstraintViolationException(failures);
   		}
   	}

    public static void delete(DataModel dataModel, Deletable deletable) {
        validate(dataModel.getValidatorFactory().getValidator(),deletable);
        deletable.deleteDependants();
        dataModel.remove(deletable);
    }

    public interface Delete {
    }

    public interface Deletable {
        public void deleteDependants();
    }
}
