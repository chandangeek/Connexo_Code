package com.elster.jupiter.orm.impl;

import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.RefAny;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;

public final class RefAnyImpl implements RefAny {
	
	private String component;
	private String table;
	private String key;
	@SuppressWarnings("unused")
	private long id;
	
	private Optional<Object> targetHolder;
	
	@Inject
	private JsonService jsonService;
	@Inject
	private OrmService ormService;
	
	RefAnyImpl init(Object value, TableImpl table) {
		this.component = table.getComponentName();
		this.table = table.getName();
		Object[] primaryKey = table.getPrimaryKey(Objects.requireNonNull(value));
		key = jsonService.serialize(primaryKey);
		if (primaryKey.length == 1 && (Number.class.isInstance(primaryKey[0]))) {
			id = ((Number) primaryKey[0]).longValue();
		} else {
			id = 0;
		}
		targetHolder = Optional.of(value);
		return this;
	}	
	
	static RefAnyImpl from(DataModel dataModel, Object value , TableImpl table) {
		return dataModel.getInstance(RefAnyImpl.class).init(value,table);
	}
	
	private OrmServiceImpl getOrmService() {
		return (OrmServiceImpl) ormService;
	}
	
	private Optional<Object> getTargetHolder() {
		if (targetHolder == null) {
			Object[] primaryKey = jsonService.deserialize(key,Object[].class);
			targetHolder = getOrmService().getDataModel(component).get().getTable(table).getOptional(primaryKey);
		}
		return targetHolder;
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
