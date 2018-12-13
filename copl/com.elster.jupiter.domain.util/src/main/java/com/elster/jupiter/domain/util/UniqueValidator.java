/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;
 
class UniqueValidator implements ConstraintValidator<Unique, Object> {
	@Inject
	private DataModel dataModel;
	private String[] fields;

	@Override
	public void initialize(Unique unique) {
		fields = unique.fields();
	}

	@Override
	public boolean isValid(Object in, ConstraintValidatorContext context) {
		DataMapper<?> mapper = dataModel.mapper(in.getClass());
		Map<String,Object> queryMap = new HashMap<>();
		for (String field : fields) {
			queryMap.put(field, mapper.getAttribute(in, field));
		}
        boolean empty = mapper.find(queryMap).isEmpty();
        if (!empty && fields.length == 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(fields[0]).addConstraintViolation();
        }
        return empty;
    }

}
