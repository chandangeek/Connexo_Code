package com.elster.jupiter.orm.associations.impl;


import java.util.Objects;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.google.common.base.Optional;

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
		return getOptional().orNull();
	}

	@Override
	public T or(T defaultValue) {
		return getOptional().or(defaultValue);
	}
	
	@Override
	public void set(T value) {
		this.value = Optional.fromNullable(value);
		primaryKey = dataMapper.getTable().getPrimaryKey(value);
	}

	@Override
	public Optional<T> getOptional() {
		if (value == null) {
			if (primaryKey.isNull()) {
				value = Optional.absent();
			} else {
				value = dataMapper.getOptional(primaryKey.getKey());
			}
		}
		return value;
	}

	@Override
	public boolean isPresent() {
		return primaryKey != null && !primaryKey.isNull();
	}
	
	public Object getKeyPart(int index) {
		return primaryKey.get(index);
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
 
}
