package com.elster.jupiter.orm.associations;

import java.util.Optional;


public interface Reference<T> {
	T get();
	T orNull();
	T or(T defaultValue);
	void set(T value);
	Optional<T> getOptional();
	boolean isPresent();
	void setNull(); 
}
