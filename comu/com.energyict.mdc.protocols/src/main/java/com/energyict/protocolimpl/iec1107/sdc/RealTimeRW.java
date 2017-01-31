/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeRW.java
 *
 * Created on 25 oktober 2004, 16:35
 * Added shorterDateStr - 04/12/07 (gna)
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
/**
 *
 * @author gna
 * <B>@beginchanges</B><BR>
 * GN|30012008| Adjusted the setDate according to the spec
 * @endchanges
 *
 */
public class RealTimeRW extends AbstractDataReadingCommand {

    Date date=null;
    /** Creates a new instance of RealTimeRW */
    public RealTimeRW(DataReadingCommandFactory drcf) {
        super(drcf);

    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        data = getDataReadingCommandFactory().getSdc().getIec1107Connection().parseDataBetweenBrackets(data);
        String str = shorterDateStr( new String(data) );
        StringTokenizer strTok = new StringTokenizer(str,",");
        //Calendar calendar = ProtocolUtils.getCleanCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar calendar = ProtocolUtils.getCleanCalendar(getDataReadingCommandFactory().getSdc().getTimeZone());
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
        Calendar calendar = ProtocolUtils.getCalendar(getDataReadingCommandFactory().getSdc().getTimeZone());
        calendar.clear();
        calendar.setTime(date);
        String strDate =
            calendar.get(Calendar.YEAR)+","+
            (calendar.get(Calendar.MONTH)+1)+","+
            calendar.get(Calendar.DAY_OF_MONTH)+","+
            calendar.get(Calendar.DAY_OF_WEEK)+","+
            calendar.get(Calendar.HOUR_OF_DAY)+","+
            calendar.get(Calendar.MINUTE)+","+
            calendar.get(Calendar.SECOND)+","+"80";
       write("RTS",strDate);
    }

    //

    public String shorterDateStr(String str){
    	String hulpStr;
    	hulpStr = (String) str.subSequence(0, 11) + ((String) str.subSequence(14, 22));
    	return hulpStr;
    }

}
