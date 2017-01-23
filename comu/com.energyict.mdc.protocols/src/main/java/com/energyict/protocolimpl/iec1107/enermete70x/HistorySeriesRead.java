/*
 * HistorySeriesRead.java
 *
 * Created on 28 oktober 2004, 14:29
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class HistorySeriesRead extends AbstractDataReadingCommand {

    private static final int DEBUG=0;

    LoadProfileDataBlock loadProfileDataBlock=null;
    int nrOfIntervals=0;

    /** Creates a new instance of HistorySeriesRead */
    public HistorySeriesRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        int pos=0;
        Date firstStartingDate=null;
        List loadProfileEntries=new ArrayList();
        int profileInterval=-1;
        DataParser dp = new DataParser(timeZone);
        String strExpression = new String(data);
        if (DEBUG >=1)
            System.out.println(strExpression);
        //DataParser gebruiken en loadProfileEnties opbouwen
        try {
            // parse nr of intervals + timestamp
            for (pos=0;pos<=nrOfIntervals;pos++) {
                String str = dp.parseBetweenBrackets(strExpression,pos);
                if (pos == 0) {
                    firstStartingDate = dp.parseDateTime(str.substring(str.indexOf(',')+1));
                    profileInterval=Integer.parseInt(str.substring(0,str.indexOf(',')))*60;
                    if (profileInterval != getDataReadingCommandFactory().getEnermet().getProfileInterval())
                         throw new IOException("HistorySeriesRead, parse, DataParseException, error different profileInterval bewteen meter ("+profileInterval+") and configured ("+getDataReadingCommandFactory().getEnermet().getProfileInterval()+")");
                }
                else {
                    int status;
                    if (str.compareTo("") == 0) {
                        loadProfileEntries.add(new LoadProfileEntry());
                    }
                    else {
                        status=0;
                        String str2 = str.split("\\*")[0];
                        BigDecimal bd=null;
                        if (str2.indexOf(',') != -1) {
                            status = Integer.parseInt(str2.split(",")[1]);
                            bd = new BigDecimal(str2.split(",")[0]);
                        }
                        else
                            bd = new BigDecimal(str2);

                        Unit unit = Unit.get(str.split("\\*")[1]);
                        loadProfileEntries.add(new LoadProfileEntry(new Quantity(bd,unit),status));
                    }

                }
            } // for (pos=0;pos<nrOfIntervals;pos++)
        }
        catch(DataParseException e) {
            throw new IOException("HistorySeriesRead, parse, DataParseException, probably wrong profileInterval, meter "+profileInterval+", configured "+getDataReadingCommandFactory().getEnermet().getProfileInterval());
        }
        loadProfileDataBlock = new LoadProfileDataBlock(firstStartingDate,loadProfileEntries,profileInterval);
    }

    /**
     * Getter for property loadProfile.
     * @return Value of property loadProfile.
     */
    public LoadProfileDataBlock getLoadProfileDataBlock(Date from, int channel) throws IOException {
        int profileInterval = getDataReadingCommandFactory().getEnermet().getProfileInterval();
        //Date to = new Date();
        //int period = (int)(to.getTime() - from.getTime());
        //int nrOfIntervalsToRetrieve = (period / 1000) / profileInterval;
        int nrOfIntervalsPerDay = (24*3600) / profileInterval;
        return getLoadProfileDataBlock(from, nrOfIntervalsPerDay, channel);
    }

    public LoadProfileDataBlock getLoadProfileDataBlock(Date from,int nrOfIntervals, int channel) throws IOException {
        this.nrOfIntervals=nrOfIntervals;
//        Calendar cal = ProtocolUtils.getCleanCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar cal = ProtocolUtils.getCleanCalendar(getDataReadingCommandFactory().getEnermet().getTimeZone());
        cal.setTime(from);
        retrieve("HSR",channel+","+
        nrOfIntervals+","+
        cal.get(Calendar.YEAR)+","+
        (cal.get(Calendar.MONTH)+1)+","+
        cal.get(Calendar.DAY_OF_MONTH)+","+
        cal.get(Calendar.HOUR_OF_DAY)+","+
        cal.get(Calendar.MINUTE)+","+
        cal.get(Calendar.SECOND));

        return getLoadProfileDataBlock();
    }

    public String toString() {
        return getLoadProfileDataBlock().toString();
    }

    /**
     * Getter for property loadProfileDataBlock.
     * @return Value of property loadProfileDataBlock.
     */
    private LoadProfileDataBlock getLoadProfileDataBlock() {
        return loadProfileDataBlock;
    }


}
