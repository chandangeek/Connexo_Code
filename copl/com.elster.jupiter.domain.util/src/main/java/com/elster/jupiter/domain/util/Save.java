package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataModel;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

public enum Save {
	CREATE(Create.class) {
		@Override
		<T> void doSave(DataModel dataModel, T object) {
			dataModel.persist(object);
		}
	},
	UPDATE(Update.class) {
		@Override
		<T> void doSave(DataModel dataModel, T object) {
			dataModel.update(object);
		}
	};
	
	private final Class<?> group;
	
	Save(Class<?> group) {
		this.group = group;
	}
	
	public final Class<?> group() {
		return group;
	}
	
	public final <T> void validate(Validator validator, T object , Class<?> ... groups) {
		Class<?>[] interfaces = new Class<?>[2 + groups.length];
		interfaces[0] = Default.class;
		interfaces[1] = group;
		System.arraycopy(groups, 0, interfaces, 2, groups.length);
		Set<ConstraintViolation<T>> failures = validator.validate(object, interfaces);
		if (!failures.isEmpty()) {
			throw new ConstraintViolationException(failures);
		}
	}
	
	public final <T> void save(DataModel dataModel, T object, Class<?> ... groups) {
		validate(dataModel.getValidatorFactory().getValidator(),object,groups);
		doSave(dataModel,object);
	}
	
	abstract <T> void doSave(DataModel dataModel, T object);

	public interface Create {
	}
	
	public interface Update {
	}

	public static Save action(long id) {
		return id == 0 ? CREATE : UPDATE;
	}

}
