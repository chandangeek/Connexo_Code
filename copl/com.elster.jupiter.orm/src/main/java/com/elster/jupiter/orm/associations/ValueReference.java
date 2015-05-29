package com.elster.jupiter.orm.associations;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

/*
 * used to initialize a Reference
 */

public final class ValueReference<T> implements Reference<T> {

	@Valid
	private T value;

	ValueReference() {
		this.value = null;
	}
	
	public static <T> Reference<T> absent() {
		return new ValueReference<>();
	}
 
	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public Optional<T> getOptional() {
		return Optional.ofNullable(value);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Reference)) {
			return false;
		}
		Reference<?> other  = (Reference<?>) o;
		return Objects.equals(value,other.orNull());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}

	@Override
	public void setNull() {
		value = null;
	}	
}
