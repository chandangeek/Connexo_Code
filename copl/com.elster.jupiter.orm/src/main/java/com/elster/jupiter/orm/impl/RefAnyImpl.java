package com.elster.jupiter.orm.impl;

import java.util.Objects;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.RefAny;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.internal.Bus;
import com.google.common.base.Optional;

public final class RefAnyImpl implements RefAny {
	
	private final String component;
	private final String table;
	private final String key;
	@SuppressWarnings("unused")
	private final long id;
	
	private transient Optional<Object> targetHolder;
	
	RefAnyImpl() {
		this.component = null;
		this.table = null;
		this.key = null;
		this.id = 0;
	}
	
	private RefAnyImpl(Object value , Table table) {
		this.component = table.getComponentName();
		this.table = table.getName();
		Object[] primaryKey = table.getPrimaryKey(Objects.requireNonNull(value));
		key = Bus.getJsonService().serialize(primaryKey);
		if (primaryKey.length == 1 && (Number.class.isInstance(primaryKey[0]))) {
			id = ((Number) primaryKey[0]).longValue();
		} else {
			id = 0;
		}
		targetHolder = Optional.of(value);
	}
	
	private Optional<Object> getTargetHolder() {
		if (targetHolder == null) {
			Object[] primaryKey = Bus.getJsonService().deserialize(key,Object[].class);
			targetHolder = Bus.getLocator().getOrmService().getDataModel(component).get().getTable(table).get(primaryKey);
		}
		return targetHolder;
	}
	
	public static RefAnyImpl of(Object ref) {
		Class<?> clazz = Objects.requireNonNull(ref).getClass();
		for (DataModel dataModel : Bus.getLocator().getOrmService().getDataModels()) {
			Optional<Table> tableHolder = dataModel.getTable(clazz);
			if (tableHolder.isPresent()) {
				return new RefAnyImpl(ref,tableHolder.get());
			}
		} 
		throw new IllegalArgumentException("No table defined that maps " + ref.getClass());
	}
	
	@Override
	public String toString() {
		return 
			"Reference to tuple with primary key " + key + 
			" in table " + table + 
			" in component " + component;
	}

	@Override
	public boolean isPresent() {
		return getTargetHolder().isPresent();
	}

	@Override
	public Object get() {
		return getTargetHolder().get();
	}
}
