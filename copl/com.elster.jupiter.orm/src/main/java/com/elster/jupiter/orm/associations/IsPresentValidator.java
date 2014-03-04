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
		if (in == null || !((in instanceof ValueReference) || (in instanceof Optional))) {
			return false;
		}
		if (in instanceof ValueReference) {
			return ((ValueReference<?>) in).isPresent();
		}
		if (in instanceof Optional) {
			return ((Optional<?>) in).isPresent();
		}
		throw new RuntimeException("Unreachable");
	}

}
