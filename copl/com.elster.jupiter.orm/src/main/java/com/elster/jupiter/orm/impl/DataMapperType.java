/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.inject.Injector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

abstract class DataMapperType<T> {
	private final TableImpl<T> table;
	
	DataMapperType(TableImpl<T> table) {
		this.table = table;
	}
	abstract boolean maps(Class<?> clazz);
	abstract Stream<Class<? extends T>> streamImplementations(List<Class<?>> fragments);
	abstract DomainMapper getDomainMapper();
	abstract boolean hasMultiple();
	abstract T newInstance();
	abstract T newInstance(String discriminator);
	abstract Class<?> getType(String fieldName);
	abstract String getDiscriminator(Class<?> clazz);
	abstract Field getField(String fieldName);
	abstract void addSqlFragment(List<SqlFragment> fragments , Class<? extends T> api, String alias);
	abstract Condition condition(Class<? extends T> api);
	abstract boolean needsRestriction(Class<? extends T> api);

	final Injector getInjector() {
		return table.getDataModel().getInjector();
	}
	
	final boolean isReference(String fieldName) {
		Class<?> clazz = getType(fieldName);
		return clazz == null ? false : Reference.class.isAssignableFrom(clazz); 
	}
	
	final TableImpl<T> getTable() {
		return table;
	}

}
