package com.elster.utils.lis200.agrmodel;


import com.elster.agrimport.agrreader.IAgrValue;

import java.text.DateFormat;
import java.util.*;

/**
 * This class holds common information of a line.
 * It can also be used to sort the interval data
 * <p/>
 * User: heuckeg
 * Date: 29.06.2010
 * Time: 09:20:08
 */
@SuppressWarnings({"unused"})
public class ArchiveLine {

    private static DateFormat dfs = null;
    /**
     * line no in file
     */
    private int lineNo;

    /**
     * sequence number of line (if exists)
     */
    private Long sequenceNo;

    /**
     * global sequence number (if exists, only LIS200)
     */
    private Long globalSeqNo;

    /**
     * time stamp of line
     */
    private Date timeStamp; /* in GMT0 */
    private boolean isSummerTime;

    /* total data of a line */
    ArrayList<IAgrValue> data = null;

    /**
     * Simplest constructor needs at least a line number
     *
     * @param lineNo - number of line in file
     */
    public ArchiveLine(int lineNo) {
        this(lineNo, null, null, null, null);
    }

    /**
     * Constructor with line no, date and sequence no
     *
     * @param lineNo     - number of line in file
     * @param date       - time stamp of line
     * @param sequenceNo - sequence number of line
     */
    public ArchiveLine(int lineNo, Date date, long sequenceNo) {
        this(lineNo, date, sequenceNo, null, null);
    }

    /**
     * Complete Constructor with line no, date, sequence no and global sequence no
     *
     * @param lineNo       - number of line in file
     * @param date         - time stamp of line
     * @param sequenceNo   - sequence number of line
     * @param isSummerTime - true if date is in daylight saving time
     * @param globSeqNo    - global sequence no (only LIS200)
     */
    public ArchiveLine(int lineNo, Date date, Long sequenceNo, Long globSeqNo, Boolean isSummerTime) {
        this.lineNo = lineNo;
        this.sequenceNo = sequenceNo;
        this.globalSeqNo = globSeqNo;

        this.timeStamp = new Date((date.getTime() / 1000) * 1000);
        this.isSummerTime = isSummerTime != null && isSummerTime;
    }

    /**
     * Getter for line number
     *
     * @return line number
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * Getter for sequence number
     *
     * @return sequence number of line
     */
    public Long getSequenceNo() {
        return sequenceNo;
    }

    /**
     * Getter for global sequence number (only valid for LIS200 devices)
     *
     * @return global sequence number
     */
    public Long getGlobalSeqNo() {
        return globalSeqNo;
    }

    /**
     * Setter for property isSummerTime
     *
     * @param value - true is date is in daylight saving time
     */
    public void setIsSummerTime(boolean value) {
        this.isSummerTime = value;
    }

    /**
     * Getter for property isSummerTime
     *
     * @return true if date of line is in daylight saving time
     */
    public boolean isSummerTime() {
        return this.isSummerTime;
    }

    /**
     * Getter for time stamp
     *
     * @return time stamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }


    public Date getTimeStampUtc(TimeZone timeZone) {
        Calendar meterCal = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));

        meterCal.setTimeInMillis(timeStamp.getTime());

        Calendar result = Calendar.getInstance(timeZone);
        result.set(meterCal.get(Calendar.YEAR),
                meterCal.get(Calendar.MONTH),
                meterCal.get(Calendar.DAY_OF_MONTH),
                meterCal.get(Calendar.HOUR_OF_DAY),
                meterCal.get(Calendar.MINUTE),
                meterCal.get(Calendar.SECOND));
        result.set(Calendar.MILLISECOND, 0);

        if ((result.get(Calendar.DST_OFFSET) == 0) && isSummerTime) {
            result.add(Calendar.HOUR_OF_DAY, -1);
        }
        return result.getTime();
    }

    /**
     * Sets all data of a line
     *
     * @param data - array of IAgrValue
     */
    public void setData(ArrayList<IAgrValue> data) {
        this.data = data;
    }

    /**
     * Gets all data of a line
     *
     * @return data - array of IAgrValues
     */
    public ArrayList<IAgrValue> getData() {
        return data;
    }

    /**
     * Gets a single value of a line
     *
     * @param index - of value in line
     * @return IAgrValue - single value
     */
    public IAgrValue getValue(int index) {
        return data.get(index);
    }

    public String toString() {

        StringBuilder line = new StringBuilder(Integer.toString(lineNo));

        if (sequenceNo != null) {
            line.append(";");
            line.append(Long.toString(sequenceNo));
        }

        if (globalSeqNo != null) {
            line.append(";");
            line.append(Long.toString(globalSeqNo));
        }

        if (timeStamp != null) {
            line.append(";");
            line.append(getDateFormat().format(getTimeStamp()));
        }

        for (IAgrValue v : data) {
            line.append(";");
            line.append(v.toString());
        }

        line.append("\r\n");
        return line.toString();
    }


    private static DateFormat getDateFormat() {
        if (dfs == null) {
            dfs = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            dfs.setTimeZone(TimeZone.getTimeZone("GMT0"));
        }
        return dfs;
    }
}
