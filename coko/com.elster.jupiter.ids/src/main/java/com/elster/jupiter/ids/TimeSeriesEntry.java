package com.elster.jupiter.ids;

import java.math.BigDecimal;
import java.util.Date;

public interface TimeSeriesEntry {

    TimeSeries getTimeSeries();

    Date getTimeStamp();

    Date getRecordDateTime();

    Date getDate(int offset);

    BigDecimal getBigDecimal(int offset);

    int size();

    long getLong(int offset);

    long getVersion();
    
    String getString(int offset);
    
    Object[] getValues();
}
