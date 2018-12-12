/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.schema.ExistingColumn;

import com.google.common.base.Joiner;

import java.sql.Types;
import java.util.List;

public class ColumnImpl implements ExistingColumn {

    private static final int MAX_ORACLE_VARCHAR_DATATYPE_SIZE = 4000;
    @SuppressWarnings("unused")
    private String tableName;
    private String name;
    @SuppressWarnings("unused")
    private int position;
    private int dataType;
    private int dataLength;
    private boolean nullable;
    private int characterLength;

    private Reference<TableImpl> table = ValueReference.absent();

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
            if (dataType == Types.DATE) {
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
            case Types.VARCHAR:
                column.varChar(Math.min(characterLength, MAX_ORACLE_VARCHAR_DATATYPE_SIZE));
                return;
            case Types.NUMERIC:
                column.number();
                return;
            case Types.DATE:
                column.type("DATE");
                return;
            default:
                column.type(toString(dataType));
                return;
        }
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    public boolean isAutoId() {
        if (!"ID".equals(name)) {
            return false;
        }
        List<ExistingColumn> primaryKeyColumns = table.get().getPrimaryKeyColumns();
        return primaryKeyColumns.size() == 1 && primaryKeyColumns.get(0).getName().equals("ID");
    }

    public boolean isForeignKeyPart() {
        for (ConstraintImpl constraint : table.get().getConstraints()) {
            if (constraint.isForeignKey() && constraint.contains(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConstraintPart() {
        for (ConstraintImpl constraint : table.get().getConstraints()) {
            if (constraint.isTableConstraint() && constraint.contains(this)) {
                return true;
            }
        }
        return false;
    }

    public String getTypeApi() {
        switch (dataType) {
            case Types.VARCHAR:
                return Joiner.on("").join("varChar(", dataLength, ")");
            case Types.NUMERIC:
                return "number()";
            case Types.DATE:
                return "type(\"DATE\")";
            default:
                return Joiner.on("").join("type(\"", toString(dataType), "(", dataLength, ")\")");
        }
    }

    private String toString(int type) {
        switch (type) {
            case 2003:
                return "ARRAY";
            case -5:
                return "BIGINT";
            case -2:
                return "BINARY";
            case -7:
                return "BIT";
            case 2004:
                return "BLOB";
            case 16:
                return "BOOLEAN";
            case 1:
                return "CHAR";
            case 2005:
                return "CLOB";
            case 70:
                return "DATALINK";
            case 91:
                return "DATE";
            case 3:
                return "DECIMAL";
            case 2001:
                return "DISTINCT";
            case 8:
                return "DOUBLE";
            case 6:
                return "FLOAT";
            case 4:
                return "INTEGER";
            case 2000:
                return "JAVA_OBJECT";
            case -16:
                return "LONGNVARCHAR";
            case -4:
                return "LONGVARBINARY";
            case -1:
                return "LONGVARCHAR";
            case -15:
                return "NCHAR";
            case 2011:
                return "NCLOB";
            case 0:
                return "NULL";
            case 2:
                return "NUMERIC";
            case -9:
                return "NVARCHAR";
            case 1111:
                return "OTHER";
            case 7:
                return "REAL";
            case 2006:
                return "REF";
            case -8:
                return "ROWID";
            case 5:
                return "SMALLINT";
            case 2009:
                return "SQLXML";
            case 2002:
                return "STRUCT";
            case 92:
                return "TIME";
            case 93:
                return "TIMESTAMP";
            case -6:
                return "TINYINT";
            case -3:
                return "VARBINARY";
            case 12:
                return "VARCHAR";
            default:
                throw new IllegalArgumentException();
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

}
