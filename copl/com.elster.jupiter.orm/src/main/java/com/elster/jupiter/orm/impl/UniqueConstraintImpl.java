/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.UniqueConstraint;
import com.elster.jupiter.orm.Version;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import java.util.Arrays;

import static com.elster.jupiter.util.Ranges.intersection;

public class UniqueConstraintImpl extends TableConstraintImpl<UniqueConstraintImpl> implements UniqueConstraint {

	private UniqueConstraintImpl predecessor;

	@Override
	UniqueConstraintImpl init(TableImpl<?> table,String name) {
		super.init(table, name);
		return this;
	}

	public UniqueConstraintImpl() {
		super(UniqueConstraintImpl.class);
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

    void setPredecessor(UniqueConstraint predecessor) {
        this.predecessor = (UniqueConstraintImpl) predecessor;
    }

    UniqueConstraintImpl getPredecessor() {
        return predecessor;
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

		@Override
		public UniqueConstraint.Builder since(Version version) {
			constraint.setVersions(intersection(constraint.getTable().getVersions(), ImmutableRangeSet.of(Range.atLeast(version))));
			return this;
		}

		@Override
		public UniqueConstraint.Builder upTo(Version version) {
			constraint.setVersions(intersection(constraint.getTable().getVersions(), ImmutableRangeSet.of(Range.lessThan(version))));
			return this;
		}

		@Override
		public UniqueConstraint.Builder during(Range... ranges) {
			ImmutableRangeSet.Builder<Version> builder = ImmutableRangeSet.builder();
			Arrays.stream(ranges)
					.forEach(builder::add);
			constraint.setVersions(intersection(constraint.getTable().getVersions(), builder.build()));
			return this;
		}

        @Override
        public UniqueConstraint.Builder previously(UniqueConstraint uniqueConstraint) {
            constraint.setPredecessor(uniqueConstraint);
            return this;
        }
	}
}
