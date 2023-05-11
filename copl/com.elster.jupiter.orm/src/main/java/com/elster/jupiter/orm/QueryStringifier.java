/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.sql.SqlFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;

public class QueryStringifier implements PreparedStatement {
    private static final int BUFFER_SIZE = 200;
    private final String[] queryPieces;
    private final StringBuilder queryBuilder;
    private final int bindvarCount;

    public QueryStringifier(SqlFragment fragment) {
        queryPieces = fragment.getText().split("\\?");
        bindvarCount = queryPieces.length - 1;
        queryBuilder = new StringBuilder(queryPieces[0]);
        try {
            fragment.bind(this, 1);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    public String getQuery() {
        return queryBuilder.toString();
    }

    @Override
    public void addBatch(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearBatch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWarnings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public void closeOnCompletion() {
        // nothing to do
    }

    @Override
    public boolean execute(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean execute(String arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean execute(String arg0, int[] arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean execute(String arg0, String[] arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] executeBatch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ResultSet executeQuery(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int executeUpdate(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int executeUpdate(String arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int executeUpdate(String arg0, int[] arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int executeUpdate(String arg0, String[] arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection getConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchDirection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ResultSet getGeneratedKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxFieldSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxRows() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getMoreResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getMoreResults(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQueryTimeout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ResultSet getResultSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResultSetConcurrency() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResultSetHoldability() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResultSetType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getUpdateCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLWarning getWarnings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCloseOnCompletion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPoolable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCursorName(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEscapeProcessing(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchDirection(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchSize(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxFieldSize(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxRows(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPoolable(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setQueryTimeout(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return (T) this;
    }

    @Override
    public void addBatch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ResultSet executeQuery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int executeUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMetaData getMetaData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterMetaData getParameterMetaData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setArray(int arg0, Array arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1) {
        doSetBinaryStream(arg0, arg1, StandardCharsets.US_ASCII);
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1, int arg2) {
        setAsciiStream(arg0, arg1, (long) arg2);
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1, long arg2) {
        setAsciiStream(arg0, arg1);
    }

    @Override
    public void setBigDecimal(int arg0, BigDecimal arg1) {
        doSetString(arg0, arg1.toString());
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1) {
        doSetBinaryStream(arg0, arg1, StandardCharsets.UTF_8);
    }

    private void doSetBinaryStream(int position, InputStream stream, Charset charset) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            boolean proceed = true;
            while (proceed) {
                bytesRead = stream.read(buffer, 0, BUFFER_SIZE);
                proceed = bytesRead == BUFFER_SIZE;
                if (bytesRead != -1) {
                    stringBuilder.append(new String(buffer, 0, bytesRead, charset));
                }
            }
            setString(position, stringBuilder.toString());
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1, int arg2) {
        setBinaryStream(arg0, arg1, (long) arg2);
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1, long arg2) {
        setBinaryStream(arg0, arg1);
    }

    @Override
    public void setBlob(int arg0, Blob arg1) throws SQLException {
        setBinaryStream(arg0, arg1.getBinaryStream());
    }

    @Override
    public void setBlob(int arg0, InputStream arg1) {
        setBinaryStream(arg0, arg1);
    }

    @Override
    public void setBlob(int arg0, InputStream arg1, long arg2) {
        setBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void setBoolean(int arg0, boolean arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByte(int arg0, byte arg1) {
        setInt(arg0, arg1);
    }

    @Override
    public void setBytes(int arg0, byte[] arg1) {
        setString(arg0, new String(arg1, StandardCharsets.UTF_8));
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            boolean proceed = true;
            while (proceed) {
                charsRead = arg1.read(buffer, 0, BUFFER_SIZE);
                proceed = charsRead == BUFFER_SIZE;
                if (charsRead != -1) {
                    stringBuilder.append(new String(buffer, 0, charsRead));
                }
            }
            setString(arg0, stringBuilder.toString());
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1, int arg2) {
        setCharacterStream(arg0, arg1, (long) arg2);
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1, long arg2) {
        setCharacterStream(arg0, arg1);
    }

    @Override
    public void setClob(int arg0, Clob arg1) throws SQLException {
        setCharacterStream(arg0, arg1.getCharacterStream());
    }

    @Override
    public void setClob(int arg0, Reader arg1) {
        setCharacterStream(arg0, arg1);
    }

    @Override
    public void setClob(int arg0, Reader arg1, long arg2) {
        setCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void setDate(int arg0, Date arg1) {
        setString(arg0, arg1.toString());
    }

    @Override
    public void setDate(int arg0, Date arg1, Calendar arg2) {
        if (arg2 == null) {
            setDate(arg0, arg1);
        } else {
            Calendar calendar = Calendar.getInstance(arg2.getTimeZone());
            calendar.setTime(arg1);
            setDate(arg0, new Date(calendar.getTimeInMillis()));
        }
    }

    @Override
    public void setDouble(int arg0, double arg1) {
        doSetString(arg0, Double.toString(arg1));
    }

    @Override
    public void setFloat(int arg0, float arg1) {
        doSetString(arg0, Float.toString(arg1));
    }

    @Override
    public void setInt(int arg0, int arg1) {
        doSetString(arg0, Integer.toString(arg1));
    }

    @Override
    public void setLong(int arg0, long arg1) {
        doSetString(arg0, Long.toString(arg1));
    }

    @Override
    public void setNCharacterStream(int arg0, Reader arg1) {
        setCharacterStream(arg0, arg1);
    }

    @Override
    public void setNCharacterStream(int arg0, Reader arg1, long arg2) {
        setCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void setNClob(int arg0, NClob arg1) throws SQLException {
        setClob(arg0, arg1);
    }

    @Override
    public void setNClob(int arg0, Reader arg1) {
        setClob(arg0, arg1);
    }

    @Override
    public void setNClob(int arg0, Reader arg1, long arg2) {
        setClob(arg0, arg1, arg2);
    }

    @Override
    public void setNString(int arg0, String arg1) {
        setString(arg0, arg1);
    }

    @Override
    public void setNull(int arg0, int arg1) {
        doSetString(arg0, "null");
    }

    @Override
    public void setNull(int arg0, int arg1, String arg2) {
        setNull(arg0, arg1);
    }

    @Override
    public void setObject(int position, Object object) throws SQLException {
        if (object == null) {
            setNull(position, java.sql.Types.OTHER);
        } else {
            if (object instanceof Byte) {
                setByte(position, (Byte) object);
            } else if (object instanceof String) {
                setString(position, (String) object);
            } else if (object instanceof BigDecimal) {
                setBigDecimal(position, (BigDecimal) object);
            } else if (object instanceof Short) {
                setShort(position, (Short) object);
            } else if (object instanceof Integer) {
                setInt(position, (Integer) object);
            } else if (object instanceof Long) {
                setLong(position, (Long) object);
            } else if (object instanceof Float) {
                setFloat(position, (Float) object);
            } else if (object instanceof Double) {
                setDouble(position, (Double) object);
            } else if (object instanceof byte[]) {
                setBytes(position, (byte[]) object);
            } else if (object instanceof Date) {
                setDate(position, (Date) object);
            } else if (object instanceof Time) {
                setTime(position, (Time) object);
            } else if (object instanceof Timestamp) {
                setTimestamp(position, (Timestamp) object);
            } else if (object instanceof Instant) {
                setTimestamp(position, new Timestamp(((Instant) object).toEpochMilli()));
            } else if (object instanceof Boolean) {
                setBoolean(position, (Boolean) object);
            } else if (object instanceof InputStream) {
                setBinaryStream(position, (InputStream) object);
            } else if (object instanceof com.elster.jupiter.orm.Blob) {
                setBinaryStream(position, ((com.elster.jupiter.orm.Blob) object).getBinaryStream());
            } else if (object instanceof Blob) {
                setBlob(position, (Blob) object);
            } else if (object instanceof Clob) {
                setClob(position, (Clob) object);
            } else if (object instanceof java.util.Date) {
                setTimestamp(position, new Timestamp(((java.util.Date) object).getTime()));
            } else if (object instanceof BigInteger) {
                doSetString(position, object.toString());
            } else {
                throw new UnsupportedOperationException("Unable to bind object " + object + " on position " + position);
            }
        }
    }

    @Override
    public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
        setObject(arg0, arg1);
    }

    @Override
    public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
        setObject(arg0, arg1, arg2);
    }

    @Override
    public void setRef(int arg0, Ref arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowId(int arg0, RowId arg1) {
        setString(arg0, arg1.toString());
    }

    @Override
    public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
        setString(arg0, arg1.getString());
    }

    @Override
    public void setShort(int arg0, short arg1) {
        doSetString(arg0, Short.toString(arg1));
    }

    @Override
    public void setString(int arg0, String arg1) {
        doSetString(arg0, "'" + arg1.replace("'", "''") + "'");
    }

    private void doSetString(int position, String string) {
        if (position < 1 || position > bindvarCount) {
            throw new IndexOutOfBoundsException();
        }
        queryBuilder.append(string).append(queryPieces[position]);
    }

    @Override
    public void setTime(int arg0, Time arg1) {
        setString(arg0, arg1.toString());
    }

    @Override
    public void setTime(int arg0, Time arg1, Calendar arg2) {
        if (arg2 == null) {
            setTime(arg0, arg1);
        } else {
            Calendar calendar = Calendar.getInstance(arg2.getTimeZone());
            calendar.setTime(arg1);
            setTime(arg0, new Time(calendar.getTimeInMillis()));
        }
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1) {
        setString(arg0, arg1.toString());
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) {
        if (arg2 == null) {
            setTimestamp(arg0, arg1);
        } else {
            Calendar calendar = Calendar.getInstance(arg2.getTimeZone());
            calendar.setTime(arg1);
            setTimestamp(arg0, new Timestamp(calendar.getTimeInMillis()));
        }
    }

    @Override
    public void setURL(int arg0, URL arg1) {
        setString(arg0, arg1.toString());
    }

    @Override
    public void setUnicodeStream(int arg0, InputStream arg1, int arg2) {
        doSetBinaryStream(arg0, arg1, StandardCharsets.UTF_16);
    }
}
