package com.elster.protocolimpl.lis100.profile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Class storing on interval value
 *
 * User: heuckeg
 * Date: 25.01.11
 * Time: 14:28
 */
@SuppressWarnings({"unused"})
public class DataElement {

    private long date;
    private Object value;
    private Integer state;

    public DataElement(long date, Object data, Integer state) {
        this.date = date;
        this.value = data;
        this.state = state;
    }

    public DataElement(long date, Object data) {
        this(date, data, null);
    }

    public long getDateLong() {
        return date;
    }

    public Object getValue() {
        return value;
    }

    public Integer getState() {
        return state;
    }

    public String toString(String delimiter) {
        String s = new Date(date) + delimiter;

        Long l = 0L;
        if (value instanceof Integer) {
            l = ((Integer)value).longValue() * 10000L;
        } else if (value instanceof Long) {
            l = (Long) value * 10000L;
        } else if (value instanceof Double) {
            double d = (Double)value;
            l = Math.round(d * 10000);
        }
        BigDecimal b = BigDecimal.valueOf(l, 4);
        s += b + delimiter;

        if (state == null) {
            s += "-";
        } else {
            s += state;
        }
        return s;
    }
}
