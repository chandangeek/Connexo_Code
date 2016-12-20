package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.util.Pair;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataMapperWriter<T> {
    private final DataMapperImpl<T> dataMapper;

    DataMapperWriter(DataMapperImpl<T> dataMapper) {
        this.dataMapper = dataMapper;
    }

    private TableImpl<? super T> getTable() {
        return dataMapper.getTable();
    }

    private TableSqlGenerator getSqlGenerator() {
        return dataMapper.getSqlGenerator();
    }

    private Connection getConnection(boolean tranactionRequired) throws SQLException {
        return getTable().getDataModel().getConnection(tranactionRequired);
    }

    private long getNext(Connection connection, String sequence) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual")) {
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void closeAll(List<IOResource> resources) {
        try {
            for (IOResource resource : resources) {
                resource.close();
            }
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void persist(T object) throws SQLException {
        prepare(object, false, getTable().getDataModel().getClock().instant());
        try (Connection connection = getConnection(true)) {
            List<IOResource> resources = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().insertSql(false))) {
                int index = 1;
                Long nextVal = null;
                ColumnImpl macColumn = null;
                int macColumnIndex = 0;
                for (ColumnImpl column : getColumns()) {
                    if (column.isAutoIncrement()) {
                        nextVal = getNext(connection, column.getQualifiedSequenceName());
                        column.setDomainValue(object, nextVal);
                        statement.setObject(index++, column.hasIntValue() ? nextVal.intValue() : nextVal);
                    } else if (column.isMAC()) {
                        macColumn = column;
                        macColumnIndex = index++;
                    } else if (!column.hasInsertValue()) {
                        column.setObject(statement, index++, object).ifPresent(resources::add);
                    }
                }
                if (macColumn != null) {
                    macColumn.setMACValue(statement, macColumnIndex, object, nextVal);
                }
                statement.executeUpdate();
            } finally {
                this.closeAll(resources);
            }
        }
        refresh(object, true);
        for (ForeignKeyConstraintImpl constraint : getTable().getReverseMappedConstraints()) {
            if (constraint.isComposition()) {
                Field field = constraint.reverseField(object.getClass());
                if (field != null) {
                    DataMapperWriter writer = constraint.reverseMapper(field).getWriter();
                    List<?> toPersist = constraint.added(object, writer.needsRefreshAfterBatchInsert());
                    if (toPersist.size() == 1) {
                        writer.persist(toPersist.get(0));
                    } else {
                        writer.persist(toPersist);
                    }
                }
            }
        }
    }

    private boolean needsRefreshAfterBatchInsert() {
        return getTable().hasAutoIncrementColumns() && !getTable().hasChildren();
    }

    public void persist(List<T> objects) throws SQLException {
        if (objects.isEmpty()) {
            return;
        }
        Instant now = getTable().getDataModel().getClock().instant();
        if (getTable().hasAutoIncrementColumns() && getTable().hasChildren()) {
            for (T tuple : objects) {
                persist(tuple);
            }
            return;
        }
        try (Connection connection = getConnection(true)) {
            List<IOResource> resources = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().insertSql(true))) {
                for (T tuple : objects) {
                    prepare(tuple, false, now);
                    int index = 1;
                    for (ColumnImpl column : getColumns()) {
                        if (!column.isAutoIncrement() && !column.hasInsertValue()) {
                            column.setObject(statement, index++, tuple).ifPresent(resources::add);
                        }
                    }
                    statement.addBatch();
                }
                statement.executeBatch();
            } finally {
                this.closeAll(resources);
            }
        }
        if (getTable().hasChildren()) {
            persistChildren(objects);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void persistChildren(List<T> objects) throws SQLException {
        for (ForeignKeyConstraintImpl constraint : getTable().getReverseMappedConstraints()) {
            if (constraint.isComposition()) {
                List allParts = new ArrayList<>();
                DataMapperWriter<?> writer = null;
                for (Object object : objects) {
                    Field field = constraint.reverseField(object.getClass());
                    if (field != null) {
                        if (writer == null) {
                            writer = constraint.reverseMapper(field).getWriter();
                        }
                        List parts = constraint.added(object, writer.needsRefreshAfterBatchInsert());
                        allParts.addAll(parts);
                    }
                }
                if (writer != null) {
                    writer.persist(allParts);
                }
            }
        }
    }

    private void journal(Object object, Instant now) throws SQLException {
        String sql = getSqlGenerator().journalSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                statement.setLong(index++, now.toEpochMilli());
                index = bindPrimaryKey(statement, index, object);
                statement.executeUpdate();
            }
        }
    }

    private void journal(List<? extends T> objects, Instant now) throws SQLException {
        String sql = getSqlGenerator().journalSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (T tuple : objects) {
                    int index = 1;
                    statement.setLong(index++, now.toEpochMilli());
                    index = bindPrimaryKey(statement, index, tuple);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
    }

    void update(T object, List<ColumnImpl> columns) throws SQLException {
        Instant now = getTable().getDataModel().getClock().instant();
        if (getTable().hasJournal() && doJournal(columns)) {
            journal(object, now);
        }
        prepare(object, true, now);
        ColumnImpl[] versionCountColumns = getTable().getVersionColumns();
        List<Pair<ColumnImpl, Long>> versionCounts = new ArrayList<>(versionCountColumns.length);
        try (Connection connection = getConnection(true)) {
            String sql = getSqlGenerator().updateSql(columns);
            List<IOResource> resources = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                for (ColumnImpl column : columns) {
                    column.setObject(statement, index++, object).ifPresent(resources::add);
                }
                for (ColumnImpl column : getTable().getAutoUpdateColumns()) {
                    column.setObject(statement, index++, object).ifPresent(resources::add);
                }
                index = bindPrimaryKey(statement, index, object);
                for (ColumnImpl column : versionCountColumns) {
                    Long value = (Long) column.domainValue(object);
                    versionCounts.add(Pair.of(column, value));
                    statement.setObject(index++, value);
                }
                int result = statement.executeUpdate();
                if (result != 1) {
                    if (versionCountColumns.length == 0) {
                        throw new UnexpectedNumberOfUpdatesException(1, result, UnexpectedNumberOfUpdatesException.Operation.UPDATE);
                    } else {
                        throw new OptimisticLockException();
                    }
                }
            } finally {
                this.closeAll(resources);
            }
        }
        for (Pair<ColumnImpl, Long> pair : versionCounts) {
            pair.getFirst().setDomainValue(object, pair.getLast() + 1);
        }
        refresh(object, false);
    }

    private boolean doJournal(List<ColumnImpl> columns) {
        return columns.stream().anyMatch(ColumnImpl::alwaysJournal);
    }

    void update(List<T> objects, List<ColumnImpl> columns) throws SQLException {
        if (getTable().getVersionColumns().length > 0) {
            for (T t : objects) {
                update(t, columns);
            }
            return;
        }
        Instant now = getTable().getDataModel().getClock().instant();
        if (getTable().hasJournal() && doJournal(columns)) {
            journal(objects, now);
        }
        try (Connection connection = getConnection(true)) {
            List<IOResource> resources = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().updateSql(columns))) {
                for (T tuple : objects) {
                    prepare(tuple, true, now);
                    int index = 1;
                    for (ColumnImpl column : columns) {
                        column.setObject(statement, index++, tuple).ifPresent(resources::add);
                    }
                    for (ColumnImpl column : getTable().getAutoUpdateColumns()) {
                        column.setObject(statement, index++, tuple).ifPresent(resources::add);
                    }
                    index = bindPrimaryKey(statement, index, tuple);
                    statement.addBatch();
                }
                statement.executeBatch();
            } finally {
                this.closeAll(resources);
            }
        }
    }

    public void remove(T object) throws SQLException {
        if (getTable().hasJournal()) {
            journal(object, getTable().getDataModel().getClock().instant());
        }
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().deleteSql())) {
                int index = 1;
                index = bindPrimaryKey(statement, index, object);
                int result = statement.executeUpdate();
                if (result != 1) {
                    throw new UnexpectedNumberOfUpdatesException(1, result, UnexpectedNumberOfUpdatesException.Operation.DELETE);
                }
            }
        }
    }

    public void remove(List<? extends T> objects) throws SQLException {
        Instant now = getTable().getDataModel().getClock().instant();
        if (getTable().hasJournal()) {
            journal(objects, now);
        }
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().deleteSql())) {
                for (T tuple : objects) {
                    int index = 1;
                    index = bindPrimaryKey(statement, index, tuple);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
    }

    private void refresh(T object, boolean afterInsert) throws SQLException {
        List<ColumnImpl> columns = afterInsert ? getTable().getColumnsThatMandateRefreshAfterInsert() : getTable().getUpdateValueColumns();
        if (columns.isEmpty()) {
            return;
        }
        refresh(object, columns);
    }

    private void refresh(T object, List<ColumnImpl> columns) throws SQLException {
        try (Connection connection = getConnection(false)) {
            String sql = getSqlGenerator().refreshSql(columns);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                index = bindPrimaryKey(statement, index, object);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    int columnIndex = 1;
                    for (ColumnImpl column : columns) {
                        column.setDomainValue(object, resultSet, columnIndex++);
                    }
                }
            }
        }
    }

    private void prepare(Object target, boolean update, Instant now) {
        for (ColumnImpl each : getColumns()) {
            each.prepare(target, update, now);
        }
    }

    private List<ColumnImpl> getColumns() {
        return getTable().getRealColumns().collect(Collectors.toList());
    }

    private int bindPrimaryKey(PreparedStatement statement, int index, Object target) throws SQLException {
        for (ColumnImpl column : getTable().getPrimaryKeyColumns()) {
            // IO related resources are not supported in the primary key so ignore the result of setObject
            column.setObject(statement, index++, target);
        }
        return index;
    }

}
