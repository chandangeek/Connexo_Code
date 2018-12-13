/*
 * RealTimeRW.java
 *
 * Created on 25 oktober 2004, 16:35
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
/**
 *
 * @author  Koen
 */
public class RealTimeRW extends AbstractDataReadingCommand {

    Date date=null;
    /** Creates a new instance of RealTimeRW */
    public RealTimeRW(DataReadingCommandFactory drcf) {
        super(drcf);

    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        data = getDataReadingCommandFactory().getEnermet().getIec1107Connection().parseDataBetweenBrackets(data);
        String str = new String(data);
        StringTokenizer strTok = new StringTokenizer(str,",");
        //Calendar calendar = ProtocolUtils.getCleanCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar calendar = ProtocolUtils.getCleanCalendar(getDataReadingCommandFactory().getEnermet().getTimeZone());
        calendar.set(Calendar.YEAR,Integer.parseInt(strTok.nextToken()));
        calendar.set(Calendar.MONTH,Integer.parseInt(strTok.nextToken())-1);
        calendar.set(Calendar.DAY_OF_MONTH,Integer.parseInt(strTok.nextToken()));
        calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(strTok.nextToken()));
        calendar.set(Calendar.MINUTE,Integer.parseInt(strTok.nextToken()));
        calendar.set(Calendar.SECOND,Integer.parseInt(strTok.nextToken()));
        date = calendar.getTime();
    }

    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date getDate() throws IOException {
        retrieve("RTR");
        return date;
    }

    public void setDate(Date date) throws IOException {
        //Calendar calendar = ProtocolUtils.getCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar calendar = ProtocolUtils.getCalendar(getDataReadingCommandFactory().getEnermet().getTimeZone());
        calendar.clear();
        calendar.setTime(date);
        String strDate =
            calendar.get(Calendar.YEAR)+","+
            (calendar.get(Calendar.MONTH)+1)+","+
            calendar.get(Calendar.DAY_OF_MONTH)+","+
            calendar.get(Calendar.HOUR_OF_DAY)+","+
            calendar.get(Calendar.MINUTE)+","+
            calendar.get(Calendar.SECOND);
       write("RTS",strDate);
    }

}