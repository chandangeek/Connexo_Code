package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by iulian on 10/7/2016.
 */
public class MBusDateTypeI {


    private boolean valid;
    private int dstDev;
    private int sec;
    private int min;
    private int hour;
    private int day;
    private int mon;
    private int year;
    private int dayOfWeek;
    private int dst;
    private int leap;
    private int dstSign;


    public MBusDateTypeI(byte[] raw) {
        decode(raw);
    }

    public MBusDateTypeI(Date activationDate) {
        update(activationDate);
    }


    public void update(Date activationDate) {
        Calendar c = getCalendarInstance();
        c.setTime(activationDate);
        c.setFirstDayOfWeek(Calendar.MONDAY);

        this.sec = c.get(Calendar.SECOND);
        this.min = c.get(Calendar.MINUTE);
        this.hour = c.get(Calendar.HOUR_OF_DAY);
        this.day = c.get(Calendar.DAY_OF_MONTH);
        this.mon = c.get(Calendar.MONTH);
        this.year = c.get(Calendar.YEAR);

        this.dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        // translate from java format (1=Sunday, 2=Monday, etc to expected format 1=Monday, 7=Sunday)
        this.dayOfWeek = this.dayOfWeek - 1;
        if (this.dayOfWeek == 0){
            this.dayOfWeek = 7;
        }

        this.leap = (this.year % 4) == 0? 1 : 0;
        this.valid = true;

        //defaults
        this.dstSign = getTimeZone().inDaylightTime(c.getTime())?1:0;
        this.dst = 0;
    }

    private byte[] encode(boolean immediateActivation) {

        byte[] encoded = new byte[6];

        if (immediateActivation){
            for (int i=0; i<6; i++){
                encoded[i] = (byte) 0xff;
            }
            return encoded;
        }

        encoded[0] = (byte) ((((sec & 0x3f) | ((leap & 0x01) << 7))) & 0xff);
        encoded[1] = (byte) (min & 0x3f);  // min + valid
        encoded[2] = (byte) (hour & 0x1f);
        encoded[3] = (byte) (day & 0x1f);
        encoded[4] = (byte) ((mon+1) & 0x0f);

        int y = (byte) (year-2000);

        encoded[3] |= (y & 0x07) << 5;
        encoded[4] |= (y & 0x78) << 1;

        encoded[2] |= (byte) ((dayOfWeek & 0x07) << 5);
     //   encoded[1] |= ((dstSign & 0x01) << 6);

        return encoded;
    }


    private void decode(byte[] raw) {
        valid   = (raw[1] & 0x80)==0;

        sec     = raw[0] & 0x3f;
        min     = raw[1] & 0x3f;
        hour    = raw[2] & 0x1f;
        day     = raw[3] & 0x1F;
        mon     = (raw[4] & 0x0F) -1;
        year    = 2000 + ( ((raw[3] & 0xE0) >> 5) | ((raw[4] & 0xF0) >> 1) );

        dayOfWeek = (raw[2] & 0xE0) >> 5;
        dst = (raw[0] & 0x40) >> 6;
        leap = (raw[0] & 0x80) >> 7;
        dstSign = (raw[1] & 0x40) >> 6;
        dstDev = (raw[5] & 0xC0) >> 6;

    }

    public long getTimeInMillis() {
        return getDate().getTime();
    }

    public Date getDate(){
        Calendar cal = getCalendarInstance();
        cal.clear();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        cal.set(year, mon, day, hour, min, sec);
        return cal.getTime();
    }


    public byte[] getEncoded() {
        return encode(false);
    }

    public byte[] getEncoded(boolean immediateActivation) {
        return encode(immediateActivation);
    }

    public boolean isValid() {
        return valid;
    }

    public int getDstDev() {
        return dstDev;
    }

    public int getSeconds() {
        return sec;
    }

    public int getMinutes() {
        return min;
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return mon;
    }

    public int getYear() {
        return year;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDst() {
        return dst;
    }

    public boolean getLeap() {
        return leap == 1;
    }

    public int getDstSign() {
        return dstSign;
    }

    public Calendar getCalendarInstance() {
        return Calendar.getInstance(getTimeZone());
    }

    private TimeZone getTimeZone() {
        return TimeZone.getTimeZone("UTC");
    }
}
