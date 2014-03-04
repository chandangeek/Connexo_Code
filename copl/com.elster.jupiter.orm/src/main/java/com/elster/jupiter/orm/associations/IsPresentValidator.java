package com.elster.jupiter.orm.associations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.base.Optional;
 
class IsPresentValidator implements ConstraintValidator<IsPresent, Object> {
	
	@Override
	public void initialize(IsPresent isPresent) {
	}

	@Override
	public boolean isValid(Object in, ConstraintValidatorContext context) {  
		if (in == null) {
			return false;
		}
		if (in instanceof ValueReference) {
			return ((ValueReference<?>) in).isPresent();
		}
		if (in instanceof Optional) {
			return ((Optional<?>) in).isPresent();
		}
		return false;
	}

}
