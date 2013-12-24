package com.elster.jupiter.domain.util;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
 
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
		return mapper.find(queryMap).isEmpty();
	}

}
