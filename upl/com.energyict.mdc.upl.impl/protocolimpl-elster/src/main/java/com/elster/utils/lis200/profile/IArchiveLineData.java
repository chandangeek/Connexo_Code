package com.elster.utils.lis200.profile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * This is an interface to get interpreted data of an archive line
 * <p/>
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:30:25
 */
public interface IArchiveLineData {

    /**
     * gets the time stamp from an archive line
     *
     * @return time stamp of line
     */
    public Date getTimeStamp();

    /**
     * get the tme stamp normalized to utc
     *
     * @param timeZone
     * @return normalized time stamp
     */
    public Date getTimeStampUtc(TimeZone timeZone);

    /**
     * gets the system state of an archive line
     *
     * @return ei server interval state of line
     */
    public int getLineState();

    /**
     * gets the value addressed by index
     *
     * @param index - address of value
     * @return the selected value
     */
    public BigDecimal getValue(int index);

    /**
     * gets the status of the value addressed by index
     *
     * @param index - address of value
     * @return the status of the selected value
     */
    public int getValueState(int index);


    /**
     * gets the lis200 event of an archive line
     *
     * @return the lis200 event code
     */
    public int getEvent();

    /**
     * get the lis200 instance state (cumulated)
     * @return
     */
    public int getInstanceState();

    /**
     * String representation of the data
     * 
     * @return
     */
    public String toString();
}


