package com.elster.jupiter.orm.associations.impl;


import java.util.Objects;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.associations.Reference;
import com.google.common.base.Optional;

public class PersistentReference<T> implements Reference<T> {
	
	private final DataMapper<T> dataMapper;
	private Object[] primaryKey;
	private Optional<T> value;
	
	public PersistentReference(Object[] primaryKey , DataMapper<T> dataMapper) {
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
			for (Object keyPart : primaryKey) {
				if (keyPart == null) {
					value = Optional.absent();
					return value;
				}
			}
			value = dataMapper.get(primaryKey);
		}
		return value;
	}

	@Override
	public boolean isPresent() {
		return getOptional().isPresent();
	}
	
	public Object getKeyPart(int index) {
		return primaryKey[index];
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
