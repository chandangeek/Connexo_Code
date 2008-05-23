/*
 * DemandData.java
 *
 * Created on 23 juni 2006, 16:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.cbo.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class DemandData extends AbstractTable {
    
    private final int DEBUG=10;
    
    private byte[] data;
    private List demandValuesList;
    Date previousDate=null;
    private int profilePeriod = 300;

	private int infoDeCoupure;

	private int posteHoraire = -1;

	private int valDePuissance;
    
    public DemandData(DataFactory dataFactory) {
        super(dataFactory);
    }
    
    protected int getCode() {
        return 8;		// 8 instead of 4 like for the CVE
//    	return 12;
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandData:\n");        
        for (int i=0;i<getDemandValuesList().size();i++) {
            DemandValues demandValues = (DemandValues)getDemandValuesList().get(i);
            strBuff.append("    demandValues["+i+"]="+demandValues+"\n");
        }   
        
        try {
            List ids = getIntervalDatas();
            for (int i=0;i<ids.size();i++) {
                IntervalData id = (IntervalData)ids.get(i);
                strBuff.append("    id="+id+"\n");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
        return strBuff.toString();
    }
    
    private int getProfileInterval() throws IOException {
        if (getDataFactory()==null) 
            return 300;
        else
            return getDataFactory().getTrimaran().getProfileInterval();
    }
    
    private TimeZone getTimeZone() {
        if (getDataFactory()==null)
            return TimeZone.getTimeZone("ECT");
        else
            return getDataFactory().getTrimaran().getTimeZone();
    }
    
    private void addValue(DemandValues demandValues, Interval val) {
        if (demandValues != null)
            demandValues.addValue(val);
    }
    
    private DemandValues createDemandValues(Calendar cal, int tariff) {
        DemandValues demandValues = new DemandValues(cal,tariff);
        getDemandValuesList().add(demandValues);
        return demandValues;
    }    
        
    // correct for the month of februari
    private void validateTimestamps() throws IOException { 
        Calendar previousIntervalCalendar=null;
        for (int i=(getDemandValuesList().size()-1);i>=0;i--) {
            DemandValues dvs = (DemandValues)getDemandValuesList().get(i);
            Calendar intervalCalendar = dvs.getCal();
             if ((previousIntervalCalendar != null) && (intervalCalendar.getTime().after(previousIntervalCalendar.getTime())))
                 intervalCalendar.add(Calendar.DAY_OF_MONTH,-12);
             previousIntervalCalendar = intervalCalendar;
        }
    }
    
    protected void parse(byte[] data) throws IOException { 
        this.setData(data);

        
//        System.out.println("KV_DEBUG> write to file");
//        File file = new File("c://TEST_FILES/Trimaran2.bin");
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(data);
//        fos.close();
        
        setDemandValuesList(new ArrayList());
        int count;
        int offset=0;
        DemandValues demandValues = null; //new DemandValues(Calendar.getInstance(), 1); 
        
        //**************************************************************
        // change this back to the calendar form the meter ...
//        Calendar retrieveCalendar = getRetrievalCalendar();
        Calendar retrieveCalendar = Calendar.getInstance();
        
        try {
            while(true) {
            	
            	if (offset == data.length)
            		break;
            	
                int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
                    
                if ((temp&0x8000)==0) {
                    if (DEBUG>=2) System.out.println("value = "+temp); 
                    infoDeCoupure = (temp&0x6000) >> 13;
                    if( (demandValues != null) && (posteHoraire == -1) ){
                    	posteHoraire = (temp&0x1800) >> 11 ;		
                    	demandValues.setTariff(posteHoraire);
                    }
                    valDePuissance = temp&0x07FF;
                    
                    switch(infoDeCoupure){
                    case 0:{
                    	addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.OK));
                    }break;
                    case 1:{
                    	addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.POWERDOWN|IntervalStateBits.POWERUP));
                    }break;
                    case 2:{
                    	if(valDePuissance==0)
                    		addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.MISSING));
                    	//TODO grande coupure - greater then 60s, but can be in same interval? 
                    	// So wait until next interval to see
                    }break;
                    case 3:{
                    	addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.SHORTLONG));
                    }break;
                    default:{
                    	//TODO check this
                    	addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.OTHER));
                    }break;
                    }

                } else {
                    if ((temp&0x4000)==0) {		// l'élément-dates
                    	if(DEBUG>=2)System.out.println("dates");
                    	int temp2 = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
                    	if(((temp2&0x4000) >> 14) == 1){
                    		Calendar cal = getCurrentDate(temp&0x1FFF, temp2&0x1FFF, getTimeZone());
                    		if (DEBUG>=2) System.out.println("date = "+cal.getTime());
                    		posteHoraire = -1;
                    		demandValues = createDemandValues(cal, 0);
                    	}
                    	else{
                    		// something freaky happened, do something
                    	}
                    } else {					// l'élément-heures
                    	// should never get here, the dates come first.
                    	if(DEBUG>=2)System.out.println("houres");
                    }
                }
            } // while(true)
            
            validateTimestamps();
            
        } catch(IOException e) {
            e.printStackTrace();
        }        
    } // protected void parse(byte[] data) throws IOException
    
    private Calendar getCurrentDate(int date, int heure, TimeZone timeZone) {
    	Calendar intervalCalendar = Calendar.getInstance(timeZone);
    	intervalCalendar.set(Calendar.DAY_OF_MONTH, (date&0x1F00) >> 8);
    	intervalCalendar.set(Calendar.MONTH, ((date&0x00F0) >> 4) - 1);
    	int yearUnit = date&0x000F;
    	intervalCalendar.set(Calendar.YEAR, (intervalCalendar.get(Calendar.YEAR)/10)*10 + yearUnit);
    	intervalCalendar.set(Calendar.HOUR_OF_DAY, (heure&0x1F00) >> 8);
    	intervalCalendar.set(Calendar.MINUTE, ((heure&0x00F0) >> 4)*(profilePeriod/60) );
    	intervalCalendar.set(Calendar.SECOND, 0);
    	intervalCalendar.set(Calendar.MILLISECOND, 0);
    	return intervalCalendar;
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
                 else intervalMonth=retrievalMonth;
                 //intervalMonth = retrievalMonth--<=0?11:retrievalMonth;
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
        else
            return getDataFactory().getTrimaran().getDataFactory().getCurrentMonthInfoTable().getTimestampCalendar();
    }
    
    private byte[] loadTestValues() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(baos);
        addIntervalValues(leos,8);
        addIntervalValues(leos,9);
        addIntervalValues(leos,10);
        addIntervalValues(leos,11);
        addIntervalValues(leos,12);
        addIntervalValues(leos,13);
        addIntervalValues(leos,14);
        leos.writeLEInt(0xFFFFFFFF);
        addIntervalValues(leos,1);
        addIntervalValues(leos,2);
        addIntervalValues(leos,3);
        addIntervalValues(leos,4);
        addIntervalValues(leos,5);
        addIntervalValues(leos,6);
        addIntervalValues(leos,7);
        
        return baos.toByteArray();
        
/*        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File file = new File("C:/Documents and Settings/koen/My Documents/projecten/edf/trimeran.txt");
        FileInputStream fis = new FileInputStream(file);
        while(true) {
            byte[] data= new byte[2];
            int retval = fis.read(data);
            if (retval==-1) {
                fis.close();
                return baos.toByteArray();
                
            }
            String str = new String(new byte[]{data[0],data[1]});
            int temp = Integer.parseInt(str,16);
            baos.write(temp);
        } // while(true)
 */
    }    
    
    // only for testing...
    static public void main(String[] args) {
        try {
            DemandData dv = new DemandData(null);
                    
        File file = new File("c://TEST_FILES/Trimaran2.bin");
        FileInputStream fis = new FileInputStream(file);
        byte[] data=new byte[(int)file.length()];
        fis.read(data);
        fis.close();             
        
            dv.parse(data);
            System.out.println(dv);
        }
        catch(IOException e) {
            e.printStackTrace();
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
        ChannelInfo channelInfo = new ChannelInfo(0,"Trimaran CJE kW channel",Unit.get("kW"));
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
            if (com.energyict.cbo.Utils.areEqual(intervalData.getEndTime(), intervalData2add.getEndTime())) {
               ParseUtils.addIntervalValues(intervalData, intervalData2add);
               intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
               intervalDatas.remove(i);
            }
        } // for (int i=0;i<(intervalDatas.size()-1);i++)
    }
}
