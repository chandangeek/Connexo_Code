package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.schema.oracle.UserConstraint;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class UserConstraintImpl implements UserConstraint {

    private String name;
    private Type type;

    private Reference<UserTableImpl> table = ValueReference.absent();
    private Reference<UserConstraintImpl> referencedConstraint = ValueReference.absent();
    private List<UserConstraintColumnImpl> columns = new ArrayList<>();


    @Override
    public String getName() {
        return name;
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

    void generate(List<String> code) {
        if (!type.needsCode()) {
            return;
        }
        String line = Joiner.on("").useForNull("null").join("\t\ttable.", type.getCodeName(), "(\"", name, "\").on(", getColumnVariables(), ")");
        if (type == Type.R) {
            String referencedTableName = referencedConstraint.get().getTable().getName();
            line = Joiner.on("").join(line, ".references(", referencedTableName, ".name()).map(\"", getVariableName(referencedTableName), "\")");
        }
        line += ".add();";
        code.add(line);
    }

    public void addTo(Table table) {
        type.buildOn(table, this);
    }

    private String getVariableName(String name) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    String getColumnVariables() {
        List<UserColumnImpl> cols = getColumns();
        String[] columnNames = new String[cols.size()];
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = cols.get(i).getVariableName();
        }
        return Joiner.on(",").join(columnNames);
    }

    private enum Type {
        R(true, "foreignKey") {
            @Override
            void buildOn(Table table, UserConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (UserColumnImpl userColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), userColumn.getName());
                }
                table.foreignKey(constraint.getName()).on(constraint.getColumns().toArray(columns))
                        .references(constraint.referencedConstraint.get().getTable().getName())
                        .map(constraint.getVariableName(constraint.referencedConstraint.get().getTable().getName())).add();

            }
        },
        U(true, "unique") {
            @Override
            void buildOn(Table table, UserConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (UserColumnImpl userColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), userColumn.getName());
                }
                table.unique(constraint.getName()).on(columns).add();
            }
        },
        P(true, "primaryKey") {
            @Override
            void buildOn(Table table, UserConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (UserColumnImpl userColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), userColumn.getName());
                }
                table.primaryKey(constraint.getName()).on(columns).add();
            }
        },
        C(false) {
            @Override
            void buildOn(Table table, UserConstraintImpl constraint) {
            }
        },
        O(false) {
            @Override
            void buildOn(Table table, UserConstraintImpl constraint) {

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

        Column getColumn(List<Column> columns, String name) {
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

        abstract void buildOn(Table table, UserConstraintImpl constraint);
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

    public List<UserColumnImpl> getColumns() {
        List<UserColumnImpl> result = new ArrayList<>(columns.size());
        for (UserConstraintColumnImpl constraintColumn : columns) {
            result.add(table.get().getColumn(constraintColumn.getColumnName()));
        }
        return result;
    }

}
