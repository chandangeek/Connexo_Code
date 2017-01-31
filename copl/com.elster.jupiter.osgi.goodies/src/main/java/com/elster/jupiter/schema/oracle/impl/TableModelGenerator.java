/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingConstraint;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class TableModelGenerator {

    private ExistingTable table;

    public TableModelGenerator(ExistingTable table) {
        this.table = table;
    }

    public List<String> generate() {
        List<String> code = new ArrayList<>();
        code.add(Joiner.on(" ").join(table.getName(), "{"));
        code.add("\t@Override");
        code.add("\tpublic void addTo(DataModel dataModel) {");
        code.add("\t\tTable<Api> table = dataModel.add(name(), Api.class);");
        code.add("\t\ttable.map(Implementation.class);");
        for (ExistingColumn column : table.getColumns()) {
            code.addAll(generate(column));
        }
        for (ExistingConstraint constraint : table.getConstraints()) {
            code.addAll(generate(constraint));
        }
        code.add("\t}");
        code.add("},");
        return code;
    }

    private List<String> generate(ExistingConstraint constraint) {
        List<String> code = new ArrayList<>();
        if (constraint.hasDefinition()) {
            String line = Joiner.on("").useForNull("null").join("\t\ttable.", constraint.getType(), "(\"", constraint.getName(), "\").on(", concatColumnNames(constraint), ")");
            if (constraint.isForeignKey()) {
                line = Joiner.on("").join(line, ".references(", constraint.getReferencedTableName(), ".name()).map(\"", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, constraint.getReferencedTableName()), "\")");
            }
            line += ".add();";
            code.add(line);
        }
        return code;
    }

    String concatColumnNames(ExistingConstraint constraint) {
        List<ExistingColumn> cols = constraint.getColumns();
        String[] columnNames = new String[cols.size()];
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = getColumnNameAsVar(cols.get(i));
        }
        return Joiner.on(",").join(columnNames);
    }

    private List<String> generate(ExistingColumn column) {
        List<String> code = new ArrayList<>();
        if (column.isAutoId()) {
            code.add(Joiner.on("").join("\t\tColumn ", getColumnNameAsVar(column), " = table.addAutoIdColumn();"));
        } else {
            code.add(Joiner.on("").join(lead(column), column.getName(), "\").", column.getTypeApi(), column.isNullable() ? "" : ".notNull()", lag(column)));
        }

        return code;
    }

    private String getColumnNameAsVar(ExistingColumn column) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column.getName());
    }

    private String lead(ExistingColumn column) {
        String result = "\t\t";
        if (column.isConstraintPart()) {
            result += "Column " + getColumnNameAsVar(column) + " = ";
        }
        return result + "table.column(\"";
    }

    private String lag(ExistingColumn column) {
        String result = ".";
        if (!column.isForeignKeyPart()) {
            result += "map(\"" + getColumnNameAsVar(column) + "\").";
        }
        if ("DATE".equals(column.getTypeApi())) {
            if ("CREDATE".equals(column.getName())) {
                result += "insertValue(\"SYSDATE\").skipOnUpdate().";
            }
            if ("MODDATE".equals(column.getName())) {
                result += "insertValue(\"SYSDATE\").updateValue(\"SYSDATE\").";
            }
        }
        return result += "add();";
    }
}
