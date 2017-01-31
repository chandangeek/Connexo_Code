/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

class ChildDataMapper<T> extends AbstractChildDataMapper<T> {

	ChildDataMapper(DataMapperImpl<T> dataMapper, ForeignKeyConstraintImpl constraint, String alias) {
		super(dataMapper, constraint, alias);
	}

	@Override
	void completeFind(Instant effectiveDate) {
		String fieldName = getConstraint().getReverseFieldName();
		if (fieldName != null) {
			super.completeFind(effectiveDate);
			for (Map.Entry<Object,List<T>> entry : getTargetCache().entrySet()) {
				if (getConstraint().isOneToOne()) {
					if (entry.getValue().size() > 1) {
						throw new NotUniqueException(fieldName);
					}
					if (entry.getValue().size() == 1) {
						getConstraint().setReverseField(entry.getKey(), entry.getValue().get(0));
					}
				} else {
					sort(entry.getValue());
					List<T> values = entry.getValue();
					getConstraint().setReverseField(entry.getKey(), values);
				}
			}
		}
	}

	private void sort(List<?> list) {
		String fieldName = getConstraint().getReverseOrderFieldName();
		if (fieldName != null) {
			Collections.sort(list, new FieldComparator(fieldName));
		}
	}


	private static class FieldComparator implements Comparator<Object> {
		private final String sortField;

		FieldComparator(String sortField) {
			this.sortField = sortField;
		}

		@Override
		public int compare(Object o1, Object o2) {
			return getValue(o1).compareTo(getValue(o2));
		}

		@SuppressWarnings({ "unchecked" })
		private Comparable<Object> getValue(Object o) {
			return (Comparable<Object>) DomainMapper.FIELDSTRICT.get(o, sortField);
		}

	}

	@Override
	public boolean isReachable() {
		return getConstraint().getReverseFieldName() != null;
	}

	@Override
	boolean isChild() {
		return !getConstraint().isOneToOne();
	}

    @Override
	boolean skipFetch(boolean marked, boolean anyChildMarked) {
		return isChild() && (marked || anyChildMarked);
	}

    @Override
	boolean needsDistinct(boolean marked, boolean anyChildMarked) {
        return this.skipFetch(marked, anyChildMarked);
	}

}