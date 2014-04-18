package com.elster.jupiter.orm.impl;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TableDdlGenerator {
    private final TableImpl<?> table;
    private List<String> ddl;

    TableDdlGenerator(TableImpl<?> table) {
        this.table = table;
    }

    List<String> getDdl() {
        ddl = new ArrayList<>();
        ddl.add(getTableDdl());
        if (table.hasJournal()) {
            ddl.add(getJournalTableDdl(table));
        }
        for (TableConstraintImpl constraint : table.getConstraints()) {
            if (constraint.needsIndex()) {
                ddl.add(getConstraintIndexDdl(constraint));
            }
        }
        for (IndexImpl index : table.getIndexes()) {
            ddl.add(getIndexDdl(index));
        }
        for (ColumnImpl column : table.getColumns()) {
            if (column.isAutoIncrement()) {
                ddl.add(getSequenceDdl(column));
            }
        }

        return ddl;
    }

    private String getTableDdl() {
        StringBuilder sb = new StringBuilder("create table ");
        sb.append(table.getQualifiedName());
        sb.append("(");
        doAppendColumns(sb, table.getColumns(), true, true);
        for (TableConstraintImpl constraint : table.getConstraints()) {
            sb.append(", ");
            sb.append(getConstraintFragment(constraint));
        }
        sb.append(")");
        if (table.isIndexOrganized()) {
            sb.append(" index organized ");
        }
        return sb.toString();
    }

    private String getJournalTableDdl(TableImpl<?> table) {
        StringBuilder sb = new StringBuilder("create table ");
        sb.append(table.getQualifiedName(table.getJournalTableName()));
        sb.append(" (");
        doAppendColumns(sb, table.getColumns(), true, true);
        String separator = ", ";
        sb.append(separator);
        sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
        sb.append(" NUMBER NOT NULL");
        TableConstraintImpl constraint = table.getPrimaryKeyConstraint();
        if (constraint != null) {
            sb.append(separator);
            sb.append(getJournalConstraint(constraint));
        }
        sb.append(")");
        return sb.toString();
    }

    private String getJournalConstraint(TableConstraintImpl constraint) {
        StringBuilder sb = new StringBuilder("constraint ");
        sb.append(constraint.getName() + "_JRNL");
        sb.append(" PRIMARY KEY ");
        sb.append("(");
        doAppendColumns(sb, constraint.getColumns(), false, false);
        sb.append(", ");
        sb.append(table.getExtraJournalPrimaryKeyColumnName());
        sb.append(")");
        return sb.toString();
    }

    private String getConstraintFragment(TableConstraintImpl constraint) {
        return constraint.getDdl();
    }


    private String getConstraintIndexDdl(TableConstraintImpl constraint) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE INDEX ");
        builder.append(constraint.getName());
        builder.append(" ON ");
        builder.append(table.getQualifiedName());
        appendColumns(builder, constraint.getColumns(), false, false);
        return builder.toString();
    }

    private String getIndexDdl(IndexImpl index) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE INDEX ");
        builder.append(index.getName());
        builder.append(" ON ");
        builder.append(table.getQualifiedName());
        appendColumns(builder, index.getColumns(), false, false);
        if (index.getCompress() > 0) {
            builder.append(" COMPRESS ");
            builder.append(index.getCompress());
        }
        return builder.toString();
    }

    private String getDropConstraintIndexDdl(TableConstraintImpl constraint) {
        return "drop index " + constraint.getName();
    }

    private String getSequenceDdl(ColumnImpl column) {
        return getSequenceDdl(column, 1);
    }

    private String getSequenceDdl(ColumnImpl column, long startValue) {
        // cache 1000 for performance in RAC environments
        return "create sequence " + column.getQualifiedSequenceName() + " start with " + startValue + " cache 1000";
    }

    private String getDropSequenceDdl(ColumnImpl column) {
        return "drop sequence " + column.getQualifiedSequenceName();
    }

    private void appendColumns(StringBuilder builder, List<ColumnImpl> columns, boolean addType, boolean addNullable) {
        builder.append(" (");
        doAppendColumns(builder, columns, addType, addNullable);
        builder.append(") ");
    }

    private void doAppendColumns(StringBuilder builder, List<ColumnImpl> columns, boolean addType, boolean addNullable) {
        String separator = "";
        for (ColumnImpl column : columns) {
            builder.append(separator);
            appendDdl(column, builder, addType, addNullable);
            separator = ", ";
        }
    }

    private void appendDdl(ColumnImpl column, StringBuilder builder, boolean addType, boolean addNullable) {
        builder.append(column.getName());
        if (addType) {
            builder.append(" ");
            builder.append(column.getDbType());
            if (addNullable && column.isNotNull()) {
                builder.append(" NOT NULL");
            }
        }
    }

    public List<String> upgradeDdl(TableImpl<?> toTable) {
        List<String> result = new ArrayList<>();
        // Columns
        List<ColumnImpl> notMatched = new ArrayList<>();
        notMatched.addAll(table.getColumns());
        for (ColumnImpl toColumn : toTable.getColumns()) {
            ColumnImpl fromColumn = table.getColumn(toColumn.getName());
            if (fromColumn != null) {
                notMatched.remove(fromColumn);
                Optional<String> upgradeDdl = getUpgradeDdl(fromColumn, toColumn);
                result.addAll(upgradeDdl.asSet());
                if (toColumn.isAutoIncrement()) {
                    if (!fromColumn.isAutoIncrement()) {
                        result.add(getSequenceDdl(toColumn));
                    }
                }
            } else {
                result.add(getAddDdl(toColumn));
                if (toColumn.isAutoIncrement()) {
                    result.add(getSequenceDdl(toColumn));
                }
            }
        }

        for (ColumnImpl column : notMatched) {
            if (column.isNotNull()) {
                result.add(getSetNullColumnDdl(column));
            }
        }
        for (IndexImpl index : toTable.getIndexes()) {
            IndexImpl fromIndex = table.getIndex(index.getName());
            if (fromIndex == null) {
                result.add(getIndexDdl(index));
            } else {
                result.addAll(getUpgradeDdl(fromIndex, index));
            }
        }
        //Constraints
        Set<TableConstraintImpl> unmatched = new HashSet<>();
        for (TableConstraintImpl constraint : table.getConstraints()) {
            unmatched.add(constraint);
        }
        for (TableConstraintImpl toConstraint : toTable.getConstraints()) {

            TableConstraintImpl fromConstraint = table.getConstraint(toConstraint.getName());
            if (fromConstraint == null) {
                fromConstraint = table.findMatchingConstraint(toConstraint);
                if (fromConstraint == null) {
                    result.addAll(getAddConstraintDdls(toConstraint));

                } else {
                    unmatched.remove(fromConstraint);
                    result.add(getRenameConstraintDdl(fromConstraint, toConstraint));
                }
            } else {
                unmatched.remove(fromConstraint);
                result.addAll(getUpgradeDdl(fromConstraint, toConstraint));

            }
        }
        for (TableConstraintImpl each : unmatched) {
            result.addAll(getDropConstraintDdls(each));
        }

        if (toTable.hasJournal() && !table.hasJournal()) {
            result.add(getJournalTableDdl(toTable));
        }
        return result;
    }

    private String getRenameConstraintDdl(TableConstraintImpl fromConstraint, TableConstraintImpl toConstraint) {
        return "alter table " + fromConstraint.getTable().getName() + " rename constraint " + fromConstraint.getName() + " to " + toConstraint.getName();
    }

    private List<String> getAddConstraintDdls(TableConstraintImpl constraint) {
        List<String> result = new ArrayList<>();
        result.add("alter table " + constraint.getTable().getName() + " add " + getConstraintFragment(constraint));
        if (constraint.needsIndex()) {
            result.add(getConstraintIndexDdl(constraint));
        }
        return result;
    }

    private List<String> getDropConstraintDdls(TableConstraintImpl constraint) {
        List<String> result = new ArrayList<>();
        result.add("alter table " + constraint.getTable().getName() + " drop constraint " + constraint.getName());
        if (constraint.needsIndex()) {
            result.add(getDropConstraintIndexDdl(constraint));
        }
        return result;
    }

    private List<String> getUpgradeDdl(TableConstraintImpl fromConstraint, TableConstraintImpl toConstraint) {
        if (fromConstraint.matches(toConstraint)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        result.addAll(getDropConstraintDdls(fromConstraint));
        result.addAll(getAddConstraintDdls(toConstraint));


        return result;
    }

    private List<String> getUpgradeDdl(IndexImpl fromIndex, IndexImpl toIndex) {
        if (fromIndex.matches(toIndex)) {
            return Collections.emptyList();
        }
        return Arrays.asList(getDropIndex(fromIndex), getIndexDdl(toIndex));
    }

    private String getDropIndex(IndexImpl fromIndex) {
        return "DROP INDEX " + fromIndex.getName();
    }


    private String getAddDdl(ColumnImpl toColumn) {
        StringBuilder builder = new StringBuilder("alter table ");
        builder.append(table.getName());
        builder.append(" add ");
        appendDdl(toColumn, builder, true, true);
        return builder.toString();
    }

    private String getSetNullColumnDdl(ColumnImpl column) {
        StringBuilder builder = new StringBuilder("alter table ");
        builder.append(column.getTable().getName());
        builder.append(" modify ");
        appendDdl(column, builder, false, false);
        builder.append(" NULL ");
        return builder.toString();
    }


    private Optional<String> getUpgradeDdl(ColumnImpl fromColumn, ColumnImpl toColumn) {
        if (!fromColumn.getDbType().equalsIgnoreCase(toColumn.getDbType())
                || (fromColumn.isNotNull() != toColumn.isNotNull())) {
            StringBuilder builder = new StringBuilder("alter table ");
            builder.append(table.getName());
            builder.append(" modify ");
            appendDdl(toColumn, builder, true, fromColumn.isNotNull() != toColumn.isNotNull());
            return Optional.of(builder.toString());
        }
        return Optional.absent();
    }

    public List<String> upgradeSequenceDdl(ColumnImpl column, long startValue) {
        return Arrays.asList(getDropSequenceDdl(column), getSequenceDdl(column, startValue));
    }
}
