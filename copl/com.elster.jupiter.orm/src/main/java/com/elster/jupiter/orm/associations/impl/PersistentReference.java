package com.elster.jupiter.orm.associations.impl;


import java.util.Objects;
import java.util.Optional;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.KeyValue;

public class PersistentReference<T> implements Reference<T> {
	
	private final DataMapperImpl<T> dataMapper;
	private KeyValue primaryKey;
	private Optional<T> value;
	
	public PersistentReference(KeyValue primaryKey , DataMapperImpl<T> dataMapper) {
		this.primaryKey = primaryKey;
		this.dataMapper = dataMapper;
	}

	@Override
	public T get() {
		return getOptional().get();
	}
	
	@Override
	public T orNull() {
		return getOptional().orElse(null);
	}

	@Override
	public T or(T defaultValue) {
		return getOptional().orElse(defaultValue);
	}
	
	@Override
	public void set(T value) {
		this.value = Optional.ofNullable(value);
		primaryKey = dataMapper.getTable().getPrimaryKey(value);
		if (value != null && !isPresent()) {
			throw new IllegalArgumentException("Object " + value + " does not have a valid primary key");
		}
	}

	@Override
	public Optional<T> getOptional() {
		if (value == null) {
			if (isPresent()) {
				value = dataMapper.getOptional(primaryKey.getKey());
			} else {
				value = Optional.empty();
			} 
		}
		return value;
	}

	@Override
	public boolean isPresent() {
		if (primaryKey == null) {
			return false;
		} else  {
			if (dataMapper.getTable().getPrimaryKeyConstraint().allowZero()) {
				return !primaryKey.isNullAllowZero();
			} else {
				return !primaryKey.isNull();
			}
		}
	}
	
	public Object getKeyPart(int index) {
		return primaryKey.get(index);
	}
	
	public KeyValue getKey() {
		return primaryKey;
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
		return Objects.equals(this.orNull(),other.orNull());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(orNull());
	}

	@Override
	public void setNull() {
		set(null);
	}
	
	
 
}
