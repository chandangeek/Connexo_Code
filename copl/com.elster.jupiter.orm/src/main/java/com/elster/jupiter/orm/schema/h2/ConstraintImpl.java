/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingConstraint;

import com.google.common.base.CaseFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstraintImpl implements ExistingConstraint {

    private static final Pattern REF_PATTERN = Pattern.compile(".* REFERENCES (?:\\w+\\.)?(\\w+)\\(.*");
    private String name;
    private String typeName;
    private Type type;
    private String uniqueIndexName;

    private Reference<TableImpl> table = ValueReference.absent();
    private String referencedIndex;
    private String sql;
    private List<ConstraintColumnImpl> columns = new ArrayList<>();


    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasDefinition() {
        return type().needsCode();
    }

    @Override
    public String getType() {
        return type().getCodeName();
    }

    @Override
    public String getReferencedTableName() {
        if (Type.R.equals(type)) {
            Matcher matcher = REF_PATTERN.matcher(sql);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return table.get().getName();
    }

    TableImpl getTable() {
        return table.get();
    }

    boolean contains(ColumnImpl column) {
        if (type().needsCode()) {
            for (ConstraintColumnImpl each : columns) {
                if (each.getColumnName().equals(column.getName())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private Type type() {
        if (type == null) {
            type = Type.forString(typeName).orElse(null);
        }
        return type;
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
            void buildOn(Table<?> table, ConstraintImpl constraint) {
                Column[] columns = new Column[constraint.getColumns().size()];
                int i = 0;
                for (ExistingColumn existingColumn : constraint.getColumns()) {
                    columns[i++] = getColumn(table.getColumns(), existingColumn.getName());
                }
                table.foreignKey(constraint.getName()).on(columns)
                        .references(constraint.getReferencedTableName())
                        .map(constraint.getVariableName(constraint.getReferencedTableName())).add();

            }
        },
        U(true, "unique") {
            @Override
            void buildOn(Table<?> table, ConstraintImpl constraint) {
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
            void buildOn(Table<?> table, ConstraintImpl constraint) {
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
            void buildOn(Table<?> table, ConstraintImpl constraint) {
            }
        },
        O(false) {
            @Override
            void buildOn(Table<?> table, ConstraintImpl constraint) {

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

        static Optional<Type> forString(String typeName) {
            switch (typeName) {
                case "REFERENTIAL":
                    return Optional.of(R);
                case "PRIMARY KEY":
                    return Optional.of(P);
                case "UNIQUE":
                    return Optional.of(U);
                default:
                    return Optional.empty();
            }
        }

        abstract void buildOn(Table<?> table, ConstraintImpl constraint);
    }

    public boolean isTableConstraint() {
        return type().needsCode();
    }

    public boolean isForeignKey() {
        return type() == Type.R;
    }

    public boolean isPrimaryKey() {
        return type() == Type.P;
    }

    public boolean isUniqueConstraint() {
        return type() == Type.U;
    }

    public List<ExistingColumn> getColumns() {
        if (isPrimaryKey() || isUniqueConstraint()) {
            return getTable().getIndexColumns(uniqueIndexName);
        }
        List<ExistingColumn> result = new ArrayList<>(columns.size());
        for (ConstraintColumnImpl constraintColumn : columns) {
            result.add(table.get().getColumn(constraintColumn.getColumnName()));
        }
        return result;
    }

}
