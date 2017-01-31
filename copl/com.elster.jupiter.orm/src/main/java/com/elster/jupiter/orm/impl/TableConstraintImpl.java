/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.intersection;

public abstract class TableConstraintImpl<S extends TableConstraint> implements TableConstraint {

    public static final Map<String, Class<? extends TableConstraint>> implementers = ImmutableMap.of(
            "PRIMARYKEY", PrimaryKeyConstraintImpl.class,
            "UNIQUE", UniqueConstraintImpl.class,
            "FOREIGNKEY", ForeignKeyConstraintImpl.class);

    private String name;
    @SuppressWarnings("unused")
    private int position;

    // associations
    private final Reference<TableImpl<?>> table = ValueReference.absent();
    private final List<ColumnInConstraintImpl> columnHolders = new ArrayList<>();

    private transient RangeSet<Version> versions = ImmutableRangeSet.<Version>of().complement();
    private transient RangeSet<Version> versionsIntersectedWithTable;
    private S self;

    TableConstraintImpl(Class<S> selfType) {
        self = selfType.cast(this);
    }

    TableConstraintImpl init(TableImpl<?> table, String name) {
        if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
            throw new IllegalTableMappingException("Table " + table.getName() + " : constraint name '" + name + "' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + " actual length is " + name.length() + ".");
        }
        this.table.set(table);
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ColumnImpl> getColumns() {
        ImmutableList.Builder<ColumnImpl> builder = new ImmutableList.Builder<>();
        for (ColumnInConstraintImpl each : columnHolders) {
            builder.add(each.getColumn());
        }
        return builder.build();
    }

    @Override
    public TableImpl<?> getTable() {
        return table.get();
    }

    void add(ColumnImpl column) {
        columnHolders.add(ColumnInConstraintImpl.from(this, column));
    }

    void add(Column[] columns) {
        for (Column column : columns) {
            add((ColumnImpl) column);
        }
    }

    String getComponentName() {
        return getTable().getComponentName();
    }


    String getTableName() {
        return getTable().getName();
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean isForeignKey() {
        return false;
    }

    @Override
    public boolean noDdl() {
    	return false;
    }

    @Override
    public boolean hasColumn(Column column) {
        return getColumns().contains(column);
    }

    @Override
    public boolean isNotNull() {
        for (Column each : getColumns()) {
            if (!each.isNotNull()) {
                return false;
            }
        }
        return true;
    }

    public KeyValue getColumnValues(Object value) {
        List<ColumnImpl> columns = getColumns();
        int columnCount = columns.size();
        Object[] result = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            result[i] = columns.get(i).domainValue(value);
        }
        return KeyValue.of(result);
    }


    boolean needsIndex() {
        return false;
    }

    abstract String getTypeString();

    void appendDdlTrailer(StringBuilder builder) {
        // do nothing by default;
    }

    public final String getDdl() {
        StringBuilder sb = new StringBuilder("constraint ");
        sb.append(name);
        sb.append(" ");
        sb.append(getTypeString());
        sb.append(" (");
        String separator = "";
        for (Column column : getColumns()) {
            sb.append(separator);
            sb.append(column.getName());
            separator = ", ";
        }
        sb.append(") ");
        appendDdlTrailer(sb);
        return sb.toString();
    }

    RangeSet<Version> versions() {
        if (versionsIntersectedWithTable == null) {
            versionsIntersectedWithTable = intersectWithTable(versions);
        }
        return versionsIntersectedWithTable;
    }

    void validate() {
        Objects.requireNonNull(getTable());
        Objects.requireNonNull(name);
        if (this.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Column list should not be emty");
        }
    }

    public boolean matches(TableConstraintImpl<?> other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (getColumns().size() != other.getColumns().size()) {
            return false;
        }
        for (int i = 0; i < getColumns().size(); i++) {
            Column each = getColumns().get(i);
            Column otherCol = other.getColumns().get(i);
            if (!each.getName().equalsIgnoreCase(otherCol.getName())) {
                return false;
            }
        }
        return true;
    }

    public boolean delayDdl() {
    	return false;
    }

    S since(Version version) {
        versions = intersectWithTable(ImmutableRangeSet.of(Range.atLeast(version)));
        return self;
    }

    S upTo(Version version) {
        versions = intersectWithTable(ImmutableRangeSet.of(Range.lessThan(version)));
        return self;
    }

    S during(Range... ranges) {
        ImmutableRangeSet.Builder<Version> builder = ImmutableRangeSet.builder();
        Arrays.stream(ranges)
                .forEach(builder::add);
        versions = intersectWithTable(builder.build());
        return self;
    }

    @Override
    public SortedSet<Version> changeVersions() {
        return versions()
                .asRanges()
                .stream()
                .flatMap(range -> Stream.of(Ranges.lowerBound(range), Ranges.upperBound(range)).flatMap(Functions.asStream()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public void setVersions(RangeSet<Version> versions) {
        if (versions instanceof ImmutableRangeSet) {
            this.versions = versions;
        } else {
            this.versions = ImmutableRangeSet.copyOf(versions);
        }
        versionsIntersectedWithTable = null;
    }

    @Override
    public boolean isInVersion(Version version) {
        return versions().contains(version);
    }

    private RangeSet<Version> intersectWithTable(RangeSet<Version> set) {
        return intersection(table.get().getVersions(), set);
    }

}
