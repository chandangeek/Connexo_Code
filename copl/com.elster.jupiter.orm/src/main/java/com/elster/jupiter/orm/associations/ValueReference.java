package com.elster.jupiter.orm.associations;

import java.util.Objects;

import com.google.common.base.Optional;

public final class ValueReference<T> implements Reference<T> {
	
	private T value;

	private ValueReference(T value) {
		this.value = value;
	}
	
	public static <T> Reference<T> of(T value) {
		return new ValueReference<>(Objects.requireNonNull(value));
	}
	
	public static <T> Reference<T> fromNullable(T value) {
		return new ValueReference<>(value);
	}
	
	@Override
	public T get() {
		return Objects.requireNonNull(value);
	}
	
	@Override
	public T orNull() {
		return value;
	}
	
	@Override
	public T or(T defaultValue) {
		return value == null ? defaultValue : value;
	}
 
	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public Optional<T> getOptional() {
		return Optional.fromNullable(value);
	}

	@Override
	public boolean isPresent() {
		return value != null;
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
	
}
