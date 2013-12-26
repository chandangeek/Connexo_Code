package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.UniqueConstraint;

public class UniqueConstraintImpl extends TableConstraintImpl implements UniqueConstraint {
	
	@Override
	UniqueConstraintImpl init(TableImpl<?> table,String name) {
		super.init(table, name);
		return this;
	}
	
	static UniqueConstraintImpl from(TableImpl<?> table , String name) {
		return (UniqueConstraintImpl) new UniqueConstraintImpl().init(table,name);
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
		
		public BuilderImpl(TableImpl<?> table, String name) {
			this.constraint = UniqueConstraintImpl.from(table,name);
		}

		@Override
		public Builder on(Column... columns) {
			constraint.add(columns);
			return this;
		}

		@Override
		public UniqueConstraint add() {
			constraint.validate();
			constraint.getTable().add(constraint);
			return constraint;
		}
	
	}
}
