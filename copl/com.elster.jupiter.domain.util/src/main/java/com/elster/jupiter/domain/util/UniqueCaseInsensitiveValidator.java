/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.conditions.Where.where;

public class UniqueCaseInsensitiveValidator implements ConstraintValidator<UniqueCaseInsensitive, Object> {

    @Inject
    private DataModel dataModel;
    private String[] fields;

    @Override
    public void initialize(UniqueCaseInsensitive unique) {
        fields = unique.fields();
    }

    @Override
    public boolean isValid(Object in, ConstraintValidatorContext context) {
        DataMapper<?> mapper = dataModel.mapper(in.getClass());

        Condition condition = Condition.TRUE;
        for (String field : fields) {
            condition = condition.and(where(field).isEqualToIgnoreCase(mapper.getAttribute(in, field)));
        }
        boolean empty = mapper.select(condition == Condition.TRUE ? Condition.FALSE : condition).isEmpty();
        if (!empty && fields.length == 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(fields[0]).addConstraintViolation();
        }
        return empty;
    }
}
