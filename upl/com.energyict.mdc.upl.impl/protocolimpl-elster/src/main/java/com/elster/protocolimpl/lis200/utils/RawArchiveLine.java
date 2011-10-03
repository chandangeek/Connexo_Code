package com.elster.protocolimpl.lis200.utils;


import com.elster.utils.lis200.profile.IArchiveLineData;
import com.energyict.protocol.IntervalStateBits;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * This class holds common information of a line.
 * It can also be used to sort the interval data
 * <p/>
 * User: heuckeg
 * Date: 29.06.2010
 * Time: 09:20:08
 */
public class RawArchiveLine
        implements IArchiveLineData {

    RawArchiveLineInfo rali;

    /* time stamp of line */
    private Date timeStamp; /* in GMT0 */

    /* total data of a line */
    String[] data = null;

    /**
     * Constructor of RawArchiveLine interface
     *
     * @param rali           - information of raw archive line
     * @param rawArchiveLine - raw data (of an archive line)
     * @throws java.text.ParseException - in case of error in date
     */
    public RawArchiveLine(RawArchiveLineInfo rali, String rawArchiveLine) throws ParseException {

        this.rali = rali;

        data = utils.splitLine(rawArchiveLine);

        timeStamp = rali.getDateFormat().parse(data[rali.getTstCol()]);
    }

    private int getSysState() {
        int sysStateCol = rali.getSystemStateCol();
        if (sysStateCol < 0) {
            return 0;
        } else {
            return utils.StateToInt(data[sysStateCol]);
        }
    }

    /**
     * Getter for property isSummerTime
     *
     * @return true if date of line is in daylight saving time
     */
    public boolean isSummerTime() {
        return (getSysState() & 0x8000) != 0;

    }

    /*-----------------------------------------------------------------------------------
    *
    * Implementation of Interface IArchiveLineData
    *
    -----------------------------------------------------------------------------------*/

    /**
     * gets the time stamp from an archive line
     *
     * @return time stamp of line
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * get the tme stamp normalized to utc
     *
     * @param timeZone - time zone of "wanted" time
     * @return normalized time stamp
     */
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

        if ((result.get(Calendar.DST_OFFSET) == 0) && isSummerTime()) {
            result.add(Calendar.HOUR_OF_DAY, -1);
        }
        return result.getTime();
    }

    /**
     * gets the system state of an archive line
     *
     * @return ei server interval state of line
     */
    public int getLineState() {

        return utils.SysStateToEIState(getSysState());

    }

    /**
     * gets the value addressed by index
     *
     * @param index - address of value
     * @return the selected value
     */
    public BigDecimal getValue(int index) {
        int col = rali.getValueColumn(index);
        if (col >= 0) {
            return new BigDecimal(data[col]);
        } else {
            return null;
        }
    }

    /**
     * gets the status of the value addressed by index
     *
     * @param index - address of value
     * @return the status of the selected value
     */
    public int getValueState(int index) {
        return 0;
    }

    /**
     * gets the lis200 event of an archive line
     *
     * @return the lis200 event code
     */
    public int getEvent() {
        String e = data[rali.getEventCol()];
        if (e.startsWith("0x") || e.startsWith("0X")) {
            return Integer.parseInt(e.substring(2), 16);
        } else {
            return Integer.parseInt(e);
        }
    }

    /**
     * get the lis200 instance state (cumulated)
     *
     * @return cummulated instance state of archive line
     */
    public int getInstanceState() {
        if (rali.getNumberOfInstanceStateCols() == 0) {
            return IntervalStateBits.OK;
        }

        for (int col : rali.getInstanceStateCols()) {
            int state = utils.StateToInt(data[col]);
            if ((state & 0x3) > 0) {
                return IntervalStateBits.CORRUPTED;
            }
        }
        return IntervalStateBits.OK;
    }

    /**
     * String representation of the data
     *
     * @return string representation of archive line
     */
    public String toString() {

        StringBuilder line = new StringBuilder();

        for (String v : data) {
            if (line.length() > 0) {
                line.append(";");
            }
            line.append("(");
            line.append(v);
            line.append(")");
        }

        return line.toString();
    }

}
