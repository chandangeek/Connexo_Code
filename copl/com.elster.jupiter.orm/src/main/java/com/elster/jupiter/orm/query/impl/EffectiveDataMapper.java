/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;


import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.impl.PersistentTemporalList;
import com.elster.jupiter.orm.associations.impl.PersistentTemporalReference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

class EffectiveDataMapper<T> extends AbstractChildDataMapper<T> {
	private SkipFetch skipFetch = SkipFetch.TRUEFORNOW;

	EffectiveDataMapper(DataMapperImpl<T> dataMapper, ForeignKeyConstraintImpl constraint, String alias) {
		super(dataMapper, constraint, alias);
	}

	@Override
	boolean hasField(String fieldName) {
		if ("interval".equals(fieldName) && skipFetch == SkipFetch.TRUEFORNOW) {
			skipFetch = isUniqueInTime() ? SkipFetch.FINALFALSE : SkipFetch.FALSEFORNOW;
		} else {
			if (skipFetch == SkipFetch.FALSEFORNOW) {
				skipFetch = SkipFetch.FINALTRUE;
			}
		}
		return super.hasField(fieldName);
	}

	@Override
	boolean skipFetch(boolean marked, boolean anyChildMarked) {
		if (skipFetch == SkipFetch.FINALFALSE) {
			return false;
		} else {
			return anyChildMarked || skipFetch.booleanValue();
		}
	}

	@Override
	boolean needsDistinct(boolean marked, boolean anyChildMarked) {
		return this.skipFetch(marked, anyChildMarked);
	}

	@Override
	void clearCache() {
		super.clearCache();
		skipFetch = SkipFetch.TRUEFORNOW;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	void completeFind(Instant effectiveDate) {
		super.completeFind(effectiveDate);
		for (Map.Entry<Object,List<T>> entry : getTargetCache().entrySet()) {
			Field field = getConstraint().reverseField(entry.getKey().getClass());
			Object temporalAspect;
			try {
				temporalAspect = field.get(entry.getKey());
			} catch (ReflectiveOperationException ex) {
				throw new MappingException(ex);
			}
			if (temporalAspect instanceof PersistentTemporalReference) {
				if (entry.getValue().size() > 1) {
					throw new NotUniqueException(getConstraint().getReverseFieldName());
				}
				((PersistentTemporalReference) temporalAspect).setPresent((Effectivity) entry.getValue().get(0));
				return;
			}
			if (temporalAspect instanceof PersistentTemporalList) {
				List values = entry.getValue();
				((PersistentTemporalList) temporalAspect).setCache(effectiveDate, values);
				return;
			}
			throw new IllegalStateException("Unknown Temporal class");
		}
	}

	@Override
	public boolean isReachable() {
		return getConstraint().getReverseFieldName() != null && !skipFetch.booleanValue();
	}

	private boolean isUniqueInTime() {
		Field field = getConstraint().getReferencedTable().getField(getConstraint().getReverseFieldName());
		return TemporalReference.class.isAssignableFrom(field.getType());
	}

	@Override
	boolean isChild() {
		return skipFetch != SkipFetch.FINALFALSE;
	}

	private enum SkipFetch {
		TRUEFORNOW(true),
		FINALTRUE(true),
		FALSEFORNOW(false),
		FINALFALSE(false);

		private boolean bool;

		SkipFetch(boolean bool) {
			this.bool = bool;
		}

		private boolean booleanValue() {
			return bool;
		}
	}
}
