/*
 * VDEWRegisterDataParse.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
abstract public class VDEWRegisterDataParse {

    private static final int DEBUG = 1;

	static public final int MODE_WINTERTIME=0;
    static public final int MODE_SUMMERTIME=1;
    static public final int MODE_UTCTIME=2;

    public final static int VDEW_STRING=0;
    public final static int VDEW_DATESTRING=1;
    public final static int VDEW_TIMESTRING=2;
    public final static int VDEW_TIMEDATE=3; // (s)HHMMSS(s)YYMMDD
    public final static int VDEW_QUANTITY=4;
    public final static int VDEW_DATE_S_TIME=5; // YYMMDDsHHMMSS where s=0 for normal time, 1 for DST and 2 for UTC
    public final static int VDEW_S_TIME_S_DATE=6; // sHHMMSSsYYMMDD see VDEW_DATE_S_TIME for explanation of 's'
    public final static int VDEW_INTEGER=7;
    public final static int VDEW_TIMEDATE_FERRANTI=8; // YYYYMMDDHHMMSS
    public final static int VDEW_TIME_HHMMSS=9; // (s)hhmmss
    public final static int VDEW_DATE_YYMMDD=10; // (s)yymmdd
    public final static int VDEW_DATE_VALUE_PAIR=11; // DateValuePair (XXXXX...)(YYMMDDHHMM)
    public final static int VDEW_GMTDATESTRING=12; // write
    public final static int VDEW_GMTTIMESTRING=13; // write
    public final static int VDEW_DATETIME=14; // YYMMDDHHMMSS
    public final static int VDEW_DATETIME_NOSEC = 15; //YYMMDDHHMM
    public static final int VDEW_STRING_VALUE_PAIR = 16;


    abstract protected Unit getUnit();
    abstract protected int getType();
    abstract protected FlagIEC1107Connection getFlagIEC1107Connection();
    abstract protected ProtocolLink getProtocolLink();
    abstract protected int getOffset();
    abstract protected int getLength();

    /** Creates a new instance of VDEWRegisterDataParse */
    public VDEWRegisterDataParse() {
    }

    // ********************************* build data to set ***********************************
    protected String buildData(Object object) throws IOException {
        switch(getType()) {
            case VDEW_STRING:
                return (String)object;

            case VDEW_DATESTRING:
                return buildDate((Date)object,false);

            case VDEW_TIMESTRING:
                return buildTime((Date)object,false);

            case VDEW_GMTDATESTRING:
                return buildDate((Date)object,true);

            case VDEW_GMTTIMESTRING:
                return buildTime((Date)object,true);

            case VDEW_DATE_S_TIME:
                return buildDateSTime((Date)object);

            case VDEW_DATETIME:
                return buildDateTime((Date)object);

            case VDEW_TIMEDATE_FERRANTI:
                return buildTimeDateFerranti((Date)object);

            default:
                throw new IOException("VDEWRegisterDataParse, parse , unknown type "+getType());
        }
    }

    private String buildDate(Date date,boolean gmt) {
        Calendar calendar = null;
        byte[] data = new byte[7];
        if (gmt) {
           data[0] = (byte)(MODE_UTCTIME+0x30);
           calendar = ProtocolUtils.getCleanGMTCalendar();
        }
        else {
           if (getProtocolLink().getTimeZone().inDaylightTime(date)) data[0] = (byte)(MODE_SUMMERTIME+0x30);
           else data[0] = (byte)(MODE_WINTERTIME+0x30);
           calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        }
        calendar.setTime(date);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)-2000,data,1);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MONTH)+1,data,3);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.DAY_OF_MONTH),data,5);
        return new String(data);
    }


    private String buildTime(Date date, boolean gmt) {
        Calendar calendar = null;
        byte[] data = new byte[7];
        if (gmt) {
           data[0] = (byte)(MODE_UTCTIME+0x30);
           calendar = ProtocolUtils.getCleanGMTCalendar();
        }
        else {
           if (getProtocolLink().getTimeZone().inDaylightTime(date)) data[0] = (byte)(MODE_SUMMERTIME+0x30);
           else data[0] = (byte)(MODE_WINTERTIME+0x30);
           calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        }
        calendar.setTime(date);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY),data,1);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE),data,3);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND),data,5);
        return new String(data);
    }

    private String buildDateSTime(Date date) {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        calendar.setTime(date);
        byte[] data = new byte[13];
        if (getProtocolLink().getTimeZone().inDaylightTime(date)) data[6] = 0x31;
        else data[6] = 0x30;
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)-2000,data,0);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MONTH)+1,data,2);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.DATE),data,4);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY),data,7);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE),data,9);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND),data,11);
        return new String(data);
    }

    private String buildDateTime(Date date) {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        calendar.setTime(date);
        byte[] data = new byte[12];
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)-2000,data,0);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MONTH)+1,data,2);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.DATE),data,4);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY),data,6);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE),data,8);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND),data,10);
        return new String(data);
    }

    private String buildTimeDateFerranti(Date date) {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        calendar.setTime(date);
        byte[] data = new byte[14];
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)/100,data,0);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)-2000,data,2);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MONTH)+1,data,4);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.DATE),data,6);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY),data,8);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE),data,10);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND),data,12);
        return new String(data);
    }


    // ********************************* parse received data *********************************
    protected Object parse(byte[] data) throws ProtocolException {
        try {
            switch(getType()) {

                case VDEW_S_TIME_S_DATE:
                    return parseSTimeSDate(data);

                case VDEW_DATE_S_TIME:
                    return parseDateSTime(data);

                case VDEW_TIMEDATE_FERRANTI:
                    return parseTimeDateFerranti(data);

                case VDEW_TIMEDATE:
                    return parseDate(data);

                case VDEW_STRING:
                    return new String(data);

                case VDEW_DATESTRING:
                    return new String(data);

                case VDEW_TIMESTRING:
                    return new String(data);

                case VDEW_QUANTITY:
                    return parseQuantity(data);

                case VDEW_INTEGER:
                    return parseInteger(data);

                case VDEW_TIME_HHMMSS:
                    return parseTimeHHMMSS(data);

                case VDEW_DATE_YYMMDD:
                    return parseDateYYMMDD(data);

                case VDEW_DATE_VALUE_PAIR:
                    return parseDateValuePair(data);

                case VDEW_DATETIME:
                    return parseDateTimeYYMMDDHHMMSS(data);

                case VDEW_DATETIME_NOSEC:
                	return parseDateYYMMDDHHMM(data);

                case VDEW_STRING_VALUE_PAIR:
                	return parseStringValuePair(data);

                default:
                    throw new ProtocolException("VDEWRegisterDataParse, parse , unknown type "+getType());
            }
        }
        catch(NumberFormatException e) {
        	throw new ProtocolException("VDEWRegisterDataParse, parse error");
        } catch (IOException e) {
            throw new ProtocolException("VDEWRegisterDataParse, parse error" + e.getMessage());
        }

    } // protected Object parse(byte[] data)

    private DateValuePair parseDateValuePair(byte[] rawdata) throws IOException {
        StringBuffer strBuff=null;
        int count=0;
        BigDecimal value = null;
        Date date = null;
        Unit unit = Unit.getUndefined();
        String text = "";

        // format is (number)(YYMMDDHHMM)
        for(int i=0;i<rawdata.length;i++) {
            if (rawdata[i] == '(') {
               strBuff = new StringBuffer();
            }
            else if (rawdata[i] == ')') {
                if (count==0) {
                    try {
                       if (strBuff.toString().length() > 0){
                       	value = new BigDecimal(strBuff.toString());
                       } else {
                    	   value = null;
                       }
                    }
                    catch(NumberFormatException e) {

                    	// Iskra adds unit to the value: ex (0000.23 kVA)(081105163000)
                    	// so we need to check if there's a valid value followed by a space

                    	if (strBuff.toString().indexOf(" ") > 0){
                    		try {
                                value = new BigDecimal(strBuff.toString().substring(0, strBuff.toString().indexOf(" ")));
                                String unitacronym = strBuff.toString().substring(strBuff.toString().indexOf(" "));
                                if (unitacronym != null){
                                	unitacronym = unitacronym.trim();
                                	if (unitacronym.equalsIgnoreCase("deg")) unitacronym = "°";
                                	unit = Unit.get(unitacronym);
                                	MT83.sendDebug("VDEWRegisterDataParse(): unitacronym = " + unitacronym + " Unit.get() = " + unit, DEBUG);
                                	if (unit == null) unit = Unit.getUndefined();
                                }
                    		} catch (NumberFormatException ee) {
                    			value = null;
                    		}
                    	} else {
                    		value = null;
                    	}

                    }
                    strBuff=null;
                    count++;
                }
                else if (count==1) {
                    try {
						text = "";
                    	date = parseDateTimeYYMMDDHHMMSS(strBuff.toString().getBytes());
					} catch (Exception e) {
						date = null;
						text = strBuff.toString();
					}
                    break;
                }
            }
            else {
                if (strBuff != null)
                    strBuff.append((char)rawdata[i]);
            }
        }
        return new DateValuePair(date,value, unit, text);
    } // private DateValuePair parseDateValuePair(byte[] rawdata) throws IOException

    private DateValuePair parseStringValuePair(byte[] rawdata) throws IOException {
        StringBuffer strBuff=null;
        int count=0;
        BigDecimal value = null;
        String text = null;
        Unit unit = Unit.getUndefined();

        // format is (number)(YYMMDDHHMM)
        for(int i=0;i<rawdata.length;i++) {
            if (rawdata[i] == '(') {
               strBuff = new StringBuffer();
            }
            else if (rawdata[i] == ')') {
                if (count==0) {
                    try {
                       if (strBuff.toString().length() > 0){
                       	value = new BigDecimal(strBuff.toString());
                       } else {
                    	   value = null;
                       }
                    }
                    catch(NumberFormatException e) {

                    	// Iskra adds unit to the value: ex (0000.23 kVA)(081105163000)
                    	// so we need to check if there's a valid value followed by a space

                    	if (strBuff.toString().indexOf(" ") > 0){
                    		try {
                                value = new BigDecimal(strBuff.toString().substring(0, strBuff.toString().indexOf(" ")));
                                String unitacronym = strBuff.toString().substring(strBuff.toString().indexOf(" "));
                                if (unitacronym != null){
                                	unitacronym = unitacronym.trim();
                                	if (unitacronym.equalsIgnoreCase("deg")) unitacronym = "°";
                                	unit = Unit.get(unitacronym);
                                	MT83.sendDebug("VDEWRegisterDataParse(): unitacronym = " + unitacronym + " Unit.get() = " + unit, DEBUG);
                                	if (unit == null) unit = Unit.getUndefined();
                                }
                    		} catch (NumberFormatException ee) {
                    			value = null;
                    		}
                    	} else {
                    		value = null;
                    	}

                    }
                    strBuff=null;
                    count++;
                }
                else if (count==1) {
                    text = strBuff.toString();
//                    date = parseDateTimeYYMMDDHHMMSS(strBuff.toString().getBytes());
                    break;
                }
            }
            else {
                if (strBuff != null)
                    strBuff.append((char)rawdata[i]);
            }
        }
        return new DateValuePair(null,value, unit, text);
    } // private DateValuePair parseDateValuePair(byte[] rawdata) throws IOException

    private Integer parseInteger(byte[] rawdata) throws IOException {
        return new Integer(Integer.parseInt(findValue(rawdata)));
    }

    private Quantity parseQuantity(byte[] rawdata) throws IOException {
        Unit unit = buildUnit(rawdata);
        BigDecimal bd = new BigDecimal(findValue(rawdata));
        return new Quantity(bd,unit);
    }

    private Unit buildUnit(byte[] rawdata) throws IOException {
        if (hasAcronym(rawdata)) {
            Unit unit = Unit.get(getUnitAcronym(rawdata));
            if (unit == null)
                throw new IOException("VDEWRegisterDataPArse, buildUnit, acronym "+getUnitAcronym(rawdata)+" not found in baseunit list!");
            else
                return unit;
        }
        else if (hasUnitDefined()) {
            return getUnit();
        }
        else return Unit.get("");
    } // private Unit buildUnit(byte[] rawdata)

    private String getUnitAcronym(byte[] rawdata) {
        StringBuffer buff = new StringBuffer();
        int state=0;
        for (int i=0;i<rawdata.length;i++) {
            if ((state==0) && (rawdata[i] == '*')) state = 1;
            else if (state==1) buff.append((char)rawdata[i]);
        }
        return buff.toString();
    } // private String findUnitAcronym(byte[] rawdata)

    private boolean hasUnitDefined() {
        return getUnit() != null;
    }

    private boolean hasAcronym(byte[] rawdata) {
        String str = new String(rawdata);
        return str.indexOf("*") != -1;
    }

    private String findValue(byte[] rawdata) {
        StringBuffer buff = new StringBuffer();
        for (int i=0;i<rawdata.length;i++) {
            if (rawdata[i] == '*') break;
            else buff.append((char)rawdata[i]);
        }
        return buff.toString();
    } // private String findValue(byte[] rawdata)

    private Date parseDateYYMMDDHHMM(byte[] rawdata) throws IOException {
        byte[] data = ProtocolUtils.convert2ascii(rawdata);
        Calendar calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        calendar.clear();
        calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(data[0])+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[2]));
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[3]));
        calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[4]));
        return calendar.getTime();
    }


    private Date parseDateTimeYYMMDDHHMMSS(byte[] rawdata) throws IOException {
        byte[] data = ProtocolUtils.convert2ascii(rawdata);
        Calendar calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        calendar.clear();
        calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(data[0])+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[2]));
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[3]));
        calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[4]));
        calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[5]));
        return calendar.getTime();
    }

    private Date parseTimeHHMMSS(byte[] rawdata) throws IOException {
        int mode=-1;
        Calendar calendar=null;
        if (rawdata.length==13) {
            mode = (int)ProtocolUtils.bcd2nibble(rawdata,0);
            rawdata = ProtocolUtils.getSubArray(rawdata,1);
        }

        if (mode == MODE_UTCTIME)
            calendar = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("GMT"));
        else
            calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());

        byte[] data = ProtocolUtils.convert2ascii(rawdata);
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[0]));
        calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[1]));
        calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[2]));
        return calendar.getTime();
    }

    private Date parseDateYYMMDD(byte[] rawdata) throws IOException {
        int mode=-1;
        Calendar calendar=null;
        if (rawdata.length==13) {
            mode = (int)ProtocolUtils.bcd2nibble(rawdata,0);
            rawdata = ProtocolUtils.getSubArray(rawdata,1);
        }
        if (mode == MODE_UTCTIME)
            calendar = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("GMT"));
        else
            calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());

        byte[] data = ProtocolUtils.convert2ascii(rawdata);
        calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(data[0])+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[2]));
        return calendar.getTime();
    }

    private Date parseDate(byte[] rawdata) throws IOException {
        Calendar calendar = null;
        int mode = -1;
        byte[] timedate = new byte[12];

        if ((rawdata.length%2) != 0)
            throw new IOException("parseDate, rawdata wrong length ("+rawdata.length+")!");

        if (rawdata.length == 14) {
           timedate = new byte[12];
           mode = (int)ProtocolUtils.bcd2nibble(rawdata,0);
           ProtocolUtils.arrayCopy(ProtocolUtils.getSubArray2(rawdata,1,6),timedate,0);
           ProtocolUtils.arrayCopy(ProtocolUtils.getSubArray2(rawdata,8,6),timedate,6);
           if (mode == MODE_UTCTIME)
              calendar = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("GMT"));
           else
              calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        }
        else {
           ProtocolUtils.arrayCopy(rawdata,timedate,0);
           calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        }

        byte[] data = ProtocolUtils.convert2ascii(timedate);
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[0]));
        calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[1]));
        calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[2]));
        calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(data[3])+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[4])-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[5]));
        return calendar.getTime();
    }

    private Date parseSTimeSDate(byte[] rawdata) throws IOException {
        if (rawdata.length != 14) throw new IOException("VDEW_S_TIME_S_DATE wrong length!");
        byte[] time = ProtocolUtils.convert2ascii(ProtocolUtils.getSubArray(rawdata,1,6));
        byte[] date = ProtocolUtils.convert2ascii(ProtocolUtils.getSubArray(rawdata,8,13));
        // one of the S flags is OK
        int seasonalInfo = rawdata[0] - 0x30;
        Calendar calendar=null;
        if (seasonalInfo == 0) // normal time
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        else if (seasonalInfo == 1) // DST
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        else if (seasonalInfo == 2) // UTC
            calendar = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(date[0])+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(date[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(date[2]));
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(time[0]));
        calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(time[1]));
        calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(time[2]));
        return calendar.getTime();
    }

    private Date parseDateSTime(byte[] rawdata) throws IOException {
        if (rawdata.length != 13) throw new IOException("VDEW_DATE_S_TIME wrong length!");
        byte[] date = ProtocolUtils.convert2ascii(ProtocolUtils.getSubArray(rawdata,0,5));
        byte[] time = ProtocolUtils.convert2ascii(ProtocolUtils.getSubArray(rawdata,7,12));
        int seasonalInfo = rawdata[6] - 0x30;
        Calendar calendar=null;
        if (seasonalInfo == 0) // normal time
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        else if (seasonalInfo == 1) // DST
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        else if (seasonalInfo == 2) // UTC
            calendar = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.YEAR,ProtocolUtils.BCD2hex(date[0])+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex(date[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(date[2]));
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(time[0]));
        calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(time[1]));
        calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(time[2]));
        return calendar.getTime();
    }

    private Date parseTimeDateFerranti(byte[] rawdata) throws IOException {
        if (rawdata.length != 14) throw new IOException("VDEW_TIMEDATE_FERRANTI wrong length!");
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        int year = ProtocolUtils.bcd2int(rawdata,0)*100;
        calendar.set(Calendar.YEAR,ProtocolUtils.bcd2int(rawdata,2)+year);
        calendar.set(Calendar.MONTH,ProtocolUtils.bcd2int(rawdata,4)-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.bcd2int(rawdata,6));
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.bcd2int(rawdata,8));
        calendar.set(Calendar.MINUTE,ProtocolUtils.bcd2int(rawdata,10));
        calendar.set(Calendar.SECOND,ProtocolUtils.bcd2int(rawdata,12));
        return calendar.getTime();
    }

} // abstract public class VDEWRegisterDataParse
