/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.schema.ExistingColumn;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.util.List;

public class UserColumnImpl implements ExistingColumn {

    @SuppressWarnings("unused")
    private String tableName;
    private String name;
    @SuppressWarnings("unused")
    private int position;
    private String dataType;
    private int dataLength;
    private boolean nullable;
    private int characterLength;
    private String dataDefault;
    private String virtual;
    private String hidden;

    private Reference<UserTableImpl> table = ValueReference.absent();

    @Override
    public String getName() {
        return name;
    }

    public void addTo(Table table) {
        if (isAutoId()) {
            table.addAutoIdColumn();
        } else {
            Column.Builder column = table.column(name);
            callTypeApi(column);
            if (!nullable) {
                column.notNull();
            }
            if ("DATE".equals(dataType)) {
                if ("CREDATE".equals(name)) {
                    column.insert("SYSDATE").skipOnUpdate();
                }
                if ("MODDATE".equals(name)) {
                    column.insert("SYSDATE").update("SYSDATE");
                }
            }
            if (!Strings.isNullOrEmpty(dataDefault)) {
                String formula = dataDefault.replaceAll("\"", "");
                column.insert(formula);
                column.update(formula);
                if (isVirtual()) {
                    column.as(formula);
                }
            }
            column.add();

        }
    }

    private void callTypeApi(Column.Builder column) {
        switch (dataType) {
            case "VARCHAR2":
                column.varChar(characterLength);
                return;
            case "NUMBER":
                column.number();
                return;
            case "DATE":
                column.type("DATE");
                return;
            case "CHAR":
                column.type("CHAR(" + dataLength + ")");
                return;
            default:
                column.type(dataType);
                return;
        }
    }


    public boolean isAutoId() {
        if (!"ID".equals(name)) {
            return false;
        }
        List<ExistingColumn> primaryKeyColumns = table.get().getPrimaryKeyColumns();
        return primaryKeyColumns.size() == 1 && primaryKeyColumns.get(0).getName().equals("ID");
    }

    public boolean isForeignKeyPart() {
        for (UserConstraintImpl constraint : table.get().getConstraints()) {
            if (constraint.isForeignKey() && constraint.contains(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConstraintPart() {
        for (UserConstraintImpl constraint : table.get().getConstraints()) {
            if (constraint.isTableConstraint() && constraint.contains(this)) {
                return true;
            }
        }
        return false;
    }

    public String getTypeApi() {
        switch (dataType) {
            case "VARCHAR2":
                return Joiner.on("").join("varChar(", dataLength, ")");
            case "NUMBER":
                return "number()";
            case "DATE":
                return "type(\"DATE\")";
            default:
                return Joiner.on("").join("type(\"", dataType, "(", dataLength, ")\")");
        }
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public String toString() {
        return Joiner.on(" ").join("Column:", name, "type:", dataType + "(", dataLength + ")");
    }

    @Override
    public boolean isVirtual() {
        return "YES".equalsIgnoreCase(virtual);
    }

    public boolean isHidden() {
        return "YES".equalsIgnoreCase(hidden);
    }
}
