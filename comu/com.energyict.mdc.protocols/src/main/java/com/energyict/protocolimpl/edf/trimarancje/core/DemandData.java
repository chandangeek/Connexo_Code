/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.elster.jupiter.util.Checks.is;

/**
 *
 * @author Koen
 */
public class DemandData extends AbstractTable {

    private final int DEBUG=0;

    private byte[] data;
    private List demandValuesList;
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

    public int getProfileInterval() {
    	if(profileInterval == -1){
    		return profileChoises[choise];
    	} else {
			return profileInterval;
		}
    }

    private TimeZone getTimeZone() {
        if (getDataFactory()==null) {
			return TimeZone.getTimeZone("ECT");
		} else {
			return getDataFactory().getTrimaran().getTimeZone();
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

    public void parse(byte[] data) throws IOException {
        this.setData(data);
        calculateProfileInterval();

        setDemandValuesList(new ArrayList());
        int offset=0;
        DemandValues demandValues = null; //new DemandValues(Calendar.getInstance(), 1);
        Calendar traceCalendar = null;
        try {
            while(true) {
            	if (offset == data.length) {
					break;
				}

                int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;

                if ((temp&0x8000)==0) {
                	if(traceCalendar != null) {
						traceCalendar.add(Calendar.SECOND, getProfileInterval());
					}
                    if (DEBUG>=2) {
						System.out.println("value = "+temp);
					}
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
                    	if(valDePuissance==0) {
							addValue(demandValues, new Interval(valDePuissance, IntervalStateBits.MISSING));
                    	//TODO grande coupure - greater then 60s, but can be in same interval?
                    	// So wait until next interval to see
						}
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
                    	if (DEBUG>=2) {
							System.out.println("dates");
						}
                    	int temp2 = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
                    	if (((temp2&0x4000) >> 14) == 1) {
                    		Calendar cal = getCurrentDate(temp&0x1FFF, temp2&0x1FFF, getTimeZone());
                    		if (DEBUG>=2) {
								System.out.println("date = "+cal.getTime());
							}
                    		if (traceCalendar == null){
                    			traceCalendar = Calendar.getInstance();
                    			traceCalendar.setTime(cal.getTime());
                    		}
                    		if ( cal.getTimeInMillis() != traceCalendar.getTimeInMillis() ) {
								fillInTheGaps(cal, traceCalendar, demandValues);
							}
                    		posteHoraire = -1;
                    		demandValues = createDemandValues(cal, 0);
                    	}
                    	else{
                    		// something freaky happened, do something...
                    		throw new IOException("Couldn't parse the received data.");
                    	}
                    } else {					// l'élément-heures
                    	// should never get here, the dates come first.
                    	if (DEBUG>=2) {
							System.out.println("houres");
						}
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

	private void calculateProfileInterval() {
    	int offset = 0;
    	int counter = 0;
    	long interval;
    	Calendar cal1 = null;
    	Calendar cal2;
    	try {
			while (true) {
				if (offset == getData().length) {
					break;
				}

				int temp = ProtocolUtils.getIntLE(getData(), offset, 2);
				offset += 2;

				if (((temp&0x6000) >> 13) == 2){ 	// in case of a gap!
					if(checkGap(offset-2)) {
						break;
					}
				}

				if(cal1 != null) {
					counter++;
				}

				if (((temp&0x8000)>>15)==1) {
					int temp2 = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
					if(((temp2&0x4000) >> 14) == 1){
						if(cal1 == null) {
							cal1 = getCurrentDate(temp&0x1FFF, temp2&0x1FFF, getTimeZone());
						} else{
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

			if (offset == getData().length) {
				return false;
			}

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
			traceCalendar.add(Calendar.SECOND, getProfileInterval());
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

    public List getDemandValuesList() {
        return demandValuesList;
    }

    private void setDemandValuesList(List demandValuesList) {
        this.demandValuesList = demandValuesList;
    }

    public List getChannelInfos(){
    	return getChannelInfos(0);
    }

    public List<ChannelInfo> getChannelInfos(int i) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ChannelInfo channelInfo = new ChannelInfo(0,"Trimaran CJE kW channel",Unit.get(units[i]));
        channelInfos.add(channelInfo);
        return channelInfos;
    }

    public List<IntervalData> getIntervalDatas() throws IOException {
        List<IntervalData> intervalDatas = new ArrayList<>();

        Iterator it = getDemandValuesList().iterator();
        while(it.hasNext()) {
            DemandValues dvs = (DemandValues)it.next();

            Calendar cal = dvs.getCal();
            int tariff = dvs.getTariff();
            cal.add(Calendar.SECOND, getProfileInterval());
            ParseUtils.roundDown2nearestInterval(cal, getProfileInterval());

            for (Interval interval : dvs.getIntervals()) {
                IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()), interval.getEiStatus(), 0, tariff);
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

	/**
	 * @param profileInterval the profileInterval to set
	 */
	public void setProfileInterval(int profileInterval) {
		this.profileInterval = profileInterval;
	}
}
