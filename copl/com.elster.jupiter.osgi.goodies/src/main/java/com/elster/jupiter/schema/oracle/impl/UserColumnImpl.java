package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.schema.oracle.UserColumn;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;

import java.util.List;

public class UserColumnImpl implements UserColumn {

    @SuppressWarnings("unused")
    private String tableName;
    private String name;
    @SuppressWarnings("unused")
    private int position;
    private String dataType;
    private int dataLength;
    private boolean nullable;
    private int characterLength;

    private Reference<UserTableImpl> table = ValueReference.absent();

    @Override
    public String getName() {
        return name;
    }

    void generate(List<String> code) {
        if (isAutoId()) {
            code.add(Joiner.on("").join("\t\tColumn ", getVariableName(), " = table.addAutoIdColumn();"));
        } else {
            code.add(Joiner.on("").join(lead(), name, "\").", getTypeApi(), nullable ? "" : ".notNull()", lag()));
        }
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
            if (dataType.equals("DATE")) {
                if (name.equals("CREDATE")) {
                    column.insert("SYSDATE").skipOnUpdate();
                }
                if (name.equals("MODDATE")) {
                    column.insert("SYSDATE").update("SYSDATE");
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
            default:
                column.type(dataType);
                return;
        }
    }


    private String lead() {
        String result = "\t\t";
        if (isConstraintPart()) {
            result += "Column " + getVariableName() + " = ";
        }
        return result + "table.column(\"";
    }

    private String lag() {
        String result = ".";
        if (!isForeignKeyPart()) {
            result += "map(\"" + getVariableName() + "\").";
        }
        if (dataType.equals("DATE")) {
            if (name.equals("CREDATE")) {
                result += "insertValue(\"SYSDATE\").skipOnUpdate().";
            }
            if (name.equals("MODDATE")) {
                result += "insertValue(\"SYSDATE\").updateValue(\"SYSDATE\").";
            }
        }
        return result += "add();";
    }

    String getVariableName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    boolean isAutoId() {
        if (!"ID".equals(name)) {
            return false;
        }
        List<UserColumnImpl> primaryKeyColumns = table.get().getPrimaryKeyColumns();
        return primaryKeyColumns.size() == 1 && primaryKeyColumns.get(0).getName().equals("ID");
    }

    boolean isForeignKeyPart() {
        for (UserConstraintImpl constraint : table.get().getConstraints()) {
            if (constraint.isForeignKey() && constraint.contains(this)) {
                return true;
            }
        }
        return false;
    }

    boolean isConstraintPart() {
        for (UserConstraintImpl constraint : table.get().getConstraints()) {
            if (constraint.isTableConstraint() && constraint.contains(this)) {
                return true;
            }
        }
        return false;
    }

    String getTypeApi() {
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
    public String toString() {
        return Joiner.on(" ").join("Column:", name, "type:", dataType + "(", dataLength + ")");
    }

}
