package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.PrimaryKeyConstraint;

public class PrimaryKeyConstraintImpl extends TableConstraintImpl implements PrimaryKeyConstraint {
	
	@Override
	PrimaryKeyConstraintImpl init(TableImpl<?> table, String name) {
		super.init(table,name);
		return this;
	}
	
	static PrimaryKeyConstraintImpl from(TableImpl<?> table , String name) {
		return new PrimaryKeyConstraintImpl().init(table,name);
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
		
		public BuilderImpl(TableImpl<?> table, String name) {
			this.constraint = PrimaryKeyConstraintImpl.from(table,name);
		}

		@Override
		public Builder on(Column... columns) {
			constraint.add(columns);
			return this;
		}

		@Override
		public PrimaryKeyConstraint add() {
			constraint.validate();
			constraint.getTable().add(constraint);
			return constraint;
		}
	
	}
}
