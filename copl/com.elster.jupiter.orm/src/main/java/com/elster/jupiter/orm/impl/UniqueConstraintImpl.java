package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UniqueConstraint;

public class UniqueConstraintImpl extends TableConstraintImpl implements UniqueConstraint {
	
	@SuppressWarnings("unused")
	private UniqueConstraintImpl() {
	}
	
	UniqueConstraintImpl(Table table , String name) {
		super(table,name);
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	String getTypeString() {
		return "unique";
	}
	
	static class BuilderImpl implements UniqueConstraint.Builder {
		private final UniqueConstraintImpl constraint;
		
		public BuilderImpl(Table table, String name) {
			this.constraint = new UniqueConstraintImpl(table,name);
		}

		@Override
		public Builder on(Column... columns) {
			constraint.add(columns);
			return this;
		}

		@Override
		public UniqueConstraint add() {
			constraint.validate();
			((TableImpl) constraint.getTable()).add(constraint);
			return constraint;
		}
	
	}
}
