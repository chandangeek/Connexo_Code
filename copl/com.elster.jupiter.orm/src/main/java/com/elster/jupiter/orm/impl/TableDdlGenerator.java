package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.util.streams.Functions;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

class TableDdlGenerator implements PartitionMethod.Visitor {

	private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final TableImpl<?> table;
    private final SqlDialect dialect;
    private final Version version; // TODO : actually use it

    TableDdlGenerator(TableImpl<?> table, SqlDialect dialect, Version version) {
        this.table = table;
        this.dialect = dialect;
        this.version = version;
    }

    List<String> getDdl() {
        List<String> ddl = new ArrayList<>();
        ddl.add(getTableDdl());
        if (table.hasJournal(this.version)) {
            ddl.add(getJournalTableDdl(table));
        }
        for (TableConstraintImpl constraint : table.getConstraints(version)) {
            if (constraint.needsIndex()) {
                ddl.add(getConstraintIndexDdl(constraint));
            }
        }
        for (IndexImpl index : table.getIndexes(version)) {
            ddl.add(getIndexDdl(index));
        }
        for (ColumnImpl column : table.getColumns(version)) {
            if (column.isAutoIncrement()) {
                ddl.add(getSequenceDdl(column));
            }
        }
        for (TableImpl<?> created : table.getDataModel().getTables(version)) {
        	if (created.equals(table)) {
        		break;
        	}
        	for (ForeignKeyConstraintImpl constraint : created.getForeignKeyConstraints(version)) {
        		if (constraint.getReferencedTable().equals(table) && !constraint.noDdl()) {
        			ddl.add("alter table " + constraint.getTable().getName() + " add " + getConstraintFragment(constraint));
        		}
        	}
        }
        return ddl;
    }

    private String getTableDdl() {
        StringBuilder sb = new StringBuilder("create table ");
        sb.append(table.getQualifiedName(version));
        sb.append("(");
        doAppendColumns(sb, table.getColumns(version), true, true);
        for (TableConstraintImpl constraint : table.getConstraints(version)) {
        	if (!constraint.delayDdl() && !constraint.noDdl()) {
        		sb.append(", ");
        		sb.append(getConstraintFragment(constraint));
        	}
        }
        sb.append(")");
        if (table.isIndexOrganized() && dialect.hasIndexOrganizedTables()) {
            sb.append(" organization index");
            if (table.getIotCompressCount() > 0) {
            	sb.append(" compress ");
            	sb.append(table.getIotCompressCount());
            }
        }
        if (dialect.hasPartitioning()) {
        	table.getPartitionMethod().visit(this,sb);
        }
        return sb.toString();
    }

    @Override
    public void visitInterval(StringBuilder sb) {
    	sb.append(" partition by range(");
		sb.append(table.partitionColumn().get().getName());
		sb.append(") interval (");
		sb.append(PARTITIONSIZE);
		sb.append(") (partition P0 values less than(0))");
	}

    @Override
    public void visitReference(StringBuilder sb) {
    	sb.append(" partition by reference(");
    	sb.append(table.refPartitionConstraint().get().getName());
    	sb.append(")");
    }

    @Override
    public void visitRange(StringBuilder sb) {
    	sb.append(" partition by range(");
		sb.append(table.partitionColumn().get().getName());
		sb.append(") ");
		long end = (System.currentTimeMillis() / PARTITIONSIZE) * PARTITIONSIZE;
		String separator = "(";
		for (int i = 0 ; i < 12 ; i++) {
			end += PARTITIONSIZE;
			String name = "P" + Instant.ofEpochMilli(end).toString().replaceAll("-","").substring(0,8);
			sb.append(separator);
			sb.append(" partition ");
			sb.append(name);
			sb.append(" values less than(");
			sb.append(end);
			sb.append(")");
			separator = ",";
		}
		sb.append(")");
    }

    private String getJournalTableDdl(TableImpl<?> table) {
        StringBuilder sb = new StringBuilder("create table ");
        sb.append(table.getQualifiedName(table.getJournalTableName(this.version)));
        sb.append(" (");
        doAppendColumns(sb, table.getColumns(version), true, true);
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
        if (dialect.hasPartitioning()) {
        	sb.append("partition by range(");
        	sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
        	sb.append(") interval (");
        	sb.append(86400L * 1000L * 30L);
        	sb.append(") (partition P0 values less than(0))");
        }
        return sb.toString();
    }

    private String getJournalConstraint(TableConstraintImpl constraint) {
        StringBuilder sb = new StringBuilder("constraint ");
        sb.append(constraint.getName()).append("_JRNL");
        sb.append(" PRIMARY KEY ");
        sb.append("(");
        doAppendColumns(sb, constraint.getColumns(), false, false);
        sb.append(", ");
        sb.append(table.getExtraJournalPrimaryKeyColumnNames());
        sb.append(")");
        if (dialect.hasPartitioning()) {
        	sb.append(" USING INDEX LOCAL");
        }
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
        if (dialect.hasIndexCompression() && index.getCompress() > 0) {
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
        builder.append("\"").append(column.getName().toUpperCase()).append("\"");
        if (addType) {
            builder.append(" ");
            builder.append(column.getDbType() == null ? "" : column.getDbType());
            if (column.isVirtual()) {
                builder.append(" AS (");
                builder.append(column.getFormula());
                builder.append(")");
            }
            if (addNullable) {
                if (column.isNotNull()) {
                    if (column.getInstallValue() != null) {
                        builder.append(" DEFAULT ").append(column.getInstallValue());
                    }
                    builder.append(" NOT");
                }
                builder.append(" NULL");
            }
        }
    }

    List<String> upgradeDdl(TableImpl<?> toTable) {
        List<String> result = new ArrayList<>();
        // Columns
        List<ColumnImpl> notMatched = new ArrayList<>();
        notMatched.addAll(table.getColumns());
        for (ColumnImpl toColumn : toTable.getColumns(version)) {
            Optional<ColumnImpl> fromColumn =
                    Stream.of(
                            table.getColumn(toColumn.getName()),
                            toColumn.getPredecessor().map(Column::getName).flatMap(table::getColumn)
                            )
                    .flatMap(Functions.asStream())
                    .findFirst();
            if (fromColumn.isPresent()) {
                notMatched.remove(fromColumn.get());
                List<String> upgradeDdl = getUpgradeDdl(fromColumn.get(), toColumn);
                result.addAll(upgradeDdl);
                if (toColumn.isAutoIncrement()) {
                    if (!fromColumn.get().isAutoIncrement()) {
                        result.add(getSequenceDdl(toColumn));
                    }
                }
            } else {
                result.add(getAddDdl(toColumn, table.getName()));
                if (table.hasJournal(this.version)) {
                    result.add(getAddDdl(toColumn, table.getJournalTableName(this.version)));
                }
                if (toColumn.isAutoIncrement()) {
                    result.add(getSequenceDdl(toColumn));
                }
            }
        }

        for (ColumnImpl column : notMatched) {
            if (column.isNotNull()) {
                result.add(getSetNullColumnDdl(column, column.getTable().getName()));
                if (column.getTable().hasJournal()) {
                    result.add(getSetNullColumnDdl(column, column.getTable().getJournalTableName()));
                }
            }
        }
        for (IndexImpl index : toTable.getIndexes(version)) {
            IndexImpl fromIndex = table.getIndex(index.getName());
            if (fromIndex == null) {
                result.add(getIndexDdl(index));
            } else {
                // a bit strange construct, but indexes on virtual columns that need change are already dropped
                // by the columns upgrade
                for (String upgradeStatement : getUpgradeDdl(fromIndex, index)) {
                    if (!result.contains(upgradeStatement)) {
                        result.add(upgradeStatement);
                    }
                }
            }
        }
        //Constraints
        Set<TableConstraintImpl> unmatched = new HashSet<>();
        for (TableConstraintImpl constraint : table.getConstraints(version)) {
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
                    if (dialect.allowsConstraintRename()) {
                        result.add(getRenameConstraintDdl(fromConstraint, toConstraint));
                    } else {
                        result.addAll(getDropConstraintDdls(fromConstraint));
                        result.addAll(getAddConstraintDdls(toConstraint));
                    }
                }
            } else {
                unmatched.remove(fromConstraint);
                result.addAll(getUpgradeDdl(fromConstraint, toConstraint));

            }
        }
        for (TableConstraintImpl each : unmatched) {
            result.addAll(getDropConstraintDdls(each));
        }

        if (toTable.hasJournal(this.version) && !table.hasJournal()) {
            result.add(getJournalTableDdl(toTable));
        }
        // name
        if (!table.getName().equals(toTable.getName(version))) {
            result.add(getRenameDdl(toTable));
        }

        return result;
    }

    private String getRenameDdl(TableImpl<?> toTable) {
        return "alter table " + table.getName() + " rename to " + toTable.getName(version);
    }

    private String getRenameConstraintDdl(TableConstraintImpl fromConstraint, TableConstraintImpl toConstraint) {
        return MessageFormat.format(
                dialect.renameConstraintSyntax(),
                fromConstraint.getTable().getName(),
                fromConstraint.getName(),
                toConstraint.getName()
        );
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
        // for the moment quite naive: drop index and recreate
        return Arrays.asList(getDropIndex(fromIndex), getIndexDdl(toIndex));
    }

    private String getDropIndex(IndexImpl fromIndex) {
        return "DROP INDEX " + fromIndex.getName();
    }


    private String getAddDdl(ColumnImpl toColumn, String tableName) {
        StringBuilder builder = new StringBuilder("alter table ");
        builder.append(tableName);
        builder.append(" add ");
        appendDdl(toColumn, builder, true, true);
        return builder.toString();
    }

    private String getSetNullColumnDdl(ColumnImpl column, String tableName) {
        StringBuilder builder = new StringBuilder("alter table ");
        builder.append(tableName);
        builder.append(" modify ");
        appendDdl(column, builder, false, false);
        builder.append(" NULL ");
        return builder.toString();
    }

    private List<String> getUpgradeDdl(ColumnImpl fromColumn, ColumnImpl toColumn) {
        if (fromColumn.isVirtual() && toColumn.isVirtual()) {
            if (!fromColumn.getFormula().equalsIgnoreCase(toColumn.getFormula())) {
                List<String> results = new ArrayList<>();
                List<IndexImpl> fromColumnIndexes = getIndexesFor(fromColumn);
                for (IndexImpl fromColumnIndex : fromColumnIndexes) {
                    results.add(getDropIndex(fromColumnIndex));
                }
                results.add("alter table " + table.getName() + " drop column " + fromColumn.getName());
                results.add(getAddDdl(toColumn, table.getName()));
                if (table.hasJournal(this.version)) {
                    results.add("alter table " + table.getJournalTableName(this.version) + " drop column " + fromColumn.getName());
                    results.add(getAddDdl(toColumn, table.getJournalTableName(this.version)));
                }
                return results;
            }
        } else if (fromColumn.isVirtual()) {
            throw new IllegalArgumentException("Cannot migrate existing virtual column '" + fromColumn.getName() + "' to non-virtual column on table " + fromColumn.getTable().getName());
        } else if (toColumn.isVirtual()) {
            throw new IllegalArgumentException("Cannot migrate existing column '" + fromColumn.getName() + "' to a virtual column on table " + fromColumn.getTable().getName());
        } else {
            List<String> returnSqls = new ArrayList<>();
            if (!fromColumn.getName().equalsIgnoreCase(toColumn.getName())) {
                String renameSql = MessageFormat.format(
                        dialect.renameColumnSyntax(),
                        table.getName(),
                        fromColumn.getName(),
                        toColumn.getName()
                );
                returnSqls.add(renameSql);
            }
            if (!fromColumn.getDbType().equalsIgnoreCase(toColumn.getDbType())
                    || (fromColumn.isNotNull() != toColumn.isNotNull())) {
                // if from is null and to is not null then we'll try to update null to the install value
                if (!fromColumn.isNotNull() && toColumn.isNotNull() && toColumn.getInstallValue() != null) {
                    StringBuilder builder = new StringBuilder("update ")
                            .append(table.getName())
                            .append(" set ")
                            .append('"').append(fromColumn.getName().toUpperCase()).append('"')
                            .append(" = ")
                            .append(toColumn.getInstallValue())
                            .append(" where ")
                            .append('"').append(fromColumn.getName().toUpperCase()).append('"')
                            .append(" is null");
                    returnSqls.add(builder.toString());
                }
                // test for rename
                StringBuilder builder = new StringBuilder("alter table ");
                builder.append(table.getName());
                builder.append(" modify ");
                appendDdl(toColumn, builder, true, fromColumn.isNotNull() != toColumn.isNotNull());
                returnSqls.add(builder.toString());
                if (table.hasJournal(this.version)) {
                    builder = new StringBuilder("alter table ");
                    builder.append(table.getJournalTableName(this.version));
                    builder.append(" modify ");
                    appendDdl(toColumn, builder, true, fromColumn.isNotNull() != toColumn.isNotNull());
                    returnSqls.add(builder.toString());
                }
            }
            return returnSqls;
        }
        return Collections.emptyList();
    }

    private List<IndexImpl> getIndexesFor(ColumnImpl fromColumn) {
        List<IndexImpl> result = new ArrayList<>();
        for (IndexImpl index : table.getIndexes()) {
            for (ColumnImpl column : index.getColumns()) {
                if (column.getName().equalsIgnoreCase(fromColumn.getName())) {
                    result.add(index);
                }
            }
        }
        return result;
    }

    List<String> upgradeSequenceDdl(ColumnImpl column, long startValue) {
        return Arrays.asList(getDropSequenceDdl(column), getSequenceDdl(column, startValue));
    }

}