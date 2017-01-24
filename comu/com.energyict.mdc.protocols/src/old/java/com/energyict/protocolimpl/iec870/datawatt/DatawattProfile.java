/*
 * DatawattProfile.java
 *
 * Created on 2 juli 2003, 13:43
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class DatawattProfile {

    DataWatt dataWatt = null;
    private static final int DEBUG=0;

    /** Creates a new instance of DatawattProfile */
    public DatawattProfile(DataWatt dataWatt) {
        this.dataWatt=dataWatt;
    }

    public ProfileData getProfileData(Calendar calendar,boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(buildChannelInfos());
        profileData.setIntervalDatas(buildIntervalDatas(calendar));
        profileData.setMeterEvents(buildMeterEvents(includeEvents));
        profileData.applyEvents(dataWatt.getProfileInterval()/60);
        return profileData;
    }

    private List buildChannelInfos() {
        List channelInfos = new ArrayList();
        // build channelinfos
        for (int i=0;i<dataWatt.getChannelMap().getNrOfChannels();i++) {
            ChannelInfo chi = new ChannelInfo(i,"datawatt_channel_"+i, Unit.get(""));
            if (dataWatt.getChannelMap().getChannel(i).isCumulative()) {
                chi.setCumulativeWrapValue(new BigDecimal(DataWatt.MAX_COUNTER));
            }
            channelInfos.add(chi);
        }
        return channelInfos;
    } // private List buildChannelInfos()

    private List buildIntervalDatas(Calendar calendar) throws IOException {
        int t,i;
        List intervalDatas = new ArrayList();
        Iterator it;
        IntervalData id = null;
        HistoricalValue hist=null;
        List partial=null;
        List historicalValues;

        // get historical data
        historicalValues = dataWatt.getApplicationFunction().historicalDataASDU(calendar);
        historicalValues = removeUnwantedHistoricalValues(historicalValues);
        if (DEBUG >=1) printHistoricalValuesList(historicalValues);

        // build intervaldata from historical data
        while(true) {
            partial = getChannelValues(historicalValues);
            if (partial == null) {
                if (intervalDatas.size() == 0)
                    dataWatt.getLogger().warning("No intervaldata build!");
                break;
            }
            if (dataWatt.getChannelMap().getNrOfChannels() != partial.size()) {
                dataWatt.getLogger().warning("Only "+partial.size()+" out of ChannelMap's "+dataWatt.getChannelMap().getNrOfChannels()+" channels found in historical data, adjust ChannelMap property!");
            }
            else {
                id=null;
                it = partial.iterator();
                while(it.hasNext()) {
                    hist = (HistoricalValue)it.next();
                    if (id==null)  id = new IntervalData(hist.getDate());
                    id.addValue(hist.getValue(),hist.getStatus(),hist.getEIStatus());
                }
                intervalDatas.add(id);
            }
        } // while(true)
        return intervalDatas;
    } // private List buildIntervalDatas(Calendar calendar)


    private static final int UNIGAS_TZ=0; // metertype
    private static final int UNIGASTZ_COMMUNICATIESTORING=12;
    private static final int UNIGASTZ_ALARM_BEGIN=5;
    private static final int UNIGASTZ_ALARM_END=11;

    private static final int UNIGAS_KAMSTRUP=1; // metertype
    private static final int UNIGASKAMSTRUP_POWERUP=12;
    private static final int UNIGASKAMSTRUP_ALARM_BEGIN=2;
    private static final int UNIGASKAMSTRUP_ALARM_END=5;

    private static final int NO_METER=2; // metertype
    private static final int NO_METER_COMMUNICATIESTORING=5;


    private List buildMeterEvents(boolean includeEvents) throws IOException {
        List meterEvents = new ArrayList();
        if (includeEvents) {
            List singlePointInfos = dataWatt.getApplicationFunction().getSinglePointInfos();
            Iterator it = singlePointInfos.iterator();
            while(it.hasNext()) {
                SinglePointInfo sp = (SinglePointInfo)it.next();
                // KV 17072003, only when input is High, generate alarm!
                if (sp.getValue().intValue() == 1) {
                    if (dataWatt.getMeterType() == UNIGAS_TZ) {
                        if (sp.getChannel().getChannelId()==UNIGASTZ_COMMUNICATIESTORING) meterEvents.add(new MeterEvent(sp.getDate(),MeterEvent.POWERUP,sp.getStatus()));
                        if ((sp.getChannel().getChannelId()>=UNIGASTZ_ALARM_BEGIN) && (sp.getChannel().getChannelId()<=UNIGASTZ_ALARM_END)) meterEvents.add(new MeterEvent(sp.getDate(),MeterEvent.METER_ALARM,sp.getStatus()));
                    }
                    else if (dataWatt.getMeterType() == UNIGAS_KAMSTRUP) {
                        if (sp.getChannel().getChannelId()==UNIGASKAMSTRUP_POWERUP) meterEvents.add(new MeterEvent(sp.getDate(),MeterEvent.POWERUP,sp.getStatus()));
                        if ((sp.getChannel().getChannelId()>=UNIGASKAMSTRUP_ALARM_BEGIN) && (sp.getChannel().getChannelId()<=UNIGASKAMSTRUP_ALARM_END)) meterEvents.add(new MeterEvent(sp.getDate(),MeterEvent.METER_ALARM,sp.getStatus()));
                    }
                    else if (dataWatt.getMeterType() == NO_METER) {
                        if (sp.getChannel().getChannelId()==NO_METER_COMMUNICATIESTORING) meterEvents.add(new MeterEvent(sp.getDate(),MeterEvent.POWERUP,sp.getStatus()));
                    }
                    else throw new IOException("DatawattProfile, buildMeterEvents, invalid meterType "+dataWatt.getMeterType());
                }
            }
        }

        return meterEvents;
    } // private List buildMeterEvents()

    private List getChannelValues(List historicalValues) {
        List partial = null;
        int count;
        Date date=null;
        Iterator itchannel = dataWatt.getChannelMap().getChannels().iterator();
        // get channel to compare with...
        while(itchannel.hasNext()) {
            Channel channel = (Channel)itchannel.next();
            // find first appearance of channel value, copy and remove from list
            Iterator ithist = historicalValues.iterator();
            while(ithist.hasNext()) {
                HistoricalValue hist = (HistoricalValue)ithist.next();
                if ((date == null) || (date.equals(hist.getDate()))) {
                    date = hist.getDate();
                    if (hist.getChannel().isEqual(channel)) {
                        if (partial==null) partial = new ArrayList();
                        partial.add(hist);
                        ithist.remove();
                        break;
                    }
                }
                else break;
            }
        }
        return partial;
    }


    private List removeUnwantedHistoricalValues(List historicalValues) {
        Iterator it = historicalValues.iterator();
        HistoricalValue hist=null;
        while(it.hasNext()) {
            hist = (HistoricalValue)it.next();
            if (dataWatt.getChannelMap().getChannel(hist.getChannel()) == null) it.remove();
        }
        return historicalValues;
    }

    protected void doLogMeterDataCollection(ProfileData profileData) {
        if (profileData == null) return;
        int i,iNROfChannels=profileData.getNumberOfChannels();
        int t,iNROfIntervals=profileData.getNumberOfIntervals();
        int z,iNROfEvents=profileData.getNumberOfEvents();
        System.out.println("Channels: "+iNROfChannels);
        System.out.println("Intervals par channel: "+iNROfIntervals);
        for (t=0;t<iNROfIntervals;t++) {
            System.out.println(" Interval "+t+" endtime = "+profileData.getIntervalData(t).getEndTime());
            for (i=0;i<iNROfChannels;i++) {
                System.out.println("Channel "+i+" Interval "+t+" = "+profileData.getIntervalData(t).get(i)+", status = "+profileData.getIntervalData(t).getEiStatus(i)+" "+profileData.getChannel(i).getUnit());
            }
        }
        System.out.println("Events in profiledata: "+iNROfEvents);
        for (z=0;z<iNROfEvents;z++) {
            System.out.println("Event "+z+" = "+profileData.getEvent(z).getEiCode()+", "+profileData.getEvent(z).getProtocolCode()+" at "+profileData.getEvent(z).getTime());
        }

    } // protected void doLogMeterDataCollection(ProfileData profileData)

    private void printHistoricalValuesList(List list) {
        if (list.size() > 0)
            System.out.println("****************************** printHistoricalValuesList ******************************");
        Iterator it = list.iterator();
        while(it.hasNext()) {
            System.out.println(((HistoricalValue)it.next()).toString());
        }
    }

} // public class DatawattProfile
