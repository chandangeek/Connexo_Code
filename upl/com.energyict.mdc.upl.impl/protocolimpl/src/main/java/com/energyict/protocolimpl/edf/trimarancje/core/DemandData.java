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
import com.energyict.protocolimpl.ansi.c12.procedures.SetDateTime;
import com.energyict.protocolimpl.base.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class DemandData extends AbstractTable {
    
    private final int DEBUG=0;
    
    private byte[] data;
    private List demandValuesList;
    Date previousDate=null;
    private int choise = 0;
    private int[] profileChoises = {300, 600, 900};		// 5 - 10 - 15 minutes 
    private int profileInterval = -1;
	private int infoDeCoupure;
	private int posteHoraire = -1;
	private int valDePuissance;
	private String[] units = {"kVA", "kW"};
    
    public DemandData(DataFactory dataFactory) {
        super(dataFactory);
    }
    
    protected int getCode() {
        return 8;		// 8 instead of 4 like for the CVE
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
    
    public int getProfileInterval() {
    	if(profileInterval == -1)
    		return profileChoises[choise];
    	else
    		return profileInterval;
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
        
    public void parse(byte[] data) throws IOException { 
        this.setData(data);
        
//        System.out.println("KV_DEBUG> write to file");
//        File file = new File("c://TEST_FILES/A12E_TJ_Actaris.bin");
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(data);
//        fos.close();
        
        calculateProfileInterval();
        
        setDemandValuesList(new ArrayList());
        int count;
        int offset=0;
        DemandValues demandValues = null; //new DemandValues(Calendar.getInstance(), 1); 
        
        Calendar traceCalendar = null;
        
        try {
            while(true) {
            	
            	if (offset == data.length)
            		break;
            	
                int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
                    
                if ((temp&0x8000)==0) {
                	if(traceCalendar != null)
                		traceCalendar.add(Calendar.SECOND, 300);
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
                    		if (traceCalendar == null){
                    			traceCalendar = Calendar.getInstance();
                    			traceCalendar.setTime(cal.getTime());
                    		}
                    		if ( cal.getTimeInMillis() != traceCalendar.getTimeInMillis() )
                    			fillInTheGaps(cal, traceCalendar, demandValues);
                    		posteHoraire = -1;
                    		demandValues = createDemandValues(cal, 0);
                    	}
                    	else{
                    		// something freaky happened, do something...
                    		throw new IOException("Couldn't parse the received data.");
                    	}
                    } else {					// l'élément-heures
                    	// should never get here, the dates come first.
                    	if(DEBUG>=2)System.out.println("houres");
                    }
                }
            } // while(true)
            
//            validateTimestamps();
            
        } catch(IOException e) {
            e.printStackTrace();
        }        
    } // protected void parse(byte[] data) throws IOException
    

	private void calculateProfileInterval() {
    	int offset = 0;
    	int counter = 0;
    	long interval;
    	Calendar cal1 = null;
    	Calendar cal2 = null;
    	try {
			while (true) {
				if (offset == getData().length)
					break;

				int temp = ProtocolUtils.getIntLE(getData(), offset, 2);
				offset += 2;
				
				if (((temp&0x6000) >> 13) == 2){ 	// in case of a gap!
					if(checkGap(offset-2))
						break;
				}
				
				if(cal1 != null) counter++;
				
				if (((temp&0x8000)>>15)==1) {
					int temp2 = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
					if(((temp2&0x4000) >> 14) == 1){
						if(cal1 == null)
							cal1 = getCurrentDate(temp&0x1FFF, temp2&0x1FFF, getTimeZone());
						else{
							cal2 = getCurrentDate(temp&0x1FFF, temp2&0x1FFF, getTimeZone());
							interval = (cal2.getTimeInMillis() - cal1.getTimeInMillis())/(1000*(counter-1));
							if((interval == profileChoises[0]) || (interval == profileChoises[1]) || (interval == profileChoises[2])){
								setProfileInterval((int)interval);
								break;
							}
							else{
								choise++;
								if(choise == 3){
									setProfileInterval(getDataFactory().getTrimaran().getMeterProfileInterval());
									break;
								}
								offset = 0;
								counter = 0;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();		
		}  
	}

	private boolean checkGap(int i) throws IOException {
		int offset = i;
		int counter = 0;
		while(true){
			
			if (offset == getData().length)
				return false;
			
			int temp = ProtocolUtils.getIntLE(getData(), offset, 2);
			offset += 2;
			
			if (((temp&0x6000) >> 13) == 2){
				counter++;
			}
			else if ((temp&0x8000)==0){
				return false;
			}
			else{
				setProfileInterval(3600/(counter-1));
				return true;
			}
		}
	}

	private void fillInTheGaps(Calendar cal, Calendar traceCalendar, DemandValues demandValues) {
		while(cal.getTimeInMillis() != traceCalendar.getTimeInMillis()){
			addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.MISSING));
			traceCalendar.add(Calendar.SECOND, 300);
		}
	}

	private Calendar getCurrentDate(int date, int heure, TimeZone timeZone) {
    	Calendar intervalCalendar = Calendar.getInstance(timeZone);
    	intervalCalendar.set(Calendar.DAY_OF_MONTH, (date&0x1F00) >> 8);
    	intervalCalendar.set(Calendar.MONTH, ((date&0x00F0) >> 4) - 1);
    	int yearUnit = date&0x000F;
    	intervalCalendar.set(Calendar.YEAR, (intervalCalendar.get(Calendar.YEAR)/10)*10 + yearUnit);
    	intervalCalendar.set(Calendar.HOUR_OF_DAY, (heure&0x1F00) >> 8);
    	intervalCalendar.set(Calendar.MINUTE, ((heure&0x00F0) >> 4)*(getProfileInterval()/60)); 
    	intervalCalendar.set(Calendar.SECOND, 0);
    	intervalCalendar.set(Calendar.MILLISECOND, 0);
    	return intervalCalendar;
	}

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }
    
    // only for testing...
    static public void main(String[] args) {
        try {
            DemandData dv = new DemandData(null);
                    
	        File file = new File("c://TEST_FILES/A12E_TJ_Actaris.bin");
	        FileInputStream fis = new FileInputStream(file);
	        byte[] data=new byte[(int)file.length()];
	        fis.read(data);
	        fis.close();             
	        
//	        dv.setData(data);
//	        dv.calculateProfileInterval();
	        dv.parse(data);
	        
//	        System.out.println(dv.getProfileInterval());
	        
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
    
    public List getChannelInfos(){
    	return getChannelInfos(0);
    }
    
    public List getChannelInfos(int i) {
        List channelInfos = new ArrayList();
        ChannelInfo channelInfo = new ChannelInfo(0,"Trimaran CJE kW channel",Unit.get(units[i]));
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

	/**
	 * @param profileInterval the profileInterval to set
	 */
	public void setProfileInterval(int profileInterval) {
		this.profileInterval = profileInterval;
	}
}
