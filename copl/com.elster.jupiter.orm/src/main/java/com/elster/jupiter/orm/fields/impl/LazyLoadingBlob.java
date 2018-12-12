/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.IOResource;
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
public final class LazyLoadingBlob implements Blob, IOResource {

    private final DataModel dataModel;
    private final ColumnImpl column;
    private InternalBlob actualBlob;
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

    @Override
    public void close() throws IOException {
        this.actualBlob.close();
    }

    public void bindTo(PreparedStatement statement, int index) throws SQLException {
        this.actualBlob.bindTo(statement, index);
    }

    @Override
    public void clear() {
        this.actualBlob.clear();
    }

    @Override
    public OutputStream setBinaryStream() {
        return this.actualBlob.setBinaryStream();
    }

    @Override
    public long length() {
        return this.actualBlob.length();
    }

    @Override
    public InputStream getBinaryStream() {
        return this.actualBlob.getBinaryStream();
    }

    private interface InternalBlob extends Blob, IOResource {
        void bindTo(PreparedStatement statement, int index) throws SQLException;
    }

    private static class TransientBlob implements InternalBlob {
        private final Blob actualBlob;
        private InputStream binaryStream;

        private TransientBlob(Blob actualBlob) {
            this.actualBlob = actualBlob;
        }

        @Override
        public void close() throws IOException {
            if (this.binaryStream != null) {
                this.binaryStream.close();
            }
        }

        @Override
        public void bindTo(PreparedStatement statement, int index) throws SQLException {
            this.binaryStream = this.actualBlob.getBinaryStream();
            statement.setBinaryStream(index, this.binaryStream);
        }

        @Override
        public long length() {
            return this.actualBlob.length();
        }

        @Override
        public InputStream getBinaryStream() {
            return this.actualBlob.getBinaryStream();
        }

        @Override
        public void clear() {
            this.actualBlob.clear();
        }

        @Override
        public OutputStream setBinaryStream() {
            return this.actualBlob.setBinaryStream();
        }
    }

    private class PersistentBlob implements InternalBlob {
        private boolean loaded = false;
        private java.sql.Blob databaseBlob;

        private void ensureLoaded() {
            if (!this.loaded) {
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
                        this.loaded = true;
                        // Note that the following may return null if the blob was empty on the database
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
        public void close() throws IOException {
            // No resources to close
        }

        @Override
        public long length() {
            try {
                this.ensureLoaded();
                if (this.databaseBlob != null) {
                    return this.databaseBlob.length();
                } else {
                    return 0;
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public InputStream getBinaryStream() {
            this.ensureLoaded();
            try {
                if (this.databaseBlob != null) {
                    return this.databaseBlob.getBinaryStream();
                } else {
                    return new EmptyStream();
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public void clear() {
            this.ensureLoaded();
            try {
                if (this.databaseBlob != null) {
                    this.databaseBlob.truncate(0);
                } else {
                    this.databaseBlob = dataModel.getConnection(true).createBlob();
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public OutputStream setBinaryStream() {
            this.ensureLoaded();
            try {
                return this.databaseBlob.setBinaryStream(1);
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

    }

    private static class EmptyStream extends InputStream {
        @Override
        public int read() throws IOException {
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return -1;
        }

        @Override
        public int available() throws IOException {
            return 0;
        }
    }

}