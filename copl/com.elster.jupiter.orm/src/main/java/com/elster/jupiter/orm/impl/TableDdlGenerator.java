/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Difference;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;

import java.sql.Statement;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;

class TableDdlGenerator implements PartitionMethod.Visitor {

    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final TableImpl<?> table;
    private final SqlDialect dialect;
    private final Version version;
    private final State state;

    private interface State {

        List<String> getRemoveDdl(ColumnImpl column, String tableName);

        Optional<Difference> getRemoveJournalTableDifference(TableImpl<?> toTable);
    }

    private static class Cautious implements State {

        private final Version version;

        private Cautious(Version version) {
            this.version = version;
        }

        @Override
        public List<String> getRemoveDdl(ColumnImpl column, String tableName) {
            if (!column.isNotNull()) {
                return Collections.emptyList();
            }
            List<String> ddls = new ArrayList<>();
            ddls.add(removeColumnDdl(column, column.getTable().getName()));
            if (column.getTable().hasJournal(version)) {
                ddls.add(removeColumnDdl(column, column.getTable().getJournalTableName(version)));
            }
            return ddls;
        }

        private String removeColumnDdl(ColumnImpl column, String tableName) {
            StringBuilder builder = new StringBuilder("alter table ")
                    .append(tableName)
                    .append(" modify ");
            appendDdl(column, builder, false, false);
            return builder.append(" NULL ").toString();
        }

        @Override
        public Optional<Difference> getRemoveJournalTableDifference(TableImpl<?> toTable) {
            return Optional.empty();
        }
    }

    private static class Strict implements State {

        private final Version version;

        private Strict(Version version) {
            this.version = version;
        }

        @Override
        public List<String> getRemoveDdl(ColumnImpl column, String tableName) {
            List<String> ddls = new ArrayList<>();
            ddls.add(removeColumnDdl(column, tableName));
            if (column.getTable().hasJournal(version)) {
                ddls.add(removeColumnDdl(column, column.getTable().getJournalTableName(version)));
            }
            return ddls;
        }

        private String removeColumnDdl(ColumnImpl column, String tableName) {
            return "alter table " + tableName + " drop column " + '\"' + column.getName().toUpperCase() + '\"';
        }

        @Override
        public Optional<Difference> getRemoveJournalTableDifference(TableImpl<?> toTable) {
            DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + toTable.getName() + " : Remove journal table.");
            difference.add("drop table " + toTable.previousJournalTableName(version).toUpperCase() + " cascade constraints");
            return difference.build();
        }
    }

    private TableDdlGenerator(TableImpl<?> table, SqlDialect dialect, Version version, State state) {
        this.table = table;
        this.dialect = dialect;
        this.version = version;
        this.state = state;
    }

    static TableDdlGenerator cautious(TableImpl<?> table, SqlDialect dialect, Version version) {
        return new TableDdlGenerator(table, dialect, version, new Cautious(version));
    }

    static TableDdlGenerator strict(TableImpl<?> table, SqlDialect dialect, Version version) {
        return new TableDdlGenerator(table, dialect, version, new Strict(version));
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
                    ddl.add("alter table " + constraint.getTable()
                            .getName() + " add " + getConstraintFragment(constraint));
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
            table.getPartitionMethod().visit(this, sb);
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
        for (int i = 0; i < 12; i++) {
            end += PARTITIONSIZE;
            String name = "P" + Instant.ofEpochMilli(end).toString().replaceAll("-", "").substring(0, 8);
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
        Optional<PrimaryKeyConstraintImpl> constraint = table.getPrimaryKeyConstraint(version);
        if (constraint.isPresent()) {
            sb.append(separator);
            sb.append(getJournalConstraint(constraint.get()));
        }
        sb.append(")");
        if (dialect.hasPartitioning()) {
            sb.append("partition by range(");
            sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
            sb.append(") interval (");
            sb.append(PARTITIONSIZE);
            sb.append(") (partition P0 values less than(0))");
        }
        return sb.toString();
    }

    private String getJournalConstraint(TableConstraintImpl<?> constraint) {
        StringBuilder sb = new StringBuilder("constraint ");
        String constraintName = constraint.getName() + "_JRNL";
        if (constraintName.length() > 30) {
            throw new IllegalArgumentException("Primary key for journal table too long: " + constraintName + ". Max length is 30 but found " + constraintName.length());
        }
        sb.append(constraintName);
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

    private String getConstraintFragment(TableConstraintImpl<?> constraint) {
        return constraint.getDdl();
    }

    private String getConstraintIndexDdl(TableConstraintImpl<?> constraint) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE INDEX ");
        builder.append(constraint.getName());
        builder.append(" ON ");
        builder.append(constraint.getTable().getQualifiedName());
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
        return getSequenceDdl(column.getQualifiedSequenceName(), 1);
    }

    private String getSequenceDdl(String name, long startValue) {
        // cache 1000 for performance in RAC environments
        return "create sequence " + name + " start with " + startValue + " cache 1000";
    }

    private String getDropSequenceDdl(String name) {
        return "drop sequence " + name;
    }

    private List<String> getRenameSequenceDdl(String oldName, String newName, DataModelImpl dataModel, Statement statement) {
        return Objects.equals(oldName, newName) ?
                Collections.emptyList() :
                dialect.renameSyntax()
                        .map(renameStatement -> MessageFormat.format(renameStatement, oldName, newName))
                        .map(Collections::singletonList)
                        .orElseGet(() -> Arrays.asList(getDropSequenceDdl(oldName), getSequenceDdl(newName, dataModel.getNextSequenceValue(statement, oldName) + 1)));
        // plus 1 to recreate the sequence with exactly same next value as current sequence would return next time;
        // for all the following checks to work well, new sequence must be strictly aligned with current one until the latter is effectively dropped.
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

    private static void appendDdl(ColumnImpl column, StringBuilder builder, boolean addType, boolean addNullable) {
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
                if (column.getInstallValue() != null) {
                    builder.append(" DEFAULT ").append(column.getInstallValue());
                }
                if (column.isNotNull()) {
                    builder.append(" NOT");
                }
                builder.append(" NULL");
            }
        }
    }

    List<Difference> upgradeDdl(TableImpl<?> toTable, Statement statement) {
        List<Difference> result = new ArrayList<>();
        // Columns
        for (ColumnImpl toColumn : toTable.getColumns(version)) {
            List<Difference> upgradeDdl = findFromColumn(toColumn)
                    .map(fromColumn -> upgradeFromDdl(fromColumn, toColumn))
                    .orElseGet(() -> newColumnDdl(toColumn));
            result.addAll(upgradeDdl);
        }

        for (ColumnImpl column : unmatchedColumns(toTable)) {
            removeColumnDifference(column).ifPresent(result::add);
        }

        for (IndexImpl index : toTable.getIndexes(version)) {
            table.getIndex(index.getName())
                    .map(fromIndex -> upgradeIndexDifference(fromIndex, index))
                    .orElseGet(() -> addIndexDifference(index))
                    .ifPresent(result::add);
        }

        // Constraints
        Set<TableConstraintImpl> unmatched = new HashSet<>(table.getConstraints(version));

        List<Difference> dropOrRenameOrDelayedConstraintDiffs = new ArrayList<>();
        List<Difference> addConstraintDiffs = new ArrayList<>();
        toTable.getConstraints(version)
                .stream()
                .map(constraint -> upgradeConstraintDdl(dropOrRenameOrDelayedConstraintDiffs, addConstraintDiffs, constraint))
                .flatMap(Functions.asStream())
                .forEach(unmatched::remove);
        unmatched.stream().map(this::getDropConstraintDifference).forEach(result::add);

        // Constraints delayed by this table
        decorate(toTable.getDataModel().getTables()
                .stream())
                .takeWhile(not(toTable::equals))
                .map(table1 -> table1.getConstraints(version))
                .flatMap(List::stream)
                .filter(TableConstraintImpl::delayDdl)
                .filterSubType(ForeignKeyConstraintImpl.class)
                .filter(foreignKeyConstraint -> foreignKeyConstraint.getReferencedTable().equals(toTable))
                .forEach(perform(this::upgradeDelayedConstraintDdl).on(dropOrRenameOrDelayedConstraintDiffs));

        result.addAll(dropOrRenameOrDelayedConstraintDiffs);

        if (toTable.hasJournal(version) && !table.hasJournal()) {
            result.add(getJournalTableDifference(toTable));
        }
        if (!toTable.hasJournal(version) && table.hasJournal()) {
            state.getRemoveJournalTableDifference(toTable).ifPresent(result::add);
        }
        // rename table
        if (!table.getName().equals(toTable.getName(version))) {
            result.add(getRenameDifference(toTable, statement));
        }
        result.addAll(addConstraintDiffs);
        return result;
    }

    private Optional<TableConstraintImpl> upgradeConstraintDdl(List<Difference> dropOrRenameConstraintDiffs,
                                                               List<Difference> addConstraintDiffs,
                                                               TableConstraintImpl toConstraint) {
        TableConstraintImpl fromConstraint = table.getConstraint(toConstraint.getName());
        if (fromConstraint == null) {
            // new constraint, or old one has been renamed
            fromConstraint = table.findMatchingConstraint(toConstraint);
            if (fromConstraint == null) {
                // new constraint, or old one has been renamed and changed
                if (!toConstraint.delayDdl() && !toConstraint.noDdl()) {
                    addConstraintDiffs.add(getAddConstraintDifference(toConstraint));
                }
                return Optional.empty();
            } else {
                // old constraint has been renamed
                if (dialect.allowsConstraintRename()) {
                    dropOrRenameConstraintDiffs.add(getRenameConstraintDifference(fromConstraint, toConstraint));
                } else {
                    Pair<Difference, Difference> diffs = getDropRecreateConstraintDifferences(fromConstraint, toConstraint);
                    dropOrRenameConstraintDiffs.add(diffs.getFirst());
                    if (!toConstraint.delayDdl() && !toConstraint.noDdl()) {
                        addConstraintDiffs.add(diffs.getLast());
                    }
                }
                return Optional.of(fromConstraint);
            }
        } else {
            // old constraint has been changed with name kept, or no change at all
            getUpgradeDifferences(fromConstraint, toConstraint)
                    .ifPresent(diffs -> {
                        dropOrRenameConstraintDiffs.add(diffs.getFirst());
                        if (!toConstraint.delayDdl() && !toConstraint.noDdl()) {
                            addConstraintDiffs.add(diffs.getLast());
                        }
                    });
            return Optional.of(fromConstraint);
        }
    }

    private void upgradeDelayedConstraintDdl(List<Difference> addOrRecreateConstraintDiffs, TableConstraintImpl toConstraint) {
        if (!alreadyUpgraded(toConstraint)) {
            // only addition / recreation is delayed
            addOrRecreateConstraintDiffs.add(getAddConstraintDifference(toConstraint));
        }
    }

    private boolean alreadyUpgraded(TableConstraintImpl constraint) {
        TableConstraintImpl<?> fromConstraint = table.getDataModel() // data model from which we upgrade
                .getTable(constraint.getTableName())
                .getConstraint(constraint.getName());
        return fromConstraint != null && fromConstraint.matches(constraint);
    }

    private Optional<Difference> removeColumnDifference(ColumnImpl column) {
        DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Column removed " + column
                .getName());
        state.getRemoveDdl(column, column.getTable().getName())
                .forEach(difference::add);
        return difference.build();
    }

    private Optional<Difference> addIndexDifference(IndexImpl index) {
        return DdlDifferenceImpl.builder("Table " + table.getName() + " : New index " + index.getName())
                .add(getIndexDdl(index))
                .build();
    }

    private Difference getJournalTableDifference(TableImpl<?> toTable) {
        DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Add journal table.");
        difference.add(getJournalTableDdl(toTable));
        return difference.build().get();
    }

    private Pair<Difference, Difference> getDropRecreateConstraintDifferences(TableConstraintImpl fromConstraint, TableConstraintImpl toConstraint) {
        DdlDifferenceImpl.DifferenceBuilder dropDifference = DdlDifferenceImpl.builder("Table " + table.getName()
                + " : Drop constraint " + fromConstraint.getName() + " in order to recreate with name " + toConstraint.getName());
        getDropConstraintDdls(fromConstraint).forEach(dropDifference::add);
        DdlDifferenceImpl.DifferenceBuilder addDifference = DdlDifferenceImpl.builder("Table " + toConstraint.getTable().getName()
                + " : Recreate constraint " + fromConstraint.getName() + " with name " + toConstraint.getName());
        getAddConstraintDdls(toConstraint).forEach(addDifference::add);
        return Pair.of(dropDifference.build().orElseThrow(IllegalStateException::new), addDifference.build().orElseThrow(IllegalStateException::new));
    }

    private Optional<Difference> upgradeIndexDifference(IndexImpl fromIndex, IndexImpl toIndex) {
        if (fromIndex.matches(toIndex)) {
            return Optional.empty();
        }
        DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Redefining index " + toIndex
                .getName());
        // for the moment quite naive: drop index and recreate
        difference.add(getDropIndex(fromIndex));
        difference.add(getIndexDdl(toIndex));
        return difference.build();
    }

    private List<ColumnImpl> unmatchedColumns(TableImpl<?> toTable) {
        Set<ColumnImpl> matched = toTable.getColumns(version)
                .stream()
                .map(this::findFromColumn)
                .flatMap(Functions.asStream())
                .collect(Collectors.toSet());
        return table.getColumns(version)
                .stream()
                .filter(not(matched::contains))
                .collect(Collectors.toList());
    }

    private List<Difference> newColumnDdl(ColumnImpl toColumn) {
        DdlDifferenceImpl.DifferenceBuilder upgradeDdl1 = DdlDifferenceImpl.builder("Table " + table.getName() + " : New column " + toColumn
                .getName());
        upgradeDdl1.add(getAddDdl(toColumn, table.getName()));
        if (table.hasJournal()) {
            upgradeDdl1.add(getAddDdl(toColumn, table.getJournalTableName()));
        }
        if (toColumn.isAutoIncrement()) {
            upgradeDdl1.add(getSequenceDdl(toColumn));
        }
        return upgradeDdl1.build().map(Collections::singletonList).orElseGet(Collections::emptyList);
    }

    private List<Difference> upgradeFromDdl(ColumnImpl fromColumn, ColumnImpl toColumn) {
        List<Difference> upgradeDifferences = getUpgradeDifference(fromColumn, toColumn);
        if (toColumn.isAutoIncrement() && !fromColumn.isAutoIncrement()) {
            upgradeDifferences = new ArrayList<>(upgradeDifferences);
            upgradeDifferences.add(addAutoIncrementSequenceDifference(toColumn));
        }
        return upgradeDifferences;
    }

    private Difference addAutoIncrementSequenceDifference(ColumnImpl toColumn) {
        DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Added sequence " + toColumn
                .getName());
        difference.add(getSequenceDdl(toColumn));
        return difference.build().get();
    }

    private Optional<ColumnImpl> findFromColumn(ColumnImpl toColumn) {
        return Stream.of(
                table.getColumn(toColumn.getName()),
                toColumn.getPredecessor().map(Column::getName).flatMap(table::getColumn)
        )
                .flatMap(Functions.asStream())
                .findFirst();
    }

    private Difference getRenameDifference(TableImpl<?> toTable, Statement statement) {
        String oldTableName = table.getName();
        String newTableName = toTable.getName(version);
        DdlDifferenceImpl.DifferenceBuilder renameBuilder = DdlDifferenceImpl.builder("Table " + oldTableName + " : Rename table to " + newTableName)
                .add("alter table " + oldTableName + " rename to " + newTableName);
        table.getRealColumns()
                .filter(Column::isAutoIncrement)
                .map(Column::getQualifiedSequenceName)
                .map(seqName -> getRenameSequenceDdl(seqName, seqName.replaceFirst(oldTableName, newTableName), table.getDataModel(), statement))
                .flatMap(List::stream)
                .forEach(renameBuilder::add);
        return renameBuilder.build()
                .orElseThrow(IllegalStateException::new);
    }

    private Difference getRenameConstraintDifference(TableConstraintImpl fromConstraint, TableConstraintImpl toConstraint) {
        String ddl = MessageFormat.format(
                dialect.renameConstraintSyntax(),
                fromConstraint.getTable().getName(),
                fromConstraint.getName(),
                toConstraint.getName()
        );
        return DdlDifferenceImpl.builder("Table " + table.getName()
                + " : Rename constraint " + fromConstraint.getName() + " to " + toConstraint.getName())
                .add(ddl)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private Difference getAddConstraintDifference(TableConstraintImpl constraint) {
        DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : New or recreated constraint " + constraint
                .getName());
        getAddConstraintDdls(constraint).forEach(difference::add);
        return difference.build()
                .orElseThrow(IllegalStateException::new);
    }

    private List<String> getAddConstraintDdls(TableConstraintImpl constraint) {
        List<String> result = new ArrayList<>();
        result.add("alter table " + constraint.getTable().getName() + " add " + getConstraintFragment(constraint));
        if (constraint.needsIndex()) {
            result.add(getConstraintIndexDdl(constraint));
        }
        return result;
    }

    private Difference getDropConstraintDifference(TableConstraintImpl constraint) {
        DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Removed constraint " + constraint
                .getName());
        getDropConstraintDdls(constraint).forEach(difference::add);
        return difference.build().get();
    }

    private List<String> getDropConstraintDdls(TableConstraintImpl<?> constraint) {
        List<String> result = new ArrayList<>();
        result.add("alter table " + constraint.getTable().getName() + " drop constraint " + constraint.getName());
        if (hasIndex(constraint)) {
            result.add(getDropConstraintIndexDdl(constraint));
        }
        return result;
    }

    private boolean hasIndex(TableConstraintImpl<?> constraint) {
        return constraint instanceof ForeignKeyConstraint && constraint.getTable()
                .getIndex(constraint.getName())
                .isPresent();
    }

    private Optional<Pair<Difference, Difference>> getUpgradeDifferences(TableConstraintImpl<?> fromConstraint, TableConstraintImpl<?> toConstraint) {
        if (fromConstraint.matches(toConstraint)) {
            return Optional.empty();
        }
        DdlDifferenceImpl.DifferenceBuilder dropDifference = DdlDifferenceImpl.builder("Table " + table.getName()
                + " : Dropping constraint " + toConstraint.getName() + " to redefine it");
        getDropConstraintDdls(fromConstraint).forEach(dropDifference::add);
        DdlDifferenceImpl.DifferenceBuilder addDifference = DdlDifferenceImpl.builder("Table " + toConstraint.getTable().getName()
                + " : Redefining constraint " + toConstraint.getName());
        getAddConstraintDdls(toConstraint).forEach(addDifference::add);

        return Optional.of(Pair.of(dropDifference.build().orElseThrow(IllegalStateException::new), addDifference.build().orElseThrow(IllegalStateException::new)));
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

    private List<Difference> getUpgradeDifference(ColumnImpl fromColumn, ColumnImpl toColumn) {
        if (fromColumn.isVirtual() && toColumn.isVirtual()) {
            if (!fromColumn.getFormula().equalsIgnoreCase(toColumn.getFormula())) {
                DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Difference in indexes for virtual column " + toColumn
                        .getName());
                List<IndexImpl> fromColumnIndexes = getIndexesFor(fromColumn);
                for (IndexImpl fromColumnIndex : fromColumnIndexes) {
                    difference.add(getDropIndex(fromColumnIndex));
                }
                difference.add("alter table " + table.getName() + " drop column " + fromColumn.getName());
                difference.add(getAddDdl(toColumn, table.getName()));
                if (table.hasJournal(version)) {
                    difference.add("alter table " + table.getJournalTableName(version) + " drop column " + fromColumn.getName());
                    difference.add(getAddDdl(toColumn, table.getJournalTableName(version)));
                }
                return difference.build().map(Collections::singletonList).orElseGet(Collections::emptyList);
            }
        } else if (fromColumn.isVirtual()) {
            throw new IllegalArgumentException("Cannot migrate existing virtual column '" + fromColumn.getName() + "' to non-virtual column on table " + fromColumn
                    .getTable()
                    .getName());
        } else if (toColumn.isVirtual()) {
            throw new IllegalArgumentException("Cannot migrate existing column '" + fromColumn.getName() + "' to a virtual column on table " + fromColumn
                    .getTable()
                    .getName());
        } else {
            DdlDifferenceImpl.DifferenceBuilder difference = DdlDifferenceImpl.builder("Table " + table.getName() + " : Column change from " + fromColumn
                    .getName() + " to " + toColumn.getName());
            if (!fromColumn.getName().equalsIgnoreCase(toColumn.getName())) {
                String renameSql = MessageFormat.format(
                        dialect.renameColumnSyntax(),
                        table.getName(),
                        fromColumn.getName(),
                        toColumn.getName()
                );
                difference.add(renameSql);
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
                    difference.add(builder.toString());
                }
                // test for rename
                StringBuilder builder = new StringBuilder("alter table ");
                builder.append(table.getName());
                builder.append(" modify ");
                appendDdl(toColumn, builder, true, fromColumn.isNotNull() != toColumn.isNotNull());
                difference.add(builder.toString());
                if (table.hasJournal(version)) {
                    builder = new StringBuilder("alter table ");
                    builder.append(table.getJournalTableName(this.version));
                    builder.append(" modify ");
                    appendDdl(toColumn, builder, true, fromColumn.isNotNull() != toColumn.isNotNull());
                    difference.add(builder.toString());
                }
            }
            return difference.build().map(Collections::singletonList).orElseGet(Collections::emptyList);
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

    Difference upgradeSequenceDifference(ColumnImpl column, long startValue) {
        return DdlDifferenceImpl.builder("Table " + table.getName() + " : Redefined sequence " + column.getSequenceName())
                .add(getDropSequenceDdl(column.getQualifiedSequenceName()))
                .add(getSequenceDdl(column.getQualifiedSequenceName(), startValue))
                .build()
                .orElseThrow(IllegalStateException::new);
    }
}
