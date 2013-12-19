package com.elster.jupiter.domain.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.elster.jupiter.orm.DataMapper;

public enum Save {
	CREATE(Create.class) {
		@Override
		<T> void doSave(DataMapper<T> mapper, T object) {
			mapper.persist(object);
		}
	},
	UPDATE(Update.class) {
		@Override
		<T> void doSave(DataMapper<T> mapper, T object) {
			mapper.update(object);
		}
	};
	
	private final Class<?> group;
	
	Save(Class<?> group) {
		this.group = group;
	}
	
	public final Class<?> group() {
		return group;
	}
	
	public final <T> void validate(DataMapper<T> mapper, T object , Class<?> ... groups) {
		Class<?>[] interfaces = new Class<?>[2 + groups.length];
		interfaces[0] = Default.class;
		interfaces[1] = group;
		for (int i = 0 ; i < groups.length ; i++) {
			interfaces[2+i] = groups[i];
		}
		/*
		Validator validator = mapper.getValidatorFactory().getValidator();
		Set<ConstraintViolation<T>> failures = validator.validate(object, interfaces);
		if (!failures.isEmpty()) {
			throw new ConstraintViolationException(failures);
		}
		*/
	}
	
	public final <T> void save(DataMapper<T> mapper , T object, Class<?> ... groups) {
		validate(mapper,object,groups);
		doSave(mapper,object);
	}
	
	abstract <T> void doSave(DataMapper<T> mapper, T object);

	public interface Create {
	}
	
	public interface Update {
	}
		
	public static Save action(long id) {
		return id == 0 ? CREATE : UPDATE;
	}
	
}
