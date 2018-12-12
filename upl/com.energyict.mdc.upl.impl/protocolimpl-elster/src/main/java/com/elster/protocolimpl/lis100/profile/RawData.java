package com.elster.protocolimpl.lis100.profile;

import java.util.ArrayList;
import java.util.Calendar;

import static com.elster.protocolimpl.lis100.objects.CalcFactorObject.RawDataToCalcFactor;
import static com.elster.protocolimpl.lis100.objects.CpValueObject.RawDataToCpValue;
import static com.elster.protocolimpl.lis100.objects.IntervalObject.StringToIntervalSec;

/**
 * class for raw data with additional helper functions
 * <p/>
 * User: heuckeg
 * Date: 07.02.11
 * Time: 11:44
 */
@SuppressWarnings({"unused"})
public class RawData extends ArrayList<Integer> {

    public static int READYEAR = 0x01;
    public static int READMONTH = 0x02;
    public static int READDAY = 0x04;
    public static int READHOUR = 0x08;
    public static int READMINUTE = 0x10;
    public static int READSECOND = 0x20;

    public static int READ_YMDHMS = READYEAR | READMONTH | READDAY | READHOUR | READMINUTE | READSECOND;
    public static int READ_YMDHM = READYEAR | READMONTH | READDAY | READHOUR | READMINUTE;
    public static int READ_DHM = READDAY | READHOUR | READMINUTE;

    private int pos;
    private int readBytes;

    private boolean foundCC;

    private int dateDirection = 1;

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public boolean isFoundCC() {
        return foundCC;
    }

    public int getDateDirection() {
        return dateDirection;
    }

    public void setDateDirection(int dateDirection) {
        this.dateDirection = dateDirection;
    }

    public ControlCodeData readCCData(Calendar oldDate, int controlCode) {

        foundCC = false;
        Calendar date = (Calendar)oldDate.clone();
        Integer i;
        String s;
        ControlCodeData result = new ControlCodeData(controlCode);
        readBytes = 0;

        switch (controlCode) {
            case 0xFFE: /* change of interval */
                i = StringToIntervalSec(Integer.toHexString(getInternal()));
                result.setParams(new Object[]{i});
                break;

            case 0xFFD: /* end of readout */
                result.setDate(readDate(date, RawData.READ_YMDHMS).getTime());
                break;

            case 0xFFC: /* Start of readout */
                result.setDate(readDate(date, RawData.READ_YMDHMS).getTime());
                i = getInternal();
                result.setParams(new Object[]{i});
                break;

            case 0xFFB: /* Begin of day */
                if (date != null) {
                    int day = getAsDecimalValue();
                    int hour = getAsDecimalValue();
                    int minute = getAsDecimalValue();
                    getInternal(); /* checksum */

                    if (!foundCC) {
                        if (day != date.get(Calendar.DAY_OF_MONTH)) {
                            date.add(Calendar.DAY_OF_MONTH, dateDirection);
                        }
                        if (day == date.get(Calendar.DAY_OF_MONTH)) {
                            date.set(Calendar.HOUR_OF_DAY, hour);
                            date.set(Calendar.MINUTE, minute);
                            date.set(Calendar.SECOND, 0);
                            date.set(Calendar.MILLISECOND, 0);
                            result.setDate(date.getTime());
                        }
                    }
                }
                break;

            case 0xFFA: /* Begin of month */
                result.setDate(readDate(date, RawData.READ_YMDHM).getTime());
                Double d = getAsCounter();
                i = StringToIntervalSec(Integer.toHexString(getInternal()));
                getInternal(); /* checksum */
                result.setParams(new Object[]{d, i});
                break;

            case 0xFF9: /* io sign */
                i = getInternal();
                result.setParams(new Object[]{i});
                break;

            case 0xFF8: /* large encoded control code */
                int size = getInternal();
                result.setSubCode(getInternal());
                switch (result.getSubCode()) {
                    case 0x001: /* new begin of day */
                        Integer db1 = getAsDecimalValue();
                        Integer db2 = getAsDecimalValue();
                        result.setParams(new Object[]{db1, db2});
                        break;
                    case 0x002: /* date correction forwards */
                        result.setDate(readDate(date, RawData.READ_YMDHMS).getTime());
                        break;
                    case 0x003: /* new cp value */
                        Double cp1 = RawDataToCpValue("" + getInternal());
                        Double cp2 = RawDataToCpValue("" + getInternal());
                        result.setParams(new Object[]{cp1, cp2});
                        break;
                    case 0x004: /* new factor */
                        s = makeStringOf(6);
                        Double cf1 = RawDataToCalcFactor(s);
                        s = makeStringOf(6);
                        Double cf2 = RawDataToCalcFactor(s);
                        result.setParams(new Object[]{cf1, cf2});
                        break;
                    case 0x00A: /* date correction backwards */
                        //System.out.println("-- FF8 00A");
                }

                while (readBytes < size) {
                    getInternal();
                }
                break;

            case 0xFF2: /* power fail restart */
                result.setDate(readDate(date, RawData.READ_YMDHMS).getTime());
                break;
        }

        result.addLength(readBytes);
        return result;
    }

    public int getValue() {
        if (pos > 0) {
            return super.get(--pos);
        }
        else {
            return 0;
        }
    }

    private int getInternal() {

        if (pos == 0) {
            return 0;
        }
        int result = super.get(pos - 1);
        readBytes++;
        foundCC = result >= 0xF00;
        if (foundCC) {
            result = 0;
        } else {
            pos -= 1;
        }
        return result;
    }

    private String makeStringOf(int length) {
        String result = "";
        while (length-- > 0) {
            result += String.format("%02x", getInternal());
        }
        return result;
    }

    public Calendar readDate(Calendar date, int parts) {

        int val;

        if ((parts & READYEAR) > 0) {
            val = getAsDecimalValue() + 1900;
            if (val < 1980) {
                val += 100;
            }
            date.set(Calendar.YEAR, val);
        }
        if ((parts & READMONTH) > 0) {
            val = getAsDecimalValue();
            date.set(Calendar.MONTH, val - 1);
        }
        if ((parts & READDAY) > 0) {
            val = getAsDecimalValue();
            date.set(Calendar.DAY_OF_MONTH, val);
        }
        if ((parts & READHOUR) > 0) {
            val = getAsDecimalValue();
            date.set(Calendar.HOUR_OF_DAY, val);
        }
        if ((parts & READMINUTE) > 0) {
            val = getAsDecimalValue();
            date.set(Calendar.MINUTE, val);
        }
        if ((parts & READSECOND) > 0) {
            val = getAsDecimalValue();
            date.set(Calendar.SECOND, val);
        } else {
            date.set(Calendar.SECOND, 0);
        }
        return date;
    }

    /**
     * method to return converted values of the data stream in their needed data form
     * 0xab -> ab (NOT a * 16 + b !!!, it has to be a * 10 + b)
     *
     * @return decimal interpretation
     */
    private int getAsDecimalValue() {
        int h = getInternal();
        return ((h >> 4) * 10) + (h & 0xF);
    }

    private double getAsCounter() {
        double result = 0.0;
        for (int i = 0; i < 4; i++) {
            result *= 100;
            result += getAsDecimalValue();
        }
        return result;
    }
}
