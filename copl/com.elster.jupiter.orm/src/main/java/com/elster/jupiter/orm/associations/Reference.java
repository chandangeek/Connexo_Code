package com.elster.jupiter.orm.associations;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


public interface Reference<T> {
	Optional<T> getOptional();
	void set(T value);
	void setNull();
	
	// guava Optional protocol compatibility
	default T orNull() {
		return orElse(null);
	}
	default T or(T defaultValue) {
		return orElse(defaultValue);
	}
	
	// java.util.Optional protocol
	default boolean isPresent() {
		return getOptional().isPresent();
	}
	default T get() {
		return getOptional().get();
	}
	default T orElse(T ersatz) {
		return getOptional().orElse(ersatz);
	}
	default T orElseGet(Supplier<T> supplier) {
		return getOptional().orElseGet(supplier);
	}
	default <X extends Throwable> T orElseThrow(Supplier<? extends X> supplier) throws X {
		return getOptional().orElseThrow(supplier);
	}
	
	default <U> Optional<U> map(Function<? super T, ? extends U> function) {
		return getOptional().map(function);
	}
	default <U> Optional<U> flatMap(Function<? super T, Optional<U>> function) {
		return getOptional().flatMap(function);
	}
	default Optional<T> filter(Predicate<T> predicate) {
		return getOptional().filter(predicate);
	}
}
