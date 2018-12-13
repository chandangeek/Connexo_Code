/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ResultSetWrapper implements ResultSet {
	
	private final ResultSet resultSet;
	private final Statement statement;
	
	ResultSetWrapper(ResultSet resultSet, Statement statement) {
		this.resultSet = resultSet;
		this.statement = statement;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
	    return iface.isInstance(this) || resultSet.isWrapperFor(iface);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.isInstance(this) ? (T) this : resultSet.unwrap(iface);
	}

	@Override
	public boolean absolute(int arg0) throws SQLException {
		return resultSet.absolute(arg0);
	}

	@Override
	public void afterLast() throws SQLException {
		resultSet.afterLast();
	}

	@Override
	public void beforeFirst() throws SQLException {
		resultSet.beforeFirst();

	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		resultSet.cancelRowUpdates();
	}

	@Override
	public void clearWarnings() throws SQLException {
		resultSet.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		resultSet.close();
	}

	@Override
	public void deleteRow() throws SQLException {
		resultSet.deleteRow();
	}

	@Override
	public int findColumn(String arg0) throws SQLException {
		return resultSet.findColumn(arg0);
	}

	@Override
	public boolean first() throws SQLException {
		return resultSet.first();
	}

	@Override
	public Array getArray(int arg0) throws SQLException {
		return resultSet.getArray(arg0);
	}

	@Override
	public Array getArray(String arg0) throws SQLException {
		return resultSet.getArray(arg0);
	}

	@Override
	public InputStream getAsciiStream(int arg0) throws SQLException {
		return resultSet.getAsciiStream(arg0);	}

	@Override
	public InputStream getAsciiStream(String arg0) throws SQLException {
		return resultSet.getAsciiStream(arg0);
	}

	@Override
	public BigDecimal getBigDecimal(int arg0) throws SQLException {
		return resultSet.getBigDecimal(arg0);
	}

	@Override
	public BigDecimal getBigDecimal(String arg0) throws SQLException {
		return resultSet.getBigDecimal(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
		return resultSet.getBigDecimal(arg0,arg1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
		return resultSet.getBigDecimal(arg0,arg1);
	}

	@Override
	public InputStream getBinaryStream(int arg0) throws SQLException {
		return resultSet.getBinaryStream(arg0);
	}

	@Override
	public InputStream getBinaryStream(String arg0) throws SQLException {
		return resultSet.getBinaryStream(arg0);
	}

	@Override
	public Blob getBlob(int arg0) throws SQLException {
		return resultSet.getBlob(arg0);
	}

	@Override
	public Blob getBlob(String arg0) throws SQLException {
		return resultSet.getBlob(arg0);
	}

	@Override
	public boolean getBoolean(int arg0) throws SQLException {
		return resultSet.getBoolean(arg0);
	}

	@Override
	public boolean getBoolean(String arg0) throws SQLException {
		return resultSet.getBoolean(arg0);
	}

	@Override
	public byte getByte(int arg0) throws SQLException {
		return resultSet.getByte(arg0);
	}

	@Override
	public byte getByte(String arg0) throws SQLException {		
		return resultSet.getByte(arg0);
	}

	@Override
	public byte[] getBytes(int arg0) throws SQLException {
		return resultSet.getBytes(arg0);
	}

	@Override
	public byte[] getBytes(String arg0) throws SQLException {		
		return resultSet.getBytes(arg0);
	}

	@Override
	public Reader getCharacterStream(int arg0) throws SQLException {
		return resultSet.getCharacterStream(arg0);
	}

	@Override
	public Reader getCharacterStream(String arg0) throws SQLException {		
		return resultSet.getCharacterStream(arg0);
	}

	@Override
	public Clob getClob(int arg0) throws SQLException {
		return resultSet.getClob(arg0);
	}

	@Override
	public Clob getClob(String arg0) throws SQLException {
		return resultSet.getClob(arg0);
	}

	@Override
	public int getConcurrency() throws SQLException {
		return resultSet.getConcurrency();
	}

	@Override
	public String getCursorName() throws SQLException {
		return resultSet.getCursorName();
	}

	@Override
	public Date getDate(int arg0) throws SQLException {
		return resultSet.getDate(arg0);
	}

	@Override
	public Date getDate(String arg0) throws SQLException {
		return resultSet.getDate(arg0);
	}

	@Override
	public Date getDate(int arg0, Calendar arg1) throws SQLException {
		return resultSet.getDate(arg0,arg1);	
	}

	@Override
	public Date getDate(String arg0, Calendar arg1) throws SQLException {
		return resultSet.getDate(arg0,arg1);
	}

	@Override
	public double getDouble(int arg0) throws SQLException {
		return resultSet.getDouble(arg0);
	}

	@Override
	public double getDouble(String arg0) throws SQLException {
		return resultSet.getDouble(arg0);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return resultSet.getFetchDirection();
	}

	@Override
	public int getFetchSize() throws SQLException {
		return resultSet.getFetchSize();
	}

	@Override
	public float getFloat(int arg0) throws SQLException {
		return resultSet.getFloat(arg0);
	}

	@Override
	public float getFloat(String arg0) throws SQLException {
		return resultSet.getFloat(arg0);
	}

	@Override
	public int getHoldability() throws SQLException {
		return resultSet.getHoldability();
	}

	@Override
	public int getInt(int arg0) throws SQLException {
		return resultSet.getInt(arg0);
	}
	
	@Override
	public int getInt(String arg0) throws SQLException {
		return resultSet.getInt(arg0);
	}

	@Override
	public long getLong(int arg0) throws SQLException {
		return resultSet.getLong(arg0);
	}

	@Override
	public long getLong(String arg0) throws SQLException {
		return resultSet.getLong(arg0);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return resultSet.getMetaData();
	}


	@Override
	public Reader getNCharacterStream(int arg0) throws SQLException {
		return resultSet.getNCharacterStream(arg0);
	}

	@Override
	public Reader getNCharacterStream(String arg0) throws SQLException {
		return resultSet.getNCharacterStream(arg0);
	}

	@Override
	public NClob getNClob(int arg0) throws SQLException {
		return resultSet.getNClob(arg0);
	}

	@Override
	public NClob getNClob(String arg0) throws SQLException {
		return resultSet.getNClob(arg0);
	}

	@Override
	public String getNString(int arg0) throws SQLException {
		return resultSet.getNString(arg0);
	}

	@Override
	public String getNString(String arg0) throws SQLException {
		return resultSet.getNString(arg0);
	}

	@Override
	public Object getObject(int arg0) throws SQLException {
		return resultSet.getObject(arg0);
	}

	@Override
	public Object getObject(String arg0) throws SQLException {
		return resultSet.getObject(arg0);
	}

	@Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
		return resultSet.getObject(arg0,arg1);
	}

	@Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
		return resultSet.getObject(arg0,arg1);
	}

	@Override
	public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
		return resultSet.getObject(arg0,arg1);
	}

	@Override
	public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
		return resultSet.getObject(arg0,arg1);
	}

	@Override
	public Ref getRef(int arg0) throws SQLException {
		return resultSet.getRef(arg0);
	}

	@Override
	public Ref getRef(String arg0) throws SQLException {
		return resultSet.getRef(arg0);
	}

	@Override
	public int getRow() throws SQLException {
		return resultSet.getRow();
	}

	@Override
	public RowId getRowId(int arg0) throws SQLException {
		return resultSet.getRowId(arg0);
	}

	@Override
	public RowId getRowId(String arg0) throws SQLException {
		return resultSet.getRowId(arg0);
	}

	@Override
	public SQLXML getSQLXML(int arg0) throws SQLException {
		return resultSet.getSQLXML(arg0);
	}

	@Override
	public SQLXML getSQLXML(String arg0) throws SQLException {
		return resultSet.getSQLXML(arg0);
	}

	@Override
	public short getShort(int arg0) throws SQLException {
		return resultSet.getShort(arg0);
	}

	@Override
	public short getShort(String arg0) throws SQLException {
		return resultSet.getShort(arg0);
	}

	@Override
	public final Statement getStatement() throws SQLException {
		return statement;
	}

	@Override
	public String getString(int arg0) throws SQLException {
		return resultSet.getString(arg0);
	}

	@Override
	public String getString(String arg0) throws SQLException {
		return resultSet.getString(arg0);
	}

	@Override
	public Time getTime(int arg0) throws SQLException {
		return resultSet.getTime(arg0);
	}

	@Override
	public Time getTime(String arg0) throws SQLException {
		return resultSet.getTime(arg0);
	}

	@Override
	public Time getTime(int arg0, Calendar arg1) throws SQLException {
		return resultSet.getTime(arg0,arg1);
	}

	@Override
	public Time getTime(String arg0, Calendar arg1) throws SQLException {
		return resultSet.getTime(arg0,arg1);
	}

	@Override
	public Timestamp getTimestamp(int arg0) throws SQLException {
		return resultSet.getTimestamp(arg0);
	}

	@Override
	public Timestamp getTimestamp(String arg0) throws SQLException {
		return resultSet.getTimestamp(arg0);
	}

	@Override
	public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
		return resultSet.getTimestamp(arg0,arg1);
	}

	@Override
	public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException {
		return resultSet.getTimestamp(arg0,arg1);
	}

	@Override
	public int getType() throws SQLException {
		return resultSet.getType();
	}

	@Override
	public URL getURL(int arg0) throws SQLException {
		return resultSet.getURL(arg0);
	}

	@Override
	public URL getURL(String arg0) throws SQLException {
		return resultSet.getURL(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputStream getUnicodeStream(int arg0) throws SQLException {
		return resultSet.getUnicodeStream(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public InputStream getUnicodeStream(String arg0) throws SQLException {
		return resultSet.getUnicodeStream(arg0);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return resultSet.getWarnings();
	}

	@Override
	public void insertRow() throws SQLException {
		resultSet.insertRow();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return resultSet.isAfterLast();
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return resultSet.isBeforeFirst();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return resultSet.isClosed();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return resultSet.isFirst();
	}

	@Override
	public boolean isLast() throws SQLException {
		return resultSet.isLast();
	}

	@Override
	public boolean last() throws SQLException {
		return resultSet.last();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		resultSet.moveToCurrentRow();
	}


	@Override
	public void moveToInsertRow() throws SQLException {
		resultSet.moveToInsertRow();
	}

	@Override
	public boolean next() throws SQLException {
		return resultSet.next();
	}

	@Override
	public boolean previous() throws SQLException {
		return resultSet.previous();
	}

	@Override
	public void refreshRow() throws SQLException {
		resultSet.refreshRow();
	}

	@Override
	public boolean relative(int arg0) throws SQLException {
		return resultSet.relative(arg0);
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return resultSet.rowDeleted();
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return resultSet.rowInserted();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return resultSet.rowUpdated();
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException {
		resultSet.setFetchDirection(arg0);
	}

	@Override
	public void setFetchSize(int arg0) throws SQLException {
		resultSet.setFetchSize(arg0);
	}

	@Override
	public void updateArray(int arg0, Array arg1) throws SQLException {
		resultSet.updateArray(arg0,arg1);
	}
	
	@Override
	public void updateArray(String arg0, Array arg1) throws SQLException {
		resultSet.updateArray(arg0,arg1);
	}
	
	@Override
	public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
		resultSet.updateAsciiStream(arg0,arg1);
	}
	
	@Override
	public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
		resultSet.updateAsciiStream(arg0,arg1);
	}
	
	@Override
	public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		resultSet.updateAsciiStream(arg0,arg1,arg2);
	}
	
	@Override
	public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
		resultSet.updateAsciiStream(arg0,arg1,arg2);
	}

	@Override
	public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		resultSet.updateAsciiStream(arg0,arg1,arg2);
	}

	@Override
	public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
		resultSet.updateAsciiStream(arg0,arg1,arg2);
	}

	@Override
	public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
		resultSet.updateBigDecimal(arg0,arg1);
	}

	@Override
	public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
		resultSet.updateBigDecimal(arg0,arg1);
	}

	@Override
	public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
		resultSet.updateBinaryStream(arg0,arg1);
	}

	@Override
	public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
		resultSet.updateBinaryStream(arg0,arg1);
	}
	
	@Override
	public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		resultSet.updateBinaryStream(arg0,arg1,arg2);
	}
	
	@Override
	public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
		resultSet.updateBinaryStream(arg0,arg1,arg2);
	}
	
	@Override
	public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		resultSet.updateBinaryStream(arg0,arg1,arg2);
	}

	@Override
	public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
		resultSet.updateBinaryStream(arg0,arg1,arg2);
	}
	
	@Override
	public void updateBlob(int arg0, Blob arg1) throws SQLException {
		resultSet.updateBlob(arg0,arg1);
	}

	@Override
	public void updateBlob(String arg0, Blob arg1) throws SQLException {
		resultSet.updateBlob(arg0,arg1);
	}

	@Override
	public void updateBlob(int arg0, InputStream arg1) throws SQLException {
		resultSet.updateBlob(arg0,arg1);
	}

	@Override
	public void updateBlob(String arg0, InputStream arg1) throws SQLException {
		resultSet.updateBlob(arg0,arg1);
	}

	@Override
	public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
		resultSet.updateBlob(arg0,arg1,arg2);
	}

	@Override
	public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
		resultSet.updateBlob(arg0,arg1,arg2);
	}

	@Override
	public void updateBoolean(int arg0, boolean arg1) throws SQLException {
		resultSet.updateBoolean(arg0,arg1);
	}

	@Override
	public void updateBoolean(String arg0, boolean arg1) throws SQLException {
		resultSet.updateBoolean(arg0,arg1);
	}

	@Override
	public void updateByte(int arg0, byte arg1) throws SQLException {
		resultSet.updateByte(arg0,arg1);
	}

	@Override
	public void updateByte(String arg0, byte arg1) throws SQLException {
		resultSet.updateByte(arg0,arg1);
	}

	@Override
	public void updateBytes(int arg0, byte[] arg1) throws SQLException {
		resultSet.updateBytes(arg0,arg1);
	}

	@Override
	public void updateBytes(String arg0, byte[] arg1) throws SQLException {
		resultSet.updateBytes(arg0,arg1);
	}

	@Override
	public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
		resultSet.updateCharacterStream(arg0,arg1);
	}

	@Override
	public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
		resultSet.updateCharacterStream(arg0,arg1);
	}

	@Override
	public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
		resultSet.updateCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
		resultSet.updateCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void updateClob(int arg0, Clob arg1) throws SQLException {
		resultSet.updateClob(arg0,arg1);
	}


	@Override
	public void updateClob(String arg0, Clob arg1) throws SQLException {
		resultSet.updateClob(arg0,arg1);
	}

	@Override
	public void updateClob(int arg0, Reader arg1) throws SQLException {
		resultSet.updateClob(arg0,arg1);
	}

	@Override
	public void updateClob(String arg0, Reader arg1) throws SQLException {
		resultSet.updateClob(arg0,arg1);
	}

	@Override
	public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateClob(arg0,arg1,arg2);
	}

	@Override
	public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateClob(arg0,arg1,arg2);
	}

	@Override
	public void updateDate(int arg0, Date arg1) throws SQLException {
		resultSet.updateDate(arg0,arg1);
	}

	@Override
	public void updateDate(String arg0, Date arg1) throws SQLException {
		resultSet.updateDate(arg0,arg1);
	}

	@Override
	public void updateDouble(int arg0, double arg1) throws SQLException {
		resultSet.updateDouble(arg0,arg1);
	}

	@Override
	public void updateDouble(String arg0, double arg1) throws SQLException {
		resultSet.updateDouble(arg0,arg1);
	}

	@Override
	public void updateFloat(int arg0, float arg1) throws SQLException {
		resultSet.updateFloat(arg0,arg1);
	}

	@Override
	public void updateFloat(String arg0, float arg1) throws SQLException {
		resultSet.updateFloat(arg0,arg1);
	}

	@Override
	public void updateInt(int arg0, int arg1) throws SQLException {
		resultSet.updateInt(arg0,arg1);
	}

	@Override
	public void updateInt(String arg0, int arg1) throws SQLException {
		resultSet.updateInt(arg0,arg1);
	}

	@Override
	public void updateLong(int arg0, long arg1) throws SQLException {
		resultSet.updateLong(arg0,arg1);
	}

	@Override
	public void updateLong(String arg0, long arg1) throws SQLException {
		resultSet.updateLong(arg0,arg1);
	}

	@Override
	public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
		resultSet.updateNCharacterStream(arg0,arg1);
	}

	@Override
	public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
		resultSet.updateNCharacterStream(arg0,arg1);
	}

	@Override
	public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateNCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateNCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void updateNClob(int arg0, NClob arg1) throws SQLException {
		resultSet.updateNClob(arg0,arg1);
	}


	@Override
	public void updateNClob(String arg0, NClob arg1) throws SQLException {
		resultSet.updateNClob(arg0,arg1);
	}

	@Override
	public void updateNClob(int arg0, Reader arg1) throws SQLException {
		resultSet.updateNClob(arg0,arg1);
	}

	@Override
	public void updateNClob(String arg0, Reader arg1) throws SQLException {
		resultSet.updateNClob(arg0,arg1);
	}

	@Override
	public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateNClob(arg0,arg1,arg2);
	}

	@Override
	public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
		resultSet.updateNClob(arg0,arg1,arg2);
	}

	@Override
	public void updateNString(int arg0, String arg1) throws SQLException {
		resultSet.updateNString(arg0,arg1);
	}

	@Override
	public void updateNString(String arg0, String arg1) throws SQLException {
		resultSet.updateNString(arg0,arg1);
	}

	@Override
	public void updateNull(int arg0) throws SQLException {
		resultSet.updateNull(arg0);
	}

	@Override
	public void updateNull(String arg0) throws SQLException {
		resultSet.updateNull(arg0);
	}

	@Override
	public void updateObject(int arg0, Object arg1) throws SQLException {
		resultSet.updateObject(arg0,arg1);
	}

	@Override
	public void updateObject(String arg0, Object arg1) throws SQLException {
		resultSet.updateObject(arg0,arg1);
	}

	@Override
	public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
		resultSet.updateObject(arg0,arg1,arg2);
	}

	@Override
	public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
		resultSet.updateObject(arg0,arg1,arg2);
	}

	@Override
	public void updateRef(int arg0, Ref arg1) throws SQLException {
		resultSet.updateRef(arg0,arg1);
	}

	@Override
	public void updateRef(String arg0, Ref arg1) throws SQLException {
		resultSet.updateRef(arg0,arg1);
	}

	@Override
	public void updateRow() throws SQLException {
		resultSet.updateRow();
	}

	@Override
	public void updateRowId(int arg0, RowId arg1) throws SQLException {
		resultSet.updateRowId(arg0,arg1);
	}

	@Override
	public void updateRowId(String arg0, RowId arg1) throws SQLException {
		resultSet.updateRowId(arg0,arg1);
	}

	@Override
	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
		resultSet.updateSQLXML(arg0,arg1);
	}

	@Override
	public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
		resultSet.updateSQLXML(arg0,arg1);
	}

	@Override
	public void updateShort(int arg0, short arg1) throws SQLException {
		resultSet.updateShort(arg0,arg1);
	}

	@Override
	public void updateShort(String arg0, short arg1) throws SQLException {
		resultSet.updateShort(arg0,arg1);
	}

	@Override
	public void updateString(int arg0, String arg1) throws SQLException {
		resultSet.updateString(arg0,arg1);
	}

	@Override
	public void updateString(String arg0, String arg1) throws SQLException {
		resultSet.updateString(arg0,arg1);
	}

	@Override
	public void updateTime(int arg0, Time arg1) throws SQLException {
		resultSet.updateTime(arg0,arg1);
	}


	@Override
	public void updateTime(String arg0, Time arg1) throws SQLException {
		resultSet.updateTime(arg0,arg1);
	}

	@Override
	public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
		resultSet.updateTimestamp(arg0,arg1);
	}

	@Override
	public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
		resultSet.updateTimestamp(arg0,arg1);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return resultSet.wasNull();
	}


}
