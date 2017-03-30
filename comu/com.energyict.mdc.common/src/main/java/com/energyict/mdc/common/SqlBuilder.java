/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import oracle.jdbc.OraclePreparedStatement;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SqlBuilder {

    private static final int OBJECT = Types.JAVA_OBJECT;
    private static final int STRING = Types.VARCHAR;
    private static final int INTEGER = Types.INTEGER;
    private static final int LONG = Types.BIGINT;
    private static final int DOUBLE = Types.DOUBLE;
    private static final int TIMESTAMP = Types.TIMESTAMP;
    private static final int BIGDECIMAL = Types.DECIMAL;
    private static final int DATE = Types.DATE;
    private static final int UTCTIMESTAMP = -Types.TIMESTAMP;
    private static final int STRUCT = Types.STRUCT;
    private static final int CLOB = Types.CLOB;

    private static final int USE_DEFAULT_FETCH_SIZE = -1;

    private StringBuffer buffer;
    private List bindObjects;
    private List<Integer> bindTypes;
    private List<String> bindTypeNames;
    private boolean hasWhere = false;
    private Calendar calendar;

    public SqlBuilder() {
        this(new StringBuffer());
    }

    public SqlBuilder(String text) {
        this(new StringBuffer(text));
    }

    public SqlBuilder(StringBuffer buffer) {
        this.buffer = buffer;
        this.bindObjects = new ArrayList();
        this.bindTypes = new ArrayList<>();
        this.bindTypeNames = new ArrayList<>();
    }

    private Calendar getUtcCalendar() {
        if (calendar == null) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        }
        return calendar;
    }

    public void append(String text) {
        buffer.append(text);
    }

    public void append(int i) {
        buffer.append(i);
    }

    public void append(long i) {
        buffer.append(i);
    }

    public void append(StringBuffer text) {
        buffer.append(text);
    }

    public void bindObject(Object bindVar) {
        bindObjects.add(bindVar);
        bindTypes.add(OBJECT);
        bindTypeNames.add("");
    }

    public void bindString(String bindVar) {
        bindObjects.add(bindVar);
        bindTypes.add(STRING);
        bindTypeNames.add("");
    }

    public void bindInt(int bindVar) {
        bindObjects.add(new Integer(bindVar));
        bindTypes.add(INTEGER);
        bindTypeNames.add("");
    }

    public void bindLong(long bindVar) {
        bindObjects.add(new Long(bindVar));
        bindTypes.add(LONG);
        bindTypeNames.add("");
    }

    public void bindLongToNull() {
        bindObjects.add(null);
        bindTypes.add(LONG);
        bindTypeNames.add("");
    }

    public void bindTimestamp(Instant bindVar) {
        Timestamp ts = null;
        if (bindVar != null) {
            ts = new Timestamp(bindVar.toEpochMilli());
        }
        bindObjects.add(ts);
        bindTypes.add(TIMESTAMP);
        bindTypeNames.add("");
    }

    public void bindDate(Instant bindVar) {
        java.sql.Date date = new java.sql.Date(bindVar.toEpochMilli());
        bindObjects.add(date);
        bindTypes.add(DATE);
        bindTypeNames.add("");
    }

    public PreparedStatement getStatement(Connection conn) throws SQLException {
        PreparedStatement result = conn.prepareStatement(buffer.toString());
        boolean failed = true;
        try {
            bind(result);
            failed = false;
        } finally {
            if (failed) {
                result.close();
            }
        }
        return result;
    }

    public void bind(PreparedStatement stmnt) throws SQLException {
        for (int i = 0; i < bindObjects.size(); i++) {
            Object bindVar = bindObjects.get(i);
            int bindType = bindTypes.get(i);
            String typeName = bindTypeNames.get(i);
            switch (bindType) {
                case OBJECT:
                    stmnt.setObject(i + 1, bindVar);
                    break;
                case STRING:
                    stmnt.setString(i + 1, (String) bindVar);
                    break;
                case CLOB: {
                    String value = (String) bindVar;
                    if (stmnt instanceof OraclePreparedStatement) {
                        OraclePreparedStatement oraclePreparedStatement = (OraclePreparedStatement) stmnt;
                        oraclePreparedStatement.setStringForClob(i + 1, value);
                    } else {
                        stmnt.setCharacterStream(i + 1, new StringReader(value), value.length());
                    }
                }
                case INTEGER:
                    if (bindVar == null) {
                        if (typeName.isEmpty()) {
                            stmnt.setNull(i + 1, Types.NUMERIC);
                        }
                        else {
                            stmnt.setNull(i + 1, Types.NUMERIC, typeName);
                        }
                    } else {
                        stmnt.setInt(i + 1, (Integer) bindVar);
                    }
                    break;
                case LONG:
                    if (bindVar == null) {
                        if (typeName.isEmpty()) {
                            stmnt.setNull(i + 1, Types.NUMERIC);
                        }
                        else {
                            stmnt.setNull(i + 1, Types.NUMERIC, typeName);
                        }
                    } else {
                        stmnt.setLong(i + 1, (Long) bindVar);
                    }
                    break;
                case BIGDECIMAL:
                    stmnt.setBigDecimal(i + 1, (BigDecimal) bindVar);
                    break;
                case Types.NUMERIC:
                    // Should only get here if value is null
                    stmnt.setNull(i + 1, Types.NUMERIC);
                    break;
                case DOUBLE:
                    stmnt.setDouble(i + 1, (Double) bindVar);
                    break;
                case TIMESTAMP:
                    stmnt.setTimestamp(i + 1, (Timestamp) bindVar);
                    break;
                case DATE:
                    stmnt.setDate(i + 1, (java.sql.Date) bindVar);
                    break;
                case UTCTIMESTAMP:
                    stmnt.setTimestamp(i + 1, (Timestamp) bindVar, getUtcCalendar());
                    break;
                case STRUCT:
                    if (bindVar == null) {
                        if (typeName.isEmpty()) {
                            stmnt.setNull(i + 1, Types.STRUCT);
                        }
                        else {
                            stmnt.setNull(i + 1, Types.STRUCT, typeName);
                        }
                    }
                    else {
                        stmnt.setObject(i + 1, bindVar, Types.STRUCT);
                    }
                    break;
                default:
                    throw new ApplicationException("Invalid type: " + bindType);
            }
        }
    }

    public String expandedText() {
        StringBuffer result = new StringBuffer(buffer.length());
        int bindIndex = 0;
        for (int i = 0; i < buffer.length(); i++) {
            char sqlChar = buffer.charAt(i);
            if (sqlChar == '?') {
                int bindType = bindTypes.get(bindIndex);
                Object bindObject = bindObjects.get(bindIndex++);
                if (bindObject == null) {
                    result.append("NULL");
                } else {
                    switch (bindType) {
                        case OBJECT:
                        case STRUCT:
                        case CLOB:
                        case STRING:
                            this.appendQuoted(result, bindObject.toString());
                            break;
                        case INTEGER:
                        case LONG:
                        case BIGDECIMAL:
                        case DOUBLE:
                            result.append(bindObject.toString());
                            break;
                        case TIMESTAMP:
                            DateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");
                            result.append("to_date('");
                            result.append(format.format((Date) bindObject));
                            result.append("','YYYYMMDD HH24MISS')");
                            break;
                        case DATE:
                            format = new SimpleDateFormat("yyyyMMdd");
                            result.append("to_date('");
                            result.append(format.format((Date) bindObject));
                            result.append("','YYYYMMDD')");
                            break;
                        case UTCTIMESTAMP:
                            format = new SimpleDateFormat("yyyyMMdd HHmmss");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            result.append("to_date('");
                            result.append(format.format((Date) bindObject));
                            result.append("','YYYYMMDD HH24MISS')");
                            break;
                        default:
                            throw new ApplicationException("Invalid type: " + bindType);
                    }
                }
            } else {
                result.append(sqlChar);
            }
        }
        return result.toString();
    }

    public void appendWhereOrAnd() {
        if (hasWhere) {
            buffer.append(" and ");
        } else {
            buffer.append(" where ");
            hasWhere = true;
        }
    }

    public void append(SqlBuilder other) {
        buffer.append(other.buffer);
        bindObjects.addAll(other.bindObjects);
        bindTypes.addAll(other.bindTypes);
        bindTypeNames.addAll(other.bindTypeNames);
    }

    public void replace(SqlBuilder other){
        if (other != this) {
            buffer.setLength(0);
            append(other);
        }
    }

    /**
     * Returns a page builder for the SQL defined in the receiver
     * When fromRow is zero all data will be returned, independent of the value in toRow
     *
     * @param fromRow start row to be returned
     * @param toRow   last row to be returned
     * @return the SqlBuilder object
     */
    public SqlBuilder asPageSqlBuilder(int fromRow, int toRow) {
        if (fromRow == 0) {
            return this;
        }
        SqlBuilder builder = new SqlBuilder("select * from (select ");
        builder.append("x.*, ROWNUM rnum from (");
        builder.append(this);
        builder.append(") x where ROWNUM <= ?) ");
        builder.bindInt(toRow);
        builder.append("where rnum >= ?");
        builder.bindInt(fromRow);
        return builder;
    }

    public String toString() {
        return buffer.toString();
    }

    private void appendQuoted(StringBuffer buffer, String in) {
        buffer.append('\'');
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'') {
                buffer.append("''");
            } else {
                buffer.append(c);
            }
        }
        buffer.append('\'');
    }

}
