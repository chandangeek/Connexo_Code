/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.MacException;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.fields.impl.ColumnEqualsFragment;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.sql.TupleParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataMapperReader<T> implements TupleParser<T> {
    private final DataMapperImpl<T> dataMapper;
    private final int SQL_IN_CLAUSE_MAX_PARTITION_SIZE = 999;

    enum MACEnforcementMode {
        Secure,
        Unsecure
    }

    DataMapperReader(DataMapperImpl<T> dataMapper) {
        this.dataMapper = dataMapper;
    }

    private String getAlias() {
        return dataMapper.getAlias();
    }

    private DataMapperType<? super T> getMapperType() {
        return dataMapper.getMapperType();
    }

    private Connection getConnection(boolean txRequired) throws SQLException {
        return getTable().getDataModel().getConnection(txRequired);
    }

    private TableImpl<? super T> getTable() {
        return dataMapper.getTable();
    }

    private TableSqlGenerator getSqlGenerator() {
        return dataMapper.getSqlGenerator();
    }

    private List<SqlFragment> getPrimaryKeyFragments(KeyValue keyValue) {
        List<ColumnImpl> pkColumns = getPrimaryKeyColumns();
        if (pkColumns.size() != keyValue.size()) {
            throw new IllegalArgumentException("Argument array length does not match Primary Key Field count of " + pkColumns.size());
        }
        List<SqlFragment> fragments = new ArrayList<>(pkColumns.size());
        for (int i = 0; i < keyValue.size(); i++) {
            fragments.add(new ColumnEqualsFragment(pkColumns.get(i), keyValue.get(i), getAlias()));
        }
        getMapperType().addSqlFragment(fragments, dataMapper.getApi(), getAlias());
        return fragments;
    }

    public Optional<T> findByPrimaryKey(KeyValue keyValue) throws SQLException {
        List<T> result = find(getPrimaryKeyFragments(keyValue), null, LockMode.NONE);
        if (result.size() > 1) {
            throw new NotUniqueException(keyValue.toString());
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    List<JournalEntry<T>> findJournals(KeyValue keyValue) throws SQLException {
        return findJournal(getPrimaryKeyFragments(keyValue), new Order[]{Order.descending(TableImpl.JOURNALTIMECOLUMNNAME)}, LockMode.NONE);
    }

    List<JournalEntry<T>> findJournals(Instant instant, List<Comparison> comparisons) throws SQLException {
        return (instant == Instant.EPOCH) ? findJournalComparison(comparisons, new Order[]{Order.descending(TableImpl.JOURNALTIMECOLUMNNAME)}, LockMode.NONE) :
                findJournalComparison(instant, comparisons, new Order[]{Order.descending(TableImpl.JOURNALTIMECOLUMNNAME)}, LockMode.NONE);
    }

    List<JournalEntry<T>> findJournals(Instant instant, Map<String, Object> valueMap) throws SQLException {
        List<SqlFragment> fragments = new ArrayList<>();
        getMapperType().addSqlFragment(fragments, dataMapper.getApi(), getAlias());
        if (valueMap != null) {
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                addFragments(fragments, entry.getKey(), entry.getValue());
            }
        }
        fragments.add(new SqlFragment() {
            @Override
            public int bind(PreparedStatement statement, int index) throws SQLException {
                statement.setLong(index++, instant.toEpochMilli());
                statement.setLong(index++, instant.toEpochMilli());
                return index;
            }

            @Override
            public String getText() {
                return TableImpl.MODTIMECOLUMNAME + " <= ? and " + TableImpl.JOURNALTIMECOLUMNNAME + " > ? ";
            }
        });
        return findJournal(fragments, null, LockMode.NONE);
    }

    T lock(KeyValue keyValue) throws SQLException {
        List<T> candidates = find(getPrimaryKeyFragments(keyValue), null, LockMode.WAIT);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    T lock(KeyValue keyValue, long version) throws SQLException {
        List<SqlFragment> fragments = new ArrayList<>(getPrimaryKeyFragments(keyValue));
        ColumnImpl versionColumn = getVersionCountColumn();
        fragments.add(new ColumnEqualsFragment(versionColumn, version, getAlias()));
        List<T> candidates = find(fragments, null, LockMode.WAIT);
        return candidates.isEmpty() ? null : candidates.get(0);
    }


    T lockNoWait(KeyValue keyValue) throws SQLException {
        try {
            List<T> candidates = find(getPrimaryKeyFragments(keyValue), null, LockMode.NOWAIT);
            return candidates.isEmpty() ? null : candidates.get(0);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 54) {
                // resource busy
                return null;
            } else {
                throw ex;
            }
        }
    }

    private Order getListOrder(String fieldName) {
        ForeignKeyConstraintImpl constraint = getTable().getConstraintForField(fieldName);
        if (constraint == null || constraint.getReverseOrderFieldName() == null) {
            return null;
        } else {
            return Order.ascending(constraint.getReverseOrderFieldName());
        }
    }

    List<T> find(String[] fieldNames, Object[] values, Order... orders) throws SQLException {
        return find(fieldNames, values, MACEnforcementMode.Secure, orders);
    }

    List<T> find(String[] fieldNames, Object[] values, MACEnforcementMode macEnforcementMode, Order... orders) throws SQLException {
        if (fieldNames != null && fieldNames.length == 1 && (orders == null || orders.length == 0)) {
            Order listOrder = getListOrder(fieldNames[0]);
            if (listOrder != null) {
                orders = new Order[]{listOrder};
            }
        }
        List<SqlFragment> fragments = new ArrayList<>();
        getMapperType().addSqlFragment(fragments, dataMapper.getApi(), getAlias());
        if (fieldNames != null) {
            for (int i = 0; i < fieldNames.length; i++) {
                addFragments(fragments, fieldNames[i], values[i]);
            }
        }
        return find(fragments, orders, LockMode.NONE, macEnforcementMode);
    }


    List<T> findWithoutMacCheck() throws SQLException {
        return find((String[]) null, (Object[]) null, MACEnforcementMode.Unsecure, Order.NOORDER);
    }

    List<T> find(Instant instant, Map<String, Object> valueMap, Order[] noorder) throws SQLException {
        return find(instant, valueMap, MACEnforcementMode.Secure, noorder);
    }

    List<T> find(Instant instant, Map<String, Object> valueMap, MACEnforcementMode macEnforcementMode, Order[] noorder) throws SQLException {
        List<SqlFragment> fragments = new ArrayList<>();
        getMapperType().addSqlFragment(fragments, dataMapper.getApi(), getAlias());
        if (valueMap != null) {
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                addFragments(fragments, entry.getKey(), entry.getValue());
            }
        }
        fragments.add(new SqlFragment() {

            @Override
            public String getText() {
                return TableImpl.MODTIMECOLUMNAME + " <= ? ";
            }

            @Override
            public int bind(PreparedStatement statement, int index) throws SQLException {
                statement.setLong(index++, instant.toEpochMilli());
                return index;
            }
        });
        return find(fragments, null, LockMode.NONE, macEnforcementMode);
    }

    private List<T> find(List<SqlFragment> fragments, Order[] orders, LockMode lockMode) throws SQLException {
        return find(fragments, orders, lockMode, MACEnforcementMode.Secure);
    }

    private List<T> find(List<SqlFragment> fragments, Order[] orders, LockMode lockMode, MACEnforcementMode macEnforcementMode) throws SQLException {
        SqlBuilder builder = selectSql(fragments, orders, lockMode);
        return doFind(fragments, builder, macEnforcementMode);
    }

    private List<JournalEntry<T>> findJournalComparison(Instant instant, List<Comparison> comparisons, Order[] orders, LockMode lockMode) throws SQLException {

        List<SqlFragment> fragments = new ArrayList<>();
        getMapperType().addSqlFragment(fragments, dataMapper.getApi(), getAlias());

        comparisons.forEach(comparison -> addFragments(fragments, comparison));

        fragments.add(new SqlFragment() {
            @Override
            public int bind(PreparedStatement statement, int index) throws SQLException {
                statement.setLong(index++, instant.toEpochMilli());
                statement.setLong(index++, instant.toEpochMilli());
                return index;
            }

            @Override
            public String getText() {
                return TableImpl.MODTIMECOLUMNAME + " <= ? and " + TableImpl.JOURNALTIMECOLUMNNAME + " > ? ";
            }
        });
        return findJournal(fragments, orders, lockMode);
    }

    private List<JournalEntry<T>> findJournalComparison(List<Comparison> comparisons, Order[] orders, LockMode lockMode) throws SQLException {

        List<SqlFragment> fragments = new ArrayList<>();

        comparisons.stream().forEach(comparison -> {
            FieldMapping mapping = getTable().getFieldMapping(comparison.getFieldName());
            if (mapping != null) {
                fragments.add(mapping.asComparisonFragment(comparison, getAlias()));
                return;
            }

            fragments.add(new SqlFragment() {
                String statement = "";

                @Override
                public String getText() {
                    String[] values = new String[comparison.getValues().length];
                    if (comparison.getOperator() == Operator.IN) {
                        Arrays.fill(values, "?");
                        if (values.length > SQL_IN_CLAUSE_MAX_PARTITION_SIZE) {
                            List<List<String>> partitions = new ArrayList<>();
                            for (int i = 0; i < values.length; i += SQL_IN_CLAUSE_MAX_PARTITION_SIZE) {
                                partitions.add(Arrays.asList(values).subList(i,
                                        Math.min(i + SQL_IN_CLAUSE_MAX_PARTITION_SIZE, values.length)));
                            }
                            Iterator partitionIterator = partitions.iterator();
                            while (partitionIterator.hasNext()) {
                                List<String> nextPartition = (List<String>) partitionIterator.next();
                                if (statement.isEmpty()) {
                                    statement += buildInCondition(nextPartition, comparison.getFieldName(), comparison.getOperator().getSymbol());
                                } else {
                                    statement += nextPartition.size() > 1 ?
                                            " OR " + buildInCondition((List<String>) partitionIterator.next(), comparison.getFieldName(), comparison.getOperator().getSymbol()) :
                                            " OR " + comparison.getFieldName() + " " + comparison.getOperator().getSymbol() + " " + " ? ";
                                }
                            }
                        } else {
                            return statement += buildInCondition(Arrays.asList(values), comparison.getFieldName(), comparison.getOperator().getSymbol());
                        }
                    } else {
                        return comparison.getFieldName() + " " + comparison.getOperator().getSymbol() + " " + " ? ";
                    }
                    return statement;
                }

                @Override
                public int bind(PreparedStatement statement, int index) throws SQLException {
                    if (comparison.getOperator() == Operator.IN) {
                        for (int i = 0; i < comparison.getValues().length; i++) {
                            Object value = comparison.getValues()[i];
                            if (value instanceof String) {
                                statement.setString(index++, comparison.getValues()[i].toString()); // only string values supported
                            } else {
                                statement.setLong(index++, Long.parseLong(comparison.getValues()[i].toString())); // only string values supported

                            }
                        }
                    } else {
                        Object value = comparison.getValues()[0];
                        if (value instanceof String) {
                            statement.setString(index++, value.toString());
                        } else if (value instanceof Instant) {
                            statement.setLong(index++, ((Instant) value).toEpochMilli());
                        } else {
                            statement.setLong(index++, Long.parseLong(value.toString()));
                        }
                    }
                    return index;
                }
            });

        });
        return findJournal(fragments, orders, lockMode);
    }

    private String buildInCondition(List<String> partition, String fieldName, String operator) {
        String statement = fieldName + " " + operator;
        statement += " ( ";
        statement += partition.stream().collect(Collectors.joining(", "));
        statement += " ) ";
        return statement;
    }

    private List<JournalEntry<T>> findJournal(List<SqlFragment> fragments, Order[] orders, LockMode lockMode) throws SQLException {
        SqlBuilder builder = selectJournalSql(fragments, orders, lockMode);
        List<JournalEntry<T>> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = builder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Instant journalTime = Instant.ofEpochMilli(resultSet.getLong(1));
                        T entry = construct(resultSet, 2, MACEnforcementMode.Secure, true)
                                .orElseThrow(fetchedRowWithNoValuesException());
                        result.add(new JournalEntry<>(journalTime, entry));
                    }
                }
            }
        }
        return result;
    }

    private List<T> doFind(List<SqlFragment> fragments, SqlBuilder builder, MACEnforcementMode macEnforcementMode) throws SQLException {
        List<Setter> setters = getSetters(fragments);
        List<T> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = builder.prepare(connection)) {
                if (fragments.isEmpty()) {
                    statement.setFetchSize(100);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(construct(resultSet, setters, macEnforcementMode));
                    }
                }
            }
        }
        return result;
    }

    private List<Setter> getSetters(List<SqlFragment> fragments) {
        List<Setter> setters = new ArrayList<>();
        for (SqlFragment each : fragments) {
            if (each instanceof Setter) {
                setters.add((Setter) each);
            }
        }
        return setters;
    }

    private SqlBuilder selectSql(List<SqlFragment> fragments, Order[] orders, LockMode lockMode) {
        SqlBuilder builder = new SqlBuilder(getSqlGenerator().getSelectFromClause(getAlias()));
        return doSelectSql(fragments, orders, lockMode, builder);
    }

    private SqlBuilder selectJournalSql(List<SqlFragment> fragments, Order[] orders, LockMode lockMode) {
        SqlBuilder builder = new SqlBuilder(getSqlGenerator().getSelectFromJournalClause(getAlias()));
        return doSelectSql(fragments, orders, lockMode, builder);
    }

    private SqlBuilder doSelectSql(List<SqlFragment> fragments, Order[] orders, LockMode lockMode, SqlBuilder builder) {
        if (!fragments.isEmpty()) {
            builder.append(" where ");
            String separator = "";
            for (SqlFragment each : fragments) {
                builder.append(separator);
                builder.add(each);
                separator = " AND ";
            }
        }
        if (orders != null && orders.length > 0) {
            builder.append(" order by ");
            String separator = "";
            for (Order each : orders) {
                FieldMapping fieldMapping = this.getTable().getFieldMapping(each.getName());
                List<? extends Column> columns = fieldMapping == null ? Collections.<Column>emptyList() : fieldMapping.getColumns();
                if (columns.isEmpty()) {
                    builder.append(separator);
                    separator = ", ";
                    builder.append(each.getClause(each.getName()));
                } else {
                    for (Column column : columns) {
                        builder.append(separator);
                        separator = ", ";
                        builder.append(each.getClause(column.getName(getAlias())));
                    }
                }
            }
        }
        builder.space();
        builder.append(lockMode.toSql());
        return builder;
    }

    private Optional<T> newInstance(ResultSet rs, int startIndex) throws SQLException {
        List<ColumnImpl> columns = getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isDiscriminator()) {
                return Optional.ofNullable(rs.getString(startIndex + i))
                        .map(getMapperType()::newInstance)
                        .map(dataMapper::cast);
            }
        }
        throw MappingException.noDiscriminatorColumn();
    }

    @Override
    public T construct(ResultSet rs) throws SQLException {
        return construct(rs, 1, MACEnforcementMode.Secure, true)
                .orElseThrow(fetchedRowWithNoValuesException());
    }

    Fetcher<T> fetcher(SqlBuilder builder) throws SQLException {
        // The connection will be closed when the Fetcher is closed - so no resource leak here
        Connection connection = getConnection(false);
        try {
              return builder.fetcher(connection, this);
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
    }

    /**
     * @param rs The result set
     * @param startIndex The index of the first column related to the constructed object
     * @param macEnforcementMode The mac enforcement mode for object construction
     * @param force If {@code true}, the object is constructed even if all columns contain Oracle NULL
     * @return The constructed object if we have at least one non-NULL field, empty optional otherwise.
     * In case of eagerly joined tables, all NULL fields related to one table would mean that no entry is fetched from this table => nothing to construct.
     * In case of root of the query or single table fetch, please use {@code force} argument to force object construction even with all NULL fields.
     */
    Optional<T> construct(ResultSet rs, int startIndex, MACEnforcementMode macEnforcementMode, boolean force) throws SQLException {
        Optional<T> optionalResult = getMapperType().hasMultiple() ?
                newInstance(rs, startIndex) :
                Optional.ofNullable(dataMapper.cast(getMapperType().newInstance()));
        if (optionalResult.isPresent()) {
            T result = optionalResult.get();
            List<Pair<ColumnImpl, Object>> columnValues = new ArrayList<>();
            boolean somethingFetched = false;

            ColumnImpl macColumn = null;
            String mac = null;
            for (ColumnImpl column : this.getRealColumns()) {
                Object value = convertFromDb(column, rs, startIndex++);
                if (!rs.wasNull()) {
                    somethingFetched = true;
                }
                if (column.isForeignKeyPart()) {
                    columnValues.add(Pair.of(column, rs.wasNull() ? null : value));
                }
                if (column.getFieldName() != null) {
                    column.setDomainValue(result, value);
                }
                if (column.isMAC()) {
                    macColumn = column;
                    mac = (String) value;
                }
            }
            if (somethingFetched || force) {
                for (ForeignKeyConstraintImpl constraint : getTable().getReferenceConstraints()) {
                    KeyValue keyValue = createKey(constraint, columnValues);
                    constraint.setField(result, keyValue);
                }
                for (ForeignKeyConstraintImpl constraint : getTable().getReverseMappedConstraints()) {
                    constraint.setReverseField(result);
                }

                if (macColumn != null && MACEnforcementMode.Secure.equals(macEnforcementMode)) {
                    if (mac == null || !macColumn.verifyMacValue(mac, result)) {
                        throw new MacException();
                    }
                }
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private Supplier<IllegalStateException> fetchedRowWithNoValuesException() {
        return fetchedRowWithNoValuesException(dataMapper.getTable());
    }

    public static Supplier<IllegalStateException> fetchedRowWithNoValuesException(Table table) {
        return () -> new IllegalStateException("No values have been fetched from the database for the requested row in " + table.getName() + '.');
    }

    private Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
        if (column.getFieldName() == null) {
            Optional<ForeignKeyConstraintImpl> foreignKeyConstraintOptional = column.getForeignKeyConstraint();
            if (foreignKeyConstraintOptional.isPresent()) {
                return convertFromDb(findReferencedColumn(foreignKeyConstraintOptional.get().getReferencedColumns(), column), rs, index);
            }
        }
        return column.convertFromDb(rs, index);
    }

    private ColumnImpl findReferencedColumn(List<ColumnImpl> columns, ColumnImpl referenceColumn) {
        // TODO: we may need a better referenced column selector in case of multi-column foreign key
        return columns.size() == 1 ?
                columns.get(0) : // one-column foreign key
                columns.stream() // multi-column foreign key
                        .filter(column -> column.getName().equals(referenceColumn.getName())) // looking for a referenced column with the same name as reference one
                        .findAny()
                        .orElseGet(() -> columns.get(0)); // no idea what to do if not found by name
    }

    private List<ColumnImpl> getRealColumns() {
        return this.getTable().getRealColumns().collect(Collectors.toList());
    }

    private Object getValue(ColumnImpl column, List<Pair<ColumnImpl, Object>> columnValues) {
        for (Pair<ColumnImpl, Object> pair : columnValues) {
            if (column.equals(pair.getFirst())) {
                return pair.getLast();
            }
        }
        throw new IllegalArgumentException();
    }

    private KeyValue createKey(ForeignKeyConstraintImpl constraint, List<Pair<ColumnImpl, Object>> columnValues) {
        List<ColumnImpl> columns = constraint.getColumns();
        Object[] result = new Object[columns.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getValue(columns.get(i), columnValues);
        }
        return KeyValue.of(result);
    }

    private T construct(ResultSet rs, List<Setter> setters, MACEnforcementMode macEnforcementMode) throws SQLException {
        T result = construct(rs, 1, macEnforcementMode, true)
                .orElseThrow(fetchedRowWithNoValuesException());
        for (Setter setter : setters) {
            setter.set(result);
        }
        if (result instanceof PersistenceAware) {
            ((PersistenceAware) result).postLoad();
        }
        return result;
    }

    private List<ColumnImpl> getColumns() {
        return this.getRealColumns();
    }

    private List<ColumnImpl> getPrimaryKeyColumns() {
        return getTable().getPrimaryKeyColumns();
    }

    private ColumnImpl getVersionCountColumn() {
        ColumnImpl[] versionColumns = getTable().getVersionColumns();
        if (versionColumns.length == 1) {
            return versionColumns[0];
        } else {
            throw new IllegalArgumentException(versionColumns.length == 0 ? "Table has no version column" : "Table has multiple version columns");
        }
    }

    private void addFragments(List<SqlFragment> fragments, String fieldName, Object value) {
        FieldMapping mapping = getTable().getFieldMapping(fieldName);
        if (mapping == null) {
            throw new IllegalArgumentException("Invalid field " + fieldName);
        } else {
            Comparison comparison = value == null ?
                    Operator.ISNULL.compare(fieldName) :
                    Operator.EQUAL.compare(fieldName, value);
            fragments.add(mapping.asComparisonFragment(comparison, getAlias()));
        }
    }

    private void addFragments(List<SqlFragment> fragments, Comparison comparison) {
        FieldMapping mapping = getTable().getFieldMapping(comparison.getFieldName());
        if (mapping == null) {
            throw new IllegalArgumentException("Invalid field " + comparison.getFieldName());
        } else {
            fragments.add(mapping.asComparisonFragment(comparison, getAlias()));
        }
    }
}
