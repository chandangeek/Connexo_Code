/*
 * DemandData.java
 *
 * Created on 23 juni 2006, 16:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocols.util.LittleEndianOutputStream;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class DemandData extends AbstractTable {

    private final int DEBUG=0;

    private byte[] data;
    private List demandValuesList;
    Date previousDate=null;

    public DemandData(DataFactory dataFactory) {
        super(dataFactory);
    }

    protected int getCode() {
        return 4;
    }

    public String toString() {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("DemandData:\n");
        for (int i=0;i<getDemandValuesList().size();i++) {
            DemandValues demandValues = (DemandValues)getDemandValuesList().get(i);
            strBuff.append("    demandValues[").append(i).append("]=").append(demandValues).append("\n");
        }

        try {
            List ids = getIntervalDatas();
            for (int i=0;i<ids.size();i++) {
                IntervalData id = (IntervalData)ids.get(i);
                strBuff.append("    id=").append(id).append("\n");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }


        return strBuff.toString();
    }

    private int getProfileInterval() throws IOException {
        if (getDataFactory()==null) {
            return 600;
        }
        else {
            return getDataFactory().getTrimeran().getProfileInterval();
        }
    }

    private TimeZone getTimeZone() {
        if (getDataFactory()==null) {
            return TimeZone.getTimeZone("ECT");
        }
        else {
            return getDataFactory().getTrimeran().getTimeZone();
        }
    }



    private void addValue(DemandValues demandValues, Interval val) {
        if (demandValues != null) {
            demandValues.addValue(val);
        }
    }

    private DemandValues createDemandValues(Calendar cal, int tariff) {
        DemandValues demandValues = new DemandValues(cal,tariff);
        getDemandValuesList().add(demandValues);
        return demandValues;
    }

    // correct for the month of februari
    private void validateTimestamps() {
        Calendar previousIntervalCalendar=null;
        for (int i=(getDemandValuesList().size()-1);i>=0;i--) {
            DemandValues dvs = (DemandValues)getDemandValuesList().get(i);
            Calendar intervalCalendar = dvs.getCal();
             if ((previousIntervalCalendar != null) && (intervalCalendar.getTime().after(previousIntervalCalendar.getTime()))) {
                 intervalCalendar.add(Calendar.DAY_OF_MONTH, -12);
             }
             previousIntervalCalendar = intervalCalendar;
        }
    }

    protected void parse(byte[] data) throws IOException {
        this.setData(data);


//System.out.println("KV_DEBUG> write to file");
//        File file = new File("trimeran.bin");
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(data);
//        fos.close();

        setDemandValuesList(new ArrayList());
        int offset=0;
        DemandValues demandValues = null; //new DemandValues();
        Calendar retrieveCalendar = getRetrievalCalendar();
        int state=0;
        int roundtrip=0;

        try {
            while(true) {
                int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
                if (offset >= data.length) {
                    offset=0; // circular buffer
                    if (roundtrip++ > 3) {
                        throw new IOException("DemandData, parse, Error parsing load profile data!");
                    }
                }

                if (state < 2) {
                    if (temp == 0xFFFF) {
                       if (DEBUG>=2) {
                           System.out.println("KV_DEBUG> 1) END OF RECENT DATA *****************************");
                       }
                       state++;

                    }
                    continue;
                } // (state < 2)
                else if (state >= 2) {

                    if (temp == 0xFFFF) {
                       if (DEBUG>=2) {
                           System.out.println("KV_DEBUG> 2) END OF RECENT DATA *****************************");
                       }
                       state++;
                       if (state < 4) {
                           continue;
                       }
                       else {
                           break;
                       }
                    }

                    if (state==4) {
                        break;
                    }

                    // parser
                    if ((temp&0x8000)==0) {
                        if (DEBUG>=2) {
                            System.out.println("value = " + temp); // value without powerfail
                        }
                        addValue(demandValues,new Interval(temp));
                    } else {
                        if ((temp&0x4000)==0) {
                            int val = ((temp&0x3FFF)*2);
                            if (DEBUG>=2) {
                                System.out.println("value = " + val + " (powerfail)"); // value with powerfail
                            }
                            if (val != 0) {
                                addValue(demandValues, new Interval(((temp & 0x3FFF) * 2), IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP));
                            }
                            else {
                                addValue(demandValues, new Interval(((temp & 0x3FFF) * 2), IntervalStateBits.MISSING));
                            }

                        } else {
                            Calendar cal = parseCalendar(temp&0x3FFF,getTimeZone(),retrieveCalendar);
                            if (DEBUG>=2) {
                                System.out.println("date = " + cal.getTime());
                            }
                            int tariff = (temp & 0x3000)>>12;
                            demandValues = createDemandValues(cal,tariff);
                        }
                    }
                } // (state == 2)

            } // while(true)

            validateTimestamps();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Calendar parseCalendar(int val, TimeZone timeZone, Calendar retrievalCalendar) throws IOException {

        Calendar intervalCalendar = getCalendarDayAndMonth((val & 0x0F00)>>8, retrievalCalendar);
        int hour = (val & 0x00F8)>>3;
        int interval = val & 0x0007;
        intervalCalendar.set(Calendar.HOUR_OF_DAY,hour);
        intervalCalendar.set(Calendar.MINUTE,interval*(getProfileInterval()/60));

        // KV 30102006 fix to adjust load profile
        // SSSSSWWWW
        //      |
        //      --> Transition from summer to wintertime AND previous time with current differs <= 2 hour BUT > 1 hour --> subtract 1 hour from current
        if (previousDate != null) {
            if (timeZone.inDaylightTime(previousDate) && !timeZone.inDaylightTime(intervalCalendar.getTime())) {
                long diff = (intervalCalendar.getTime().getTime() - previousDate.getTime())/1000;
                if ((diff<=7200) && (diff>3600)) {
                    intervalCalendar.add(Calendar.HOUR_OF_DAY,-1);
                }
            }
        }
        previousDate = intervalCalendar.getTime();


        return intervalCalendar;

    } // private Date parseDate(int val, TimeZone timeZone)


    private Calendar getCalendarDayAndMonth(int quinzaineDay, Calendar retrievalCalendar) {


         int retrievalDayOfMonth = retrievalCalendar.get(Calendar.DAY_OF_MONTH);
         int retrievalMonth = retrievalCalendar.get(Calendar.MONTH);
         int intervalYear = retrievalCalendar.get(Calendar.YEAR);
         int intervalDay;
         int intervalMonth;

         if (retrievalDayOfMonth < 16) {
             if (quinzaineDay > retrievalDayOfMonth) {
                 // previous month, quinzaine 2
                 intervalDay = quinzaineDay+16; // 0=16, 1=17, ... 15=31
                 if (retrievalMonth--<=0) {
                    intervalMonth = 11;
                    intervalYear--;
                 }
                 else {
                     intervalMonth = retrievalMonth;
                 }
             }
             else {
                 // current month, quinzaine 1
                 intervalDay = quinzaineDay; // 1=1, 2=2, ... 15=15
                 intervalMonth = retrievalMonth;
             }

         }
         else {
             if (quinzaineDay > (retrievalDayOfMonth-16)) {
                 // current month, quinzaine 1
                 intervalDay = quinzaineDay; // 1=1, 2=2, ... 15=15
                 intervalMonth = retrievalMonth;
             }
             else {
                 // current month, quinzaine 2
                 intervalDay = quinzaineDay+16; // 0=16, 1=17, ... 15=31
                 intervalMonth = retrievalMonth;
             }
         }

         Calendar intervalCal = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("ECT"));
         intervalCal.set(Calendar.YEAR,intervalYear);
         intervalCal.set(Calendar.MONTH,intervalMonth);
         intervalCal.set(Calendar.DAY_OF_MONTH,intervalDay);
         return intervalCal;
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }



    // only for testing...
    private int getTestTimestamp(int quinzaineDay, int hour) {
        int temp = 0xC000;
        temp = temp | (quinzaineDay<<8);
        temp = temp | (hour<<3);

        return temp;
    }
    private void addIntervalValues(LittleEndianOutputStream leos, int quinzaineDay) throws IOException {

        for (int hour=0;hour<24;hour++) {
            leos.writeLEShort((short)getTestTimestamp(quinzaineDay,hour));
            leos.writeLEShort((short)100);
            leos.writeLEShort((short)200);
            leos.writeLEShort((short)300);
            leos.writeLEShort((short)400);
            leos.writeLEShort((short)500);
            leos.writeLEShort((short)600);
        }
    }


    private Calendar getRetrievalCalendar() throws IOException {
        if (getDataFactory()==null) {
            Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
            cal.set(Calendar.YEAR,2007);
            cal.set(Calendar.MONTH,9);
            cal.set(Calendar.DAY_OF_MONTH,30);
            cal.set(Calendar.HOUR_OF_DAY,10);
            cal.set(Calendar.MINUTE,15);
            return cal;
        }
        else {
            return getDataFactory().getTrimeran().getDataFactory().getCurrentMonthInfoTable().getTimestampCalendar();
        }
    }

    public List getDemandValuesList() {
        return demandValuesList;
    }

    private void setDemandValuesList(List demandValuesList) {
        this.demandValuesList = demandValuesList;
    }

    public List getChannelInfos() {
        List channelInfos = new ArrayList();
        ChannelInfo channelInfo = new ChannelInfo(0,"Trimeran CVE kW channel", Unit.get("kW"));
        channelInfos.add(channelInfo);
        return channelInfos;
    }

    public List getIntervalDatas() throws IOException {
        List intervalDatas = new ArrayList();

        Iterator it = getDemandValuesList().iterator();
        while(it.hasNext()) {
            DemandValues dvs = (DemandValues)it.next();

            Calendar cal = dvs.getCal();
            int tariff = dvs.getTariff();
            cal.add(Calendar.SECOND, getProfileInterval());
            ParseUtils.roundDown2nearestInterval(cal, getProfileInterval());

            Iterator it2 = dvs.getIntervals().iterator();
            while(it2.hasNext()) {
                Interval interval = (Interval)it2.next();
                IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()),interval.getEiStatus(),0,tariff);
                intervalData.addValue(interval.getValue());
                intervalDatas.add(intervalData);
                cal.add(Calendar.SECOND, getProfileInterval());

            }
        }

        validateIntervalDatas(intervalDatas);

        return intervalDatas;
    }

    protected void validateIntervalDatas(List intervalDatas) {
        IntervalData intervalData,intervalData2add;
        for (int i=0;i<(intervalDatas.size()-1);i++) {
            intervalData2add = (IntervalData)intervalDatas.get(i);
            intervalData = (IntervalData)intervalDatas.get(i+1);
            if (is(intervalData.getEndTime()).equalTo(intervalData2add.getEndTime())) {
                ParseUtils.addIntervalValues(intervalData, intervalData2add);
                intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                intervalDatas.remove(i);
            }
        }
    }

}