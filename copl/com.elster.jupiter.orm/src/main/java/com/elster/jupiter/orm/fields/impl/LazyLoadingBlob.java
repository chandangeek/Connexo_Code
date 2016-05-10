package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Provides an implementation for the {@link Blob} interface
 * to read and write SQL BLOB values from and to the database.
 * It will replace the {@link Blob}s found in fields of objects
 * that are being created for the first time.
 * It will be injected into fields of that type
 * when objects are queried from the database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-06 (15:07)
 */
public final class LazyLoadingBlob implements Blob {

    private final DataModel dataModel;
    private final ColumnImpl column;
    private EnhancedSqlBlob actualBlob;
    private KeyValue keyValue;

    public static LazyLoadingBlob from(ColumnImpl column) {
        LazyLoadingBlob blob = new LazyLoadingBlob(column.getTable().getDataModel(), column);
        blob.prepareForLoading();
        return blob;
    }

    public static LazyLoadingBlob from(Blob simpleBlob, ColumnImpl column) {
        LazyLoadingBlob lazyLoadingBlob = new LazyLoadingBlob(column.getTable().getDataModel(), column);
        lazyLoadingBlob.initFromBlob(simpleBlob);
        return lazyLoadingBlob;
    }

    private LazyLoadingBlob(DataModel dataModel, ColumnImpl column) {
        this.dataModel = dataModel;
        this.column = column;
    }

    private void prepareForLoading() {
        this.actualBlob = new PersistentBlob();
    }

    private void initFromBlob(Blob blob) {
        this.actualBlob = new TransientBlob(blob);
    }

    private String tableName() {
        return this.column.getTable().getName();
    }

    private String columnName() {
        return this.column.getName();
    }

    public void setKeyValueFor(Object target) {
        this.keyValue = this.column.getTable().getPrimaryKey(target);
    }

    public void bindTo(PreparedStatement statement, int index) throws SQLException {
        this.actualBlob.bindTo(statement, index);
    }

    @Override
    public void clear() {
        try {
            this.actualBlob.truncate(0);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public OutputStream setBinaryStream() {
        try {
            return this.actualBlob.setBinaryStream(1);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public long length() {
        try {
            return this.actualBlob.length();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public InputStream getBinaryStream() {
        try {
            return this.actualBlob.getBinaryStream();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) {
        try {
            return actualBlob.getBinaryStream(pos, length);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private interface EnhancedSqlBlob extends java.sql.Blob {
        void bindTo(PreparedStatement statement, int index) throws SQLException;
    }

    private static class TransientBlob implements EnhancedSqlBlob {
        private final Blob simpleBlob;

        private TransientBlob(Blob simpleBlob) {
            this.simpleBlob = simpleBlob;
        }

        @Override
        public void bindTo(PreparedStatement statement, int index) throws SQLException {
            statement.setBinaryStream(index, this.simpleBlob.getBinaryStream());
        }

        @Override
        public long length() throws SQLException {
            return this.simpleBlob.length();
        }

        @Override
        public byte[] getBytes(long pos, int length) throws SQLException {
            byte[] bytes = new byte[length];
            InputStream stream = this.getBinaryStream(pos, length);
            int b;
            int index = 0;
            try {
                while ((b = stream.read()) != -1) {
                    bytes[index] = (byte) b;
                    index++;
                }
                return bytes;
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }

        @Override
        public InputStream getBinaryStream() throws SQLException {
            return this.simpleBlob.getBinaryStream();
        }

        @Override
        public InputStream getBinaryStream(long pos, long length) throws SQLException {
            return this.simpleBlob.getBinaryStream(pos, length);
        }

        @Override
        public void free() throws SQLException {
            // Nothing to free
        }

        @Override
        public void truncate(long len) throws SQLException {
            throw new UnsupportedOperationException("Not expecting the jdbc layer to be calling truncate while persisting a BLOB");
        }

        @Override
        public OutputStream setBinaryStream(long pos) throws SQLException {
            throw new UnsupportedOperationException("Not expecting the jdbc layer to be calling setBinaryStream while persisting a BLOB");
        }

        @Override
        public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
            throw new UnsupportedOperationException("Not expecting the jdbc layer to be calling setBytes while persisting a BLOB");
        }

        @Override
        public int setBytes(long pos, byte[] bytes) throws SQLException {
            throw new UnsupportedOperationException("Not expecting the jdbc layer to be calling setBytes while persisting a BLOB");
        }

        @Override
        public long position(byte[] pattern, long start) throws SQLException {
            throw new UnsupportedOperationException("Not expecting the jdbc layer to be calling position while persisting a BLOB");
        }

        @Override
        public long position(java.sql.Blob pattern, long start) throws SQLException {
            throw new UnsupportedOperationException("Not expecting the jdbc layer to be calling position while persisting a BLOB");
        }
    }

    private class PersistentBlob implements EnhancedSqlBlob {
        private java.sql.Blob databaseBlob;

        private void ensureLoaded() {
            if (this.databaseBlob == null) {
                this.loadBlob();
            }
        }

        private void loadBlob() {
            if (keyValue == null) {
                throw new IllegalStateException("KeyValue not inject yet into PersistentBlob for column " + columnName() + " of table " + tableName());
            }
            try {
                try (Connection connection = dataModel.getConnection(false)) {
                    SqlBuilder sqlBuilder = new SqlBuilder("SELECT ");
                    sqlBuilder.append(columnName());
                    sqlBuilder.append(" FROM ");
                    sqlBuilder.append(tableName());
                    sqlBuilder.append(" WHERE ");
                    this.appendPrimaryKey(sqlBuilder);
                    this.execute(sqlBuilder, connection);
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        private void appendPrimaryKey(SqlBuilder sqlBuilder) {
            DualIterable<ColumnImpl, Object> keyColumnAndValues =
                    DualIterable.endWithShortest(
                            column.getTable().getPrimaryKeyColumns(),
                            Arrays.asList(keyValue.getKey()));
            keyColumnAndValues.forEach(keyColumnAndValue -> this.appendPrimaryKey(sqlBuilder, keyColumnAndValue));
        }

        private void appendPrimaryKey(SqlBuilder sqlBuilder, Pair<ColumnImpl, Object> keyColumnAndValue) {
            sqlBuilder.append(keyColumnAndValue.getFirst().getName());
            sqlBuilder.append(" = ");
            sqlBuilder.addObject(keyColumnAndValue.getFirst().convertToDb(keyColumnAndValue.getLast()));
        }

        private void execute(SqlBuilder sqlBuilder, Connection connection) throws SQLException {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        this.databaseBlob = resultSet.getBlob(1);
                    } else {
                        throw new UnsupportedOperationException("Owning object for blob in column " + columnName() + " of table " + tableName() + " identified by " + keyValue + " no longer exists");
                    }
                }
            }
        }

        @Override
        public void bindTo(PreparedStatement statement, int index) throws SQLException {
            /* Binding is only called if field was really updated
             * because all blob fields (i.e. the ones created with com.elster.jupiter.orm.Column.Builder.blob())
             * are skipped on update so the owning business object is forced
             * to use com.elster.jupiter.orm.DataModel#update(Object, String...) with the
             * name of the blob field that should be updated.
             * Therefore, the client call will also have already called
             * one of the methods that modify the actual blob. */
            statement.setBlob(index, this.databaseBlob);
        }

        @Override
        public long length() {
            try {
                this.ensureLoaded();
                return this.databaseBlob.length();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public byte[] getBytes(long pos, int length) throws SQLException {
            this.ensureLoaded();
            return this.databaseBlob.getBytes(pos, length);
        }

        @Override
        public InputStream getBinaryStream() {
            this.ensureLoaded();
            return this.getBinaryStream(1, this.length());
        }

        @Override
        public InputStream getBinaryStream(long position, long length) {
            try {
                this.ensureLoaded();
                return this.databaseBlob.getBinaryStream(position, length);
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public long position(java.sql.Blob pattern, long start) throws SQLException {
            this.ensureLoaded();
            return this.databaseBlob.position(pattern, start);
        }

        @Override
        public long position(byte[] pattern, long start) throws SQLException {
            this.ensureLoaded();
            return this.databaseBlob.position(pattern, start);
        }

        @Override
        public void truncate(long len) throws SQLException {
            this.ensureLoaded();
            this.databaseBlob.truncate(len);
        }

        @Override
        public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
            this.ensureLoaded();
            return this.databaseBlob.setBytes(pos, bytes, offset, len);
        }

        @Override
        public int setBytes(long pos, byte[] bytes) throws SQLException {
            this.ensureLoaded();
            return this.databaseBlob.setBytes(pos, bytes);
        }

        @Override
        public OutputStream setBinaryStream(long pos) throws SQLException {
            this.ensureLoaded();
            return this.databaseBlob.setBinaryStream(pos);
        }

        @Override
        public void free() throws SQLException {
            if (this.databaseBlob != null) {
                this.databaseBlob.free();
            }
        }
    }

}