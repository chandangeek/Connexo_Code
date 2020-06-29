/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataMapperImpl<T> extends AbstractFinder<T> implements DataMapper<T> {

    /* contains relevant content from: select keyword from v$reserved_words where length(keyword) < 4 order by keyword
     * that will be used to generate correct table alias names from the name of the API class. */
    private static final String[] RESERVED_ALIAS_WORDS = {
            "A",
            "ABS",
            "ACL",
            "ACL",
            "ADD",
            "ALL",
            "AND",
            "ANY",
            "AS",
            "ASC",
            "AT",
            "AVG",
            "BY",
            "CHR",
            "COS",
            "CV",
            "D",
            "DAY",
            "DBA",
            "DDL",
            "DEC",
            "DML",
            "DV",
            "E",
            "EM",
            "END",
            "EXP",
            "FAR",
            "FOR",
            "G",
            "GET",
            "H",
            "HOT",
            "ID",
            "IF",
            "ILM",
            "IN",
            "INT",
            "IS",
            "JOB",
            "K",
            "KEY",
            "LAG",
            "LN",
            "LOB",
            "LOG",
            "LOW",
            "M",
            "MAX",
            "MIN",
            "MOD",
            "NAN",
            "NAV",
            "NEG",
            "NEW",
            "NO",
            "NOT",
            "NVL",
            "OF",
            "OFF",
            "OID",
            "OLD",
            "OLS",
            "ON",
            "ONE",
            "OR",
            "OWN",
            "P",
            "PER",
            "RAW",
            "RBA",
            "REF",
            "ROW",
            "SB4",
            "SCN",
            "SD",
            "SET",
            "SID",
            "SIN",
            "SQL",
            "SUM",
            "T",
            "TAG",
            "TAN",
            "THE",
            "TO",
            "TX",
            "U",
            "UB2",
            "UBA",
            "UID",
            "USE",
            "V1",
            "V2",
            "XID",
            "XML",
            "XS",
            "YES",
            "FILE"};
    private final Class<T> api;
    private final TableImpl<? super T> table;
    private final TableSqlGenerator sqlGenerator;
    private final String alias;
    private final DataMapperReader<T> reader;
    private final DataMapperWriter<T> writer;

    DataMapperImpl(Class<T> api, TableImpl<? super T> table) {
        this.api = api;
        this.table = table;
        this.sqlGenerator = new TableSqlGenerator(table);
        this.alias = createAlias(api.getName());
        this.reader = new DataMapperReader<>(this);
        this.writer = new DataMapperWriter<>(this);
    }

    private String createAlias(String apiName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < apiName.length(); i++) {
            char next = apiName.charAt(i);
            if (Character.isUpperCase(next)) {
                builder.append(next);
            }
        }
        if (builder.length() == 0) {
            builder.append('X');
        }
        String result = builder.toString().toUpperCase();
        if (Arrays.binarySearch(RESERVED_ALIAS_WORDS, result) >= 0) {
            return result + "1";
        } else {
            return result;
        }
    }

    public String getAlias() {
        return alias;
    }

    public Class<T> getApi() {
        return api;
    }

    public TableImpl<? super T> getTable() {
        return table;
    }

    public TableSqlGenerator getSqlGenerator() {
        return sqlGenerator;
    }

    private TableCache<? super T> getCache() {
        return getTable().getCache();
    }


    @Override
    Optional<T> findByPrimaryKey(KeyValue keyValue) {
        TableCache<? super T> cache = getCache();
        Object cacheVersion = cache.get(keyValue);
        //System.out.println("CACHE VERSION = "+ cacheVersion);
        /* When caching of whole table is used it is assumed that all objects from table should be in cache.
         * In no , it means that cache was invalidated or eviction time was expired. In both cases we have to load table to cache again.*/
        if (cache.reloadCacheIfNeeded(cacheVersion, (Finder)this, keyValue)){
            cacheVersion = cache.get(keyValue);
        }
        if (cacheVersion != null) {
            if (api.isInstance(cacheVersion)) {
                return Optional.of(api.cast(cacheVersion));
            } else {
                return Optional.empty();
            }
        }
        try {
            Optional<T> result = reader.findByPrimaryKey(keyValue);
            result.ifPresent(t -> cache.put(keyValue, t));
            return result;
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    int getPrimaryKeyLength() {
        return getTable().getPrimaryKeyColumns().size();
    }

    @Override
    public T lock(Object... values) {
        try {
            return reader.lock(KeyValue.of(values));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public Optional<T> lockObjectIfVersion(long version, Object... values) {
        try {
            return Optional.ofNullable(reader.lock(KeyValue.of(values), version));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public Optional<T> lockNoWait(Object... values) {
        try {
            return Optional.ofNullable(reader.lockNoWait(KeyValue.of(values)));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public List<T> find(String[] fieldNames, Object[] values, Order... orders) {
        try {
            return reader.find(fieldNames, values, orders);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    protected List<T> findWithoutMacCheck() {
        try {
            return reader.findWithoutMacCheck();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public List<JournalEntry<T>> getJournal(Object... values) {
        try {
            return reader.findJournals(KeyValue.of(values));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public JournalFinderImpl at(Instant instant) {
        if (!table.hasJournal() || !table.getColumn(TableImpl.MODTIMECOLUMNAME).isPresent()) {
            throw new IllegalStateException();
        }
        return new JournalFinderImpl(Objects.requireNonNull(instant));
    }

    public Optional<T> construct(ResultSet rs, int startIndex, boolean force) throws SQLException {
        return reader.construct(rs, startIndex, DataMapperReader.MACEnforcementMode.Secure, force);
    }

    private void preventIfChild(List<? extends T> objects) {
        if (getTable().isChildFor(objects)) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void persist(T object) {
        preventIfChild(Collections.singletonList(object));
        // do not cache object at this time, as tx may rollback
        try {
            writer.persist(object);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    // note that this will not fill back auto increment columns.
    public void persist(List<T> objects) {
        if (objects.isEmpty()) {
            return;
        }
        if (objects.size() == 1) {
            persist(objects.get(0));
            return;
        }
        preventIfChild(objects);
        // do not cache object at this time, as tx may rollback
        try {
            writer.persist(objects);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public void update(T object, String... fieldNames) {
        update(object, getUpdateColumns(fieldNames));
    }

    public void touch(T object) {
        if (table.getAutoUpdateColumns().isEmpty()) {
            throw new IllegalStateException("Nothing to touch");
        } else {
            update(object, table.getColumns().stream().filter(ColumnImpl::isMAC).collect(Collectors.toList()));
        }
    }

    private List<ColumnImpl> getUpdateColumns(String[] fieldNames) {
        if (fieldNames.length == 0) {
            return table.getStandardColumns();
        }
        List<ColumnImpl> columns = new ArrayList<>(fieldNames.length + 1);
        for (String fieldName : fieldNames) {
            FieldMapping mapping = getTable().getFieldMapping(fieldName);
            if (mapping == null) {
                throw new IllegalArgumentException("No mapping for field " + fieldName);
            }
            List<ColumnImpl> cols = mapping.getColumns();
            if (cols.isEmpty()) {
                throw new IllegalArgumentException("No columns found in mapping for field " + fieldName);
            }
            for (ColumnImpl column : cols) {
                if (column.isPrimaryKeyColumn() || column.isVersion() || column.hasUpdateValue() || column.isDiscriminator()) {
                    throw new IllegalArgumentException("Cannot update special column");
                } else {
                    columns.add(column);
                }
            }
        }
        getTable().getColumns()
                .stream()
                .filter(ColumnImpl::isMAC)
                .findAny()
                .ifPresent(columns::add);
        return columns;
    }

    private void update(T object, List<ColumnImpl> columns) {
        try {
            //if (writer.isSomethingChanged(object, reader.findByPrimaryKey(table.getPrimaryKey(object)).get(), columns)) {
            writer.update(object, columns);
            //	}
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        } finally {
            //remove object from cache, as we do not know if tx will commit or rollback
            getCache().remove(object);
        }
    }

    @Override
    public void update(List<T> objects, String... fieldNames) {
        if (objects.isEmpty()) {
            return;
        }
        if (objects.size() == 1) {
            update(objects.get(0), fieldNames);
            return;
        }
        update(objects, getUpdateColumns(fieldNames));
    }

    private void update(List<T> objects, List<ColumnImpl> columns) {
        try {
            writer.update(objects, columns);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        } finally {
            //remove objects from cache, as we do not know if tx will commit or rollback
            for (T each : objects) {
                getCache().remove(each);
            }
        }
    }

    @Override
    public void remove(T object) {
        preventIfChild(Collections.singletonList(object));
        try {
            writer.remove(object);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        } finally {
            getCache().remove(object);
        }
    }

    @Override
    public void remove(List<? extends T> objects) {
        preventIfChild(objects);
        if (objects.isEmpty()) {
            return;
        }
        try {
            writer.remove(objects);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        } finally {
            for (T each : objects) {
                getCache().remove(each);
            }
        }
    }

    QueryExecutorImpl<T> with(DataMapper<?>... dataMappers) {
        QueryExecutorImpl<T> result = new QueryExecutorImpl<>(this);
        for (DataMapper<?> each : dataMappers) {
            DataMapperImpl<?> dataMapper = (DataMapperImpl<?>) each;
            if (dataMapper.needsRestriction()) {
                throw new IllegalStateException("No Restriction allowed on additional mappers: " + dataMapper);
            } else {
                result.add(dataMapper);
            }
        }
        result.setRestriction(getMapperType().condition(getApi()));
        return result;
    }

    public QueryExecutorImpl<T> query(Class<?>... eagers) {
        return getDataModel().query(this, eagers);
    }

    public Object convert(ColumnImpl column, String value) {
        return column.convert(value);
    }

    private List<ColumnImpl> getColumns() {
        return getTable().getColumns();
    }

    private int getIndex(ColumnImpl column) {
        int i = getColumns().indexOf(column);
        if (i < 0) {
            throw new IllegalArgumentException(column.toString());
        }
        return i;
    }

    private Object getValue(ColumnImpl column, ResultSet rs, int startIndex) throws SQLException {
        int offset = getIndex(column);
        return column.convertFromDb(rs, startIndex + offset);
    }

    public KeyValue getPrimaryKey(ResultSet rs, int index) throws SQLException {
        List<ColumnImpl> primaryKeyColumns = getTable().getPrimaryKeyColumns();
        Object[] values = new Object[primaryKeyColumns.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = getValue(primaryKeyColumns.get(i), rs, index);
            if (rs.wasNull()) {
                return null;
            }
        }
        return KeyValue.of(values);
    }

    public Class<?> getType(String fieldName) {
        return getMapperType().getType(fieldName);
    }

    @Override
    public List<T> select(Condition condition, Order... orders) {
        return with().select(condition, orders);
    }

    DataMapperType<? super T> getMapperType() {
        return table.getMapperType();
    }

    public DataMapperWriter<T> getWriter() {
        return writer;
    }

    public DataMapperReader<T> getReader() {
        return reader;
    }

    @Override
    public Optional<T> getEager(Object... key) {
        return getTable().getQuery(getApi()).getOptional(key);
    }

    @Override
    public Object getAttribute(Object target, String fieldName) {
        return DomainMapper.FIELDSTRICT.get(target, fieldName);
    }

    private boolean needsRestriction() {
        return getMapperType().needsRestriction(api);
    }

    public T cast(Object object) {
        return api.cast(object);
    }

    @Override
    @Deprecated
    public List<T> find(String fieldName, Object value, String order) {
        return find(fieldName, value, Order.ascending(order));
    }

    @Override
    @Deprecated
    public List<T> find(String fieldName1, Object value1, String fieldName2, Object value2, String order) {
        return find(fieldName1, value1, fieldName2, value2, Order.ascending(order));
    }

    @Override
    @Deprecated
    public List<T> find(String[] fieldNames, Object[] values, String order, String... orders) {
        return find(fieldNames, values, Order.from(order, orders));
    }

    @Override
    @Deprecated
    public List<T> find(Map<String, Object> valueMap, String order, String... orders) {
        return find(valueMap, Order.from(order, orders));
    }

    @Override
    public Fetcher<T> fetcher(SqlBuilder builder) {
        try {
            return reader.fetcher(builder);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public SqlBuilder builder(String alias, String... hints) {
        return new SqlBuilder(getSqlGenerator().getSelectFromClause(alias, hints));
    }

    @Override
    public SqlBuilder builderWithAdditionalColumns(String alias, String... columns) {
        return new SqlBuilder(getSqlGenerator().getSelectFromClauseWithAdditionalColumns(alias, columns));
    }

    public DataModelImpl getDataModel() {
        return getTable().getDataModel();
    }

    public <R extends T> DataMapperImpl<R> subMapper(Class<R> subApi) {
        return getDataModel().mapper(subApi);
    }

    @Override
    public Optional<JournalEntry<T>> getJournalEntry(Instant instant, Object... values) {
        return getJournal(values).stream()
                .filter(journalEntry -> instant.isBefore(journalEntry.getJournalTime()))
                .reduce((previous, current) -> current);
    }

    @Override
    public Set<String> getQueryFields() {
        Set<String> result = new LinkedHashSet<>();
        Map<Column, String> constraintMapping = new HashMap<>();
        getTable().getForeignKeyConstraints().stream()
                .filter(foreignKey -> foreignKey.getFieldName() != null)
                .forEach(constraint -> constraint.getColumns()
                        .forEach(column -> constraintMapping.put(column, constraint.getFieldName())));
        getTable().getRealColumns().forEach(each -> {
            String fieldName = each.getFieldName();
            if (fieldName == null) {
                fieldName = constraintMapping.get(each);
            }
            if (fieldName != null) {
                for (int index = fieldName.indexOf('.'); index > -1; index = fieldName.indexOf('.', index + 1)) {
                    result.add(fieldName.substring(0, index));
                }
                result.add(fieldName);
            }
        });
        return result;
    }

    class JournalFinderImpl implements Finder.JournalFinder<T> {

        private final Instant instant;

        JournalFinderImpl(Instant instant) {
            this.instant = instant;
        }

        @Override
        public List<JournalEntry<T>> find(Map<String, Object> valueMap) {
            try {
                Stream<JournalEntry<T>> current = reader.find(instant, valueMap, Order.NOORDER).stream().map(JournalEntry::new);
                Stream<JournalEntry<T>> old = reader.findJournals(instant, valueMap).stream();
                return Stream.concat(current, old).collect(Collectors.toList());
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public List<JournalEntry<T>> find(List<Comparison> comparisons) {
            try {
                Stream<JournalEntry<T>> old = reader.findJournals(instant, comparisons).stream();
                return old.collect(Collectors.toList());
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
