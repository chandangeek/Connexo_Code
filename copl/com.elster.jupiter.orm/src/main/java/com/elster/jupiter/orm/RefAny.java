package com.elster.jupiter.orm;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Optional;

public final class RefAny {
	
	private static final AtomicReference<OrmService> ormServiceHolder = new AtomicReference<>();
	
	private final String component;
	private final String table;
	private final String key;
	@SuppressWarnings("unused")
	private final long id;
	
	private transient Optional<Object> targetHolder;
	
	private RefAny() {
		this.component = null;
		this.table = null;
		this.key = null;
		this.id = 0;
	}
	
	private RefAny(Object value , Table table) {
		this.component = table.getComponentName();
		this.table = table.getName();
		Object[] primaryKey = table.getPrimaryKey(Objects.requireNonNull(value));
		key = getOrmService().serialize(primaryKey);
		if (primaryKey.length == 1 && (Number.class.isInstance(primaryKey[0]))) {
			id = ((Number) primaryKey[0]).longValue();
		} else {
			id = 0;
		}
	}
	
	public static void setOrmService(OrmService ormService) {
		ormServiceHolder.set(Objects.requireNonNull(ormService));
	}
	
	public static void clearOrmService(OrmService old) {
		ormServiceHolder.compareAndSet(Objects.requireNonNull(old), null);
	}
	
	public static OrmService getOrmService() {
		return ormServiceHolder.get();
	}
	
	public Optional<Object> get() {
		if (targetHolder == null) {
			Object[] primaryKey = getOrmService().deserialize(key);
			targetHolder = getOrmService().getDataModel(component).get().getTable(table).get(primaryKey);
		}
		return targetHolder;
	}
	
	public static RefAny of(Object ref) {
		Optional<Table> tableHolder = getOrmService().getTable(Objects.requireNonNull(ref).getClass());
		if (tableHolder.isPresent()) {
			return new RefAny(ref,tableHolder.get());
		} else {
			throw new IllegalArgumentException("No table defined that maps " + ref.getClass());
		}
	}
	
	@Override
	public String toString() {
		return 
			"Reference to tuple with primary key " + key + 
			" in table " + table + 
			" in component " + component;
	}
}
