/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public final class RefAnyImpl implements RefAny {

	private String component;
	private String table;
	private String key;
	@SuppressWarnings("unused")
	private long id;
	private Optional<?> targetHolder;

	private final OrmService ormService;
	private final JsonService jsonService;


	@Inject
	public RefAnyImpl(OrmService ormService , JsonService jsonService) {
		this.ormService = ormService;
		this.jsonService = jsonService;
	}

	public RefAnyImpl init(Object value, TableImpl<?> table) {
		this.component = table.getComponentName();
		this.table = table.getName();
		KeyValue primaryKey = table.getPrimaryKey(Objects.requireNonNull(value));
		key = jsonService.serialize(primaryKey.getKey());
		id = primaryKey.getId();
		targetHolder = Optional.of(value);
		return this;
	}

	public RefAnyImpl init(String component, String table, Object... keys) {
		this.component = component;
		this.table = table;
		this.key = jsonService.serialize(keys);
		String value = key.replace("[", "").replace("]", "");
		try {
			id = Long.parseLong(value.trim());
		} catch (NumberFormatException ex) {
			id = 0;
		}
		return this;
	}

	static RefAnyImpl from(DataModel dataModel, Object value , TableImpl<?> table) {
		return dataModel.getInstance(RefAnyImpl.class).init(value,table);
	}

	private OrmServiceImpl getOrmService() {
		return (OrmServiceImpl) ormService;
	}

	private Optional<?> getTargetHolder() {
		if (targetHolder == null) {
			if (component == null || table == null) {
				targetHolder = Optional.empty();
			} else {
				targetHolder = getOrmService().getDataModelImpl(component)
						.get()
						.getTable(table)
						.getOptional(getPrimaryKey());
			}
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

	@Override
	public Optional<?> getOptional() {
		return getTargetHolder();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (this.getClass() != other.getClass()) {
			return false;
		}

		RefAnyImpl o = (RefAnyImpl) other;
		return this.component.equals(o.component) && this.table.equals(o.table) && this.key.equals(o.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(component,table,key);
	}

	@Override
	public String getComponent() {
		return component;
	}

	@Override
	public String getTableName() {
		return table;
	}

	@Override
	public Object[] getPrimaryKey() {
		return jsonService.deserialize(key,Object[].class);
	}
}
