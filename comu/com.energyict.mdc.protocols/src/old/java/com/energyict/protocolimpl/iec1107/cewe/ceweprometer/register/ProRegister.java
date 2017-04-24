/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.NestedIOException;

import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <blockquote><pre>
 * <p/>
 * Responsibilities:
 * - can read it's register counterpart in the meter ( getRawData() )
 * - if a ProRegister is cacheable, getRawData will only fetch it once
 * - is able to parse all fields in a register ( asDate(), asInt(), as...() )
 * <p/>
 * If the register is cacheable, it can be parsed using all the "as...()"
 * conversion methods.  However, if it is not cacheable, only the method
 * getRawData() is supported.  Every time the getRawData() is called, it
 * will refetch the data from the meter.
 * <p/>
 * To avoid this refetching it is possible to freeze a register.
 * <p/>
 * </pre></blockquote>
 *
 * @author fbo
 */

public class ProRegister {

    private static final String NOT_SUPPORTED_EXCEPTION = "Register is not cacheable, so method not supported";

    /* reference to protocol object */
    private CewePrometer meter;
    /* protocol id of register */
    private String id;
    /* flag indicating of the register can be cached or not */
    private boolean cacheable;
    /* nr of records to be returned by command (default=1) */
    private int fetchSize;

    private String rawData;
    private List fields;

    /**
     * @param meter
     * @param id
     */
    public ProRegister(CewePrometer meter, String id) {
        this(meter, id, true);
    }

    /**
     * @param meter
     * @param id
     * @param cacheable
     */
    public ProRegister(CewePrometer meter, String id, boolean cacheable) {
        this(meter, id, cacheable, 1);
    }

    /**
     * @param meter
     * @param id
     * @param cacheable
     * @param fetchSize
     */
    public ProRegister(CewePrometer meter, String id, boolean cacheable, int fetchSize) {
        this.meter = meter;
        this.id = id;
        this.cacheable = cacheable;
        this.fetchSize = fetchSize;
    }

    /**
     * @param rawData
     */
    public ProRegister(String rawData) {
        this.rawData = rawData;
        this.cacheable = true;
        String tmp = rawData.substring(1, rawData.length() - 1); // remove braces ()
        fields = Arrays.asList(tmp.split(","));
    }

    /**
     * id as byte[]
     */
    public byte[] getId() {
        return id.getBytes();
    }

    /**
     * nr of fields
     */
    public int size() {
        return fields.size();
    }

    /**
     * iterator over all the fields
     */
    public Iterator iterator() {
        return fields.iterator();
    }

    public String getRawData() throws IOException {
        return getRawData(true);
    }

    public String getRawData(boolean retry) throws IOException {
        if (rawData == null) {
            String tmp = meter.read(id + "(" + fetchSize + ")", retry);
            if (!cacheable) {
                return tmp;
            }
            rawData = tmp;
        }
        return rawData;
    }

    public ProRegister readAndFreeze() throws IOException {
        ProRegister register = new ProRegister(getRawData());
        register.meter = meter;
        register.cacheable = true;
        return register;
    }

    public void setCeweProMeter(CewePrometer meter) {
        this.meter = meter;
    }

    /**
     * parse field 0 as String
     */
    public String asString() throws IOException {
        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }
        return asString(0);
    }

    /**
     * parse field 0 as String
     */
    public String asCompleteString() throws IOException {
        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        if (fields == null) {
            String tmp = getRawData();
            tmp = tmp.substring(1, tmp.length() - 1); // remove braces ()
            List f = Arrays.asList(tmp.split(","));

            if (cacheable) {
                fields = f;
            } else {
                return appendItemsAsString(f);
            }

        }

        return appendItemsAsString(fields);

    }

    private String appendItemsAsString(List f) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < f.size(); i++) {
            Object part = f.get(i);
            sb.append(part).append(" ");
        }
        return sb.toString();
    }


    /**
     * parse field: fieldIdx as String
     */
    public String asString(int fieldIdx) throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        if (fields == null) {
            String tmp = getRawData();
            tmp = tmp.substring(1, tmp.length() - 1); // remove braces ()
            List f = Arrays.asList(tmp.split(","));

            if (cacheable) {
                fields = f;
            } else {
                return (f.size() > fieldIdx) ? (String) f.get(fieldIdx) : "";
            }

        }

        return (fields.size() > fieldIdx) ? (String) fields.get(fieldIdx) : "";

    }

    /**
     * parse field: fieldIdx as Double
     */
    public Double asDouble(int fieldIdx) throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        return new Double(asString(fieldIdx));

    }

    /**
     * parse field 0 as Double
     */
    public Double asDouble() throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        return asDouble(0);

    }

    /**
     * parse field: fieldIdx int field
     */
    public Integer asInteger(int fieldIdx) throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        return new Integer(asString(fieldIdx));

    }

    /**
     * parse field 0 as Integer
     */
    public Integer asInteger() throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        return asInteger(0);

    }

    /**
     * parse field: fieldIdx as int
     */
    public int asInt(int fieldIdx) throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        return Integer.parseInt(asString(fieldIdx));

    }

    /**
     * parse field 0 as int
     */
    public int asInt() throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        return asInt(0);

    }

    /**
     * parse field 0 as Date with LongDateFormat
     */
    public Date asDate() throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        try {
            return meter.getDateFormats().getLongDateFormat().parse(asString(0) + asString(1));
        } catch (ParseException e) {
            throw new NestedIOException(e);
        }

    }

    /**
     * parse field 0 as Date with sdf as DateFormat
     */
    public Date asDate(SimpleDateFormat sdf) throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        try {
            return sdf.parse(asString(0) + asString(1));
        } catch (ParseException e) {
            throw new NestedIOException(e);
        }

    }


    /**
     * parse fieldas Date with short dateFormat
     */
    public Date asShortDate(int fieldIdx) throws IOException {

        if (!cacheable) {
            throw new ApplicationException(NOT_SUPPORTED_EXCEPTION);
        }

        try {
            return meter.getDateFormats().getShortDateFormat().parse(asString(fieldIdx));
        } catch (ParseException e) {
            throw new NestedIOException(e);
        }

    }

    /**
     * 19700101,000000 semantically means no date or NULL
     */
    public boolean isNullDate() throws IOException {
        String tmp = getRawData();
        tmp = tmp.substring(1, tmp.length() - 1); // remove braces ()
        return "19700101,000000".equals(tmp);
    }

}
