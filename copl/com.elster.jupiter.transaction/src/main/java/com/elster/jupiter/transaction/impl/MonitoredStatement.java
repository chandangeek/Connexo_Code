/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.elster.jupiter.transaction.SqlEvent;
import com.elster.jupiter.util.time.StopWatch;

class MonitoredStatement extends PreparedStatementWrapper {

    private final List<Object> parameters = new ArrayList<>();
    private final StopWatch stopWatch;
    private final String text;
    private MonitoredResultSet resultSet;
    private final TransactionServiceImpl transactionService;
    private int batchCount;
    private int rowCount;

    MonitoredStatement(TransactionServiceImpl transactionService, PreparedStatement statement, Connection connection, String text) {
    	super(statement,connection);
    	this.transactionService = transactionService;
        this.text = text;
        this.stopWatch = new StopWatch();
    }

    private void add(int offset, Object value) {
        int zeroBasedOffset = offset - 1;
        if (parameters.size() == zeroBasedOffset) {
            parameters.add(value);
            return;
        }
        if (parameters.size() > zeroBasedOffset) {
            parameters.set(zeroBasedOffset, value);
            return;
        }
        for (int i = parameters.size(); i < zeroBasedOffset; i++) {
            parameters.add(null);
        }
        parameters.add(value);
    }

    @Override
    ResultSet wrap(ResultSet resultSet) {
        this.resultSet = new MonitoredResultSet(resultSet,this);
        return this.resultSet;
    }

  
    @Override
    public void close() throws SQLException {
        stopWatch.stop();
        super.close();
        int fetchCount = resultSet == null ? -1 : resultSet.getFetchCount();
        SqlEvent event = new SqlEvent(stopWatch, text, parameters, fetchCount, rowCount, batchCount);
        transactionService.publish(event);
        if (transactionService.printSql()) {
        	System.out.println(event.print());
        }
    }

    @Override
    public void setArray(int arg0, Array arg1) throws SQLException {
        add(arg0, arg1);
        super.setArray(arg0, arg1);
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
        add(arg0, arg1);
        super.setAsciiStream(arg0, arg1);
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        add(arg0, arg1);
        super.setAsciiStream(arg0, arg1, arg2);
    }

    @Override
    public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setAsciiStream(arg0, arg1, arg2);
    }

    @Override
    public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
        add(arg0, arg1);
        super.setBigDecimal(arg0, arg1);
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
        add(arg0, arg1);
        super.setBinaryStream(arg0, arg1);
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        add(arg0, arg1);
        super.setBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setBinaryStream(arg0, arg1, arg2);
    }

    @Override
    public void setBlob(int arg0, Blob arg1) throws SQLException {
        add(arg0, arg1);
        super.setBlob(arg0, arg1);
    }

    @Override
    public void setBlob(int arg0, InputStream arg1) throws SQLException {
        add(arg0, arg1);
        super.setBlob(arg0, arg1);
    }

    @Override
    public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setBlob(arg0, arg1, arg2);
    }

    @Override
    public void setBoolean(int arg0, boolean arg1) throws SQLException {
        add(arg0, arg1);
        super.setBoolean(arg0, arg1);
    }

    @Override
    public void setByte(int arg0, byte arg1) throws SQLException {
        add(arg0, arg1);
        super.setByte(arg0, arg1);
    }

    @Override
    public void setBytes(int arg0, byte[] arg1) throws SQLException {
        add(arg0, arg1);
        super.setBytes(arg0, arg1);
    }

    public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
        add(arg0, arg1);
        super.setCharacterStream(arg0, arg1);
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
        add(arg0, arg1);
        super.setCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void setClob(int arg0, Clob arg1) throws SQLException {
        add(arg0, arg1);
        super.setClob(arg0, arg1);
    }

    @Override
    public void setClob(int arg0, Reader arg1) throws SQLException {
        add(arg0, arg1);
        super.setClob(arg0, arg1);
    }

    @Override
    public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setClob(arg0, arg1, arg2);
    }

    @Override
    public void setDate(int arg0, Date arg1) throws SQLException {
        add(arg0, arg1);
        super.setDate(arg0, arg1);
    }

    @Override
    public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
        add(arg0, arg1);
        super.setDate(arg0, arg1, arg2);
    }

    @Override
    public void setDouble(int arg0, double arg1) throws SQLException {
        add(arg0, arg1);
        super.setDouble(arg0, arg1);
    }

    @Override
    public void setFloat(int arg0, float arg1) throws SQLException {
        add(arg0, arg1);
        super.setFloat(arg0, arg1);
    }

    @Override
    public void setInt(int arg0, int arg1) throws SQLException {
        add(arg0, arg1);
        super.setInt(arg0, arg1);
    }

    @Override
    public void setLong(int arg0, long arg1) throws SQLException {
        add(arg0, arg1);
        super.setLong(arg0, arg1);
    }

    @Override
    public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
        add(arg0, arg1);
        super.setNCharacterStream(arg0, arg1);
    }

    @Override
    public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setNCharacterStream(arg0, arg1, arg2);
    }

    @Override
    public void setNClob(int arg0, NClob arg1) throws SQLException {
        add(arg0, arg1);
        super.setNClob(arg0, arg1);
    }

    @Override
    public void setNClob(int arg0, Reader arg1) throws SQLException {
        add(arg0, arg1);
        super.setNClob(arg0, arg1);
    }

    @Override
    public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
        add(arg0, arg1);
        super.setNClob(arg0, arg1, arg2);
    }

    @Override
    public void setNString(int arg0, String arg1) throws SQLException {
        add(arg0, arg1);
        super.setNString(arg0, arg1);
    }

    @Override
    public void setNull(int arg0, int arg1) throws SQLException {
        add(arg0, null);
        super.setNull(arg0, arg1);
    }

    @Override
    public void setNull(int arg0, int arg1, String arg2) throws SQLException {
        add(arg0, null);
        super.setNull(arg0, arg1, arg2);
    }

    @Override
    public void setObject(int arg0, Object arg1) throws SQLException {
        add(arg0, arg1);
        super.setObject(arg0, arg1);
    }

    @Override
    public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
        add(arg0, arg1);
        super.setObject(arg0, arg1, arg2);
    }

    @Override
    public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
        add(arg0, arg1);
        super.setObject(arg0, arg1, arg2, arg3);
    }

    @Override
    public void setRef(int arg0, Ref arg1) throws SQLException {
        add(arg0, arg1);
        super.setRef(arg0, arg1);
    }

    @Override
    public void setRowId(int arg0, RowId arg1) throws SQLException {
        add(arg0, arg1);
        super.setRowId(arg0, arg1);
    }

    @Override
    public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
        add(arg0, arg1);
        super.setSQLXML(arg0, arg1);
    }

    @Override
    public void setShort(int arg0, short arg1) throws SQLException {
        add(arg0, arg1);
        super.setShort(arg0, arg1);
    }

    @Override
    public void setString(int arg0, String arg1) throws SQLException {
        add(arg0, arg1);
        super.setString(arg0, arg1);        // TODO Auto-generated method stub
    }

    @Override
    public void setTime(int arg0, Time arg1) throws SQLException {
        add(arg0, arg1);
        super.setTime(arg0, arg1);
    }

    @Override
    public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
        add(arg0, arg1);
        super.setTime(arg0, arg1, arg2);
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
        add(arg0, arg1);
        super.setTimestamp(arg0, arg1);
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
        add(arg0, arg1);
        super.setTimestamp(arg0, arg1);
    }

    @Override
    public void setURL(int arg0, URL arg1) throws SQLException {
        add(arg0, arg1);
        super.setURL(arg0, arg1);
    }

    @Override
    public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
        add(arg0, arg1);
        super.setUnicodeStream(arg0, arg1, arg2);
    }

    @Override
    public int executeUpdate() throws SQLException {
    	int result = super.executeUpdate();
    	rowCount += result;
    	return result;
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
    	int [] result = super.executeBatch();
    	batchCount++;
    	rowCount += result.length;
    	return super.executeBatch();
    }
    
    
}
