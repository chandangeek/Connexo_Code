package com.elster.jupiter.orm;

import com.google.common.base.Optional;

public interface Reference<T> {
	T get();
	T orNull();
	T or(T defaultValue);
	void set(T value);
	Optional<T> getOptional();
	boolean isPresent();
}
