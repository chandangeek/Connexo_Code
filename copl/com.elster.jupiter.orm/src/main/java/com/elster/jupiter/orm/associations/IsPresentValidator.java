package com.elster.jupiter.orm.associations;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

 
class IsPresentValidator implements ConstraintValidator<IsPresent, Object> {
	
	@Override
	public void initialize(IsPresent isPresent) {
	}

	@Override
	public boolean isValid(Object in, ConstraintValidatorContext context) {  
		if (in instanceof Reference) {
			return ((Reference<?>) in).isPresent();
		}
		if (in instanceof Optional) {
			return ((Optional<?>) in).isPresent();
		}
		return false;
	}

}
