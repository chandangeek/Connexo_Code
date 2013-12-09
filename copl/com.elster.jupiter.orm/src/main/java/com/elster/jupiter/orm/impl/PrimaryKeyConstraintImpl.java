package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;

public class PrimaryKeyConstraintImpl extends TableConstraintImpl implements PrimaryKeyConstraint {
	
	@SuppressWarnings("unused")
	private PrimaryKeyConstraintImpl() {
	}
	
	PrimaryKeyConstraintImpl(Table table , String name) {
		super(table,name);
	}
	
	@Override
	public boolean isPrimaryKey() {
		return true;
	}

	@Override
	public String getTypeString() {
		return "primary key";
	}
	
	static class BuilderImpl implements PrimaryKeyConstraint.Builder {
		private final PrimaryKeyConstraintImpl constraint;
		
		public BuilderImpl(Table table, String name) {
			this.constraint = new PrimaryKeyConstraintImpl(table,name);
		}

		@Override
		public Builder on(Column... columns) {
			constraint.add(columns);
			return this;
		}

		@Override
		public PrimaryKeyConstraint add() {
			constraint.validate();
			((TableImpl) constraint.getTable()).add(constraint);
			return constraint;
		}
	
	}
}
