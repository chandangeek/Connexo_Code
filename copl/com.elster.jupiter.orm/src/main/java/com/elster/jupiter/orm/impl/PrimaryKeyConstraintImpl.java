/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Version;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import java.util.Arrays;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.Ranges.intersection;

public class PrimaryKeyConstraintImpl extends TableConstraintImpl<PrimaryKeyConstraintImpl> implements PrimaryKeyConstraint {

    private boolean allowZero;
    private PrimaryKeyConstraintImpl predecessor;

    @Override
    PrimaryKeyConstraintImpl init(TableImpl<?> table, String name) {
        if (is(name).empty()) {
            throw new IllegalTableMappingException("Table " + table.getName() + " : primary key can not have an empty name.");
        }
        super.init(table, name);
        return this;
    }

    public PrimaryKeyConstraintImpl() {
        super(PrimaryKeyConstraintImpl.class);
    }

    static PrimaryKeyConstraintImpl from(TableImpl<?> table, String name) {
        return new PrimaryKeyConstraintImpl().init(table, name);
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    public String getTypeString() {
        return "primary key";
    }

    @Override
    public boolean allowZero() {
        return allowZero;
    }
    
    @Override
    void appendDdlTrailer(StringBuilder builder) {
        getTable().partitionColumn()
        	.filter( column -> getColumns().contains(column))
        	.ifPresent( column -> builder.append(" USING INDEX LOCAL "));
    }

    void setPredecessor(PrimaryKeyConstraint predecessor) {
        this.predecessor = (PrimaryKeyConstraintImpl) predecessor;
    }

    static class BuilderImpl implements PrimaryKeyConstraint.Builder {
        private final PrimaryKeyConstraintImpl constraint;

        public BuilderImpl(TableImpl<?> table, String name) {
            this.constraint = PrimaryKeyConstraintImpl.from(table, name);
        }

        @Override
        public Builder on(Column... columns) {
            for (Column column : columns) {
                if (!constraint.getTable().equals(column.getTable())) {
                    throw new IllegalTableMappingException("Table " + constraint.getTable().getName() + " : primary key can not have columns from another table : " + column.getName() + ".");
                }
                if (!column.isNotNull()) {
                    throw new IllegalTableMappingException("Table " + constraint.getTable().getName() + " : primary key cannot be put on nullable column : " + column.getName() + ".");
                }
            }
            constraint.add(columns);
            return this;
        }

        @Override
        public Builder allowZero() {
            this.constraint.allowZero = true;
            return this;
        }

        @Override
        public PrimaryKeyConstraint add() {
            constraint.validate();
            constraint.getTable().add(constraint);
            return constraint;
        }

        @Override
        public Builder since(Version version) {
            constraint.setVersions(intersection(constraint.getTable().getVersions(), ImmutableRangeSet.of(Range.atLeast(version))));
            return this;
        }

        @Override
        public Builder upTo(Version version) {
            constraint.setVersions(intersection(constraint.getTable().getVersions(), ImmutableRangeSet.of(Range.lessThan(version))));
            return this;
        }

        @Override
        public Builder during(Range... ranges) {
            ImmutableRangeSet.Builder<Version> builder = ImmutableRangeSet.builder();
            Arrays.stream(ranges)
                    .forEach(builder::add);
            constraint.setVersions(intersection(constraint.getTable().getVersions(), builder.build()));
            return this;
        }

        @Override
        public Builder previously(PrimaryKeyConstraint primaryKeyConstraint) {
            constraint.setPredecessor(primaryKeyConstraint);
            return this;
        }

    }
}
