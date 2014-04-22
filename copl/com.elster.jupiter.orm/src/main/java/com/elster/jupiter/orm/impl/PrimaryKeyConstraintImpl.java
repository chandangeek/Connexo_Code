package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.PrimaryKeyConstraint;

import static com.elster.jupiter.util.Checks.is;

public class PrimaryKeyConstraintImpl extends TableConstraintImpl implements PrimaryKeyConstraint {

    private boolean allowZero;

    @Override
    PrimaryKeyConstraintImpl init(TableImpl<?> table, String name) {
        if (is(name).empty()) {
            throw new IllegalTableMappingException("Table " + table.getName() + " : primary key can not have an empty name.");
        }
        super.init(table, name);
        return this;
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


    }
}
