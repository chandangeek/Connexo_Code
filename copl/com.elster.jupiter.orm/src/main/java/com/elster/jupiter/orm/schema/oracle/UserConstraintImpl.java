/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingConstraint;

import com.google.common.base.CaseFormat;

import java.util.ArrayList;
import java.util.List;

public class UserConstraintImpl implements ExistingConstraint {

    private String name;
    private Type type;
    private String deleteRule;

    private Reference<UserTableImpl> table = ValueReference.absent();
    private Reference<UserConstraintImpl> referencedConstraint = ValueReference.absent();
    private List<UserConstraintColumnImpl> columns = new ArrayList<>();


    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasDefinition() {
        return type.needsCode();
    }

    @Override
    public String getType() {
        return type.getCodeName();
    }

    @Override
    public String getReferencedTableName() {
        return referencedConstraint.get().getTable().getName();
    }

    UserTableImpl getTable() {
        return table.get();
    }

    boolean contains(UserColumnImpl column) {
        if (type.needsCode()) {
            for (UserConstraintColumnImpl each : columns) {
                if (each.getColumnName().equals(column.getName())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }


    public void addTo(Table table) {
        type.buildOn(table, this);
    }

    private String getVariableName(String name) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    private enum Type {
        R(true, "foreignKey") {
            @Override
            void buildOn(Table<?> table, UserConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (ExistingColumn existingColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), existingColumn.getName());
                }
                table.foreignKey(constraint.getName()).on(columns).onDelete(constraint.getDeleteRule())
                        .references(constraint.referencedConstraint.get().getTable().getName())
                        .map(constraint.getVariableName(constraint.referencedConstraint.get().getTable().getName())).add();

            }
        },
        U(true, "unique") {
            @Override
            void buildOn(Table<?> table, UserConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (ExistingColumn existingColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), existingColumn.getName());
                }
                table.unique(constraint.getName()).on(columns).add();
            }
        },
        P(true, "primaryKey") {
            @Override
            void buildOn(Table<?> table, UserConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (ExistingColumn existingColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), existingColumn.getName());
                }
                table.primaryKey(constraint.getName()).on(columns).add();
            }
        },
        C(false) {
            @Override
            void buildOn(Table<?> table, UserConstraintImpl constraint) {
            }
        },
        O(false) {
            @Override
            void buildOn(Table<?> table, UserConstraintImpl constraint) {

            }
        },
        F(false) {
            @Override
            void buildOn(Table<?> table, UserConstraintImpl constraint) {

            }
        };

        final boolean needsCode;
        final String codeName;

        Type(boolean needsCode) {
            this(needsCode, null);
        }

        Type(boolean needsCode, String codeName) {
            this.needsCode = needsCode;
            this.codeName = codeName;
        }

        Column getColumn(List<? extends Column> columns, String name) {
            for (Column column : columns) {
                if (column.getName().equals(name)) {
                    return column;
                }
            }
            return null;
        }

        boolean needsCode() {
            return needsCode;
        }

        String getCodeName() {
            return codeName;
        }

        abstract void buildOn(Table<?> table, UserConstraintImpl constraint);
    }

    private DeleteRule getDeleteRule() {
        switch (deleteRule) {
            case "CASCADE":
                return DeleteRule.CASCADE;
            case "SET NULL":
                return DeleteRule.SETNULL;
            default:
                return DeleteRule.RESTRICT;
        }
    }

    public boolean isTableConstraint() {
        return type.needsCode();
    }

    public boolean isForeignKey() {
        return type == Type.R;
    }

    public boolean isPrimaryKey() {
        return type == Type.P;
    }

    public List<ExistingColumn> getColumns() {
        List<ExistingColumn> result = new ArrayList<>(columns.size());
        for (UserConstraintColumnImpl constraintColumn : columns) {
            result.add(table.get().getColumn(constraintColumn.getColumnName()));
        }
        return result;
    }

}
