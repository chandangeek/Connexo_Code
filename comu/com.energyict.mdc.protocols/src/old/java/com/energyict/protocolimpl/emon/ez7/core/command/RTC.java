/*
 * RTC.java
 *
 * Created on 17 mei 2005, 14:37
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.common.NestedIOException;

import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class RTC extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String READCOMMAND="RT";
    private static final String WRITECOMMAND="ST";
    Date date;
    /** Creates a new instance of RTC */
    public RTC(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        return getDate().toString();
    }

    public void write() throws IOException {
        write(0);
    }

    public void write(int roundTripCorrection) throws IOException {
        TimeZone tz = ez7CommandFactory.getEz7().getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.add(Calendar.MILLISECOND,roundTripCorrection);
        sdf.applyPattern("MM/dd/yy");
        String date = sdf.format(cal.getTime());
        sdf.applyPattern("HH:mm:ss");
        String time = sdf.format(cal.getTime());
        String writeStr = date+" "+cal.get(Calendar.DAY_OF_WEEK)+" "+time+" "+(tz.inDaylightTime(cal.getTime())?"01":"00");
        if (DEBUG>=1) {
            System.out.println(writeStr);
        }
        ez7CommandFactory.getEz7().getEz7Connection().sendCommand(WRITECOMMAND,writeStr);
    }

    public void build() throws IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(READCOMMAND);
        parse(data);
    }

    private void parse(byte[] data) throws IOException {

        if (DEBUG>=1) {
            System.out.println(new String(data));
        }
        String dateTimeStr = new String(data);
        dateTimeStr = dateTimeStr.replaceAll("\r\n"," ");
        StringTokenizer strTok = new StringTokenizer(dateTimeStr," ");
        String date=null,time=null;
        while(strTok.hasMoreTokens()) {
             date = strTok.nextToken();
             if (date.contains("/")) {
                 break;
             }
        }
        while(strTok.hasMoreTokens()) {
             time = strTok.nextToken();
             if (time.contains(":")) {
                 break;
             }
        }
        //String weekDay = strTok.nextToken();
        //String ds = strTok.nextToken();

        if ((time==null) || (date==null)) {
            throw new IOException("RTC, time and/or date string is null, cannot continue!");
        }

        //Calendar cal = ProtocolUtils.getCleanCalendar(ez7CommandFactory.getEz7().getTimeZone());
        DateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        sdf.setTimeZone(ez7CommandFactory.getEz7().getTimeZone());

        try {
            setDate(sdf.parse(date+" "+time));
        }
        catch(ParseException e) {
            throw new NestedIOException(e,"RTC, parse, Error parsing the date time string!");
        }

    }

    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date getDate() {
        return date;
    }

    /**
     * Setter for property date.
     * @param date New value of property date.
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }



}

















