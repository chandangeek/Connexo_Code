/*
 * MK10ProfileData.java
 *
 * Created on 4 april 2006, 14:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.protocolimpl.edmi.mk10.loadsurvey.*;

import java.io.*;
import java.util.*;

import com.energyict.protocol.*;

/**
 *
 * @author koen
 */
public class MK10Profile {
    
    private final int DEBUG=0;

    MK10 mk10;
    LoadSurvey loadSurvey=null;
    LoadSurvey eventLog=null;
    
    /** Creates a new instance of MK10ProfileData */
    public MK10Profile(MK10 mk10) {
        this.mk10=mk10;
    }
        
    private LoadSurvey getLoadSurvey() throws IOException {
    	if (loadSurvey==null) {
    		loadSurvey = new LoadSurvey(mk10.getCommandFactory(), mk10.getLoadSurveyNumber());            
        }
        return loadSurvey;
    }
    
    private LoadSurvey getEventLog() throws IOException {
        if (eventLog==null) {
            //TODO AANVULLEN
        }
        return eventLog;
    }
    
    public int getProfileInterval() throws IOException {
        return getLoadSurvey().getProfileInterval();
    }
    
    public int getNumberOfChannels() throws IOException {
        return getLoadSurvey().getNrOfChannels();
    }
    
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        ProfileData profileData=new ProfileData();
        
        if (DEBUG>=1) System.out.println("KV_DEBUG> "+getLoadSurvey());
        LoadSurveyData loadSurveyData = getLoadSurvey().readFile(from);
        if (DEBUG>=1) System.out.println("KV_DEBUG> "+loadSurveyData);

        profileData.setChannelInfos(buildChannelInfos(loadSurveyData));
        profileData.setIntervalDatas(buildIntervalDatas(loadSurveyData));
        
        if (includeEvents) {
            if (DEBUG>=1) System.out.println("KV_DEBUG> "+getEventLog());
            LoadSurveyData eventLogData = getEventLog().readFile(from);
            if (DEBUG>=1) System.out.println("KV_DEBUG> "+eventLogData);
            profileData.setMeterEvents(buildMeterEvents(eventLogData));
            profileData.applyEvents(loadSurvey.getProfileInterval()/60);
        }
        
        return profileData;
    } // public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException 
    
    private List buildChannelInfos(LoadSurveyData loadSurveyData) {
        List channelInfos = new ArrayList();
        for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++) {
        	ChannelInfo channelInfo = new ChannelInfo(channel,"EDMI MK10 channel "+channel,loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getUnit());    
        	channelInfo.setMultiplier(loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getScalingFactor());
        	channelInfos.add(channelInfo);        
        } // for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++)
        return channelInfos;
    } // private List buildChannelInfos(LoadSurveyData loadSurveyData) 
    
    private List buildIntervalDatas(LoadSurveyData loadSurveyData) throws IOException {
        List intervalDatas = new ArrayList();
        Calendar cal = ProtocolUtils.getCleanCalendar(mk10.getTimeZone());
        cal.setTime(loadSurveyData.getFirstTimeStamp());
        for (int interval=0; interval<loadSurveyData.getNumberOfRecords(); interval++) {
            IntervalData intervalData= new IntervalData(new Date(cal.getTime().getTime()));
            for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels() ; channel++) {
                if (channel == loadSurveyData.getLoadSurvey().getNrOfChannels()-1 ) {
                    int protocolStatus=loadSurveyData.getChannelValues(interval)[0].getBigDecimal().intValue();
                    int eiStatus=mapProtocolStatus2EiStatus(protocolStatus);
                    intervalData.setEiStatus(eiStatus);
                    intervalData.setProtocolStatus(protocolStatus);
                }
//                if (((channel==0) && (mk10.isStatusFlagChannel())) || (channel>0)) {
                   intervalData.addValue(loadSurveyData.getChannelValues(interval)[channel].getBigDecimal());
//                }
            } // for (int channel=1; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++)
            intervalDatas.add(intervalData);
            cal.add(Calendar.SECOND,loadSurveyData.getLoadSurvey().getProfileInterval());
        } // for (int interval=0; interval<loadSurveyData.getNumberOfRecords(); interval++)
        
        return intervalDatas;
        
    } // private List buildIntervalDatas(LoadSurveyData loadSurveyData) 

    private final int ERROR_READING_REGISTER=0x0001;
    private final int MISSING_DATA=0x0002; 
    private final int POWER_FAILED_DURING_INTERVAL=0x0004;
    private final int INCOMPLETE_INTERVAL=0x0008;
    private final int DST_IN_EFFECT=0x0010;
    private final int CALIBRATION_LOST=0x0020;
    private final int SVFRM_FAILURE=0x0040;
    private final int EFA_FAILURE_USER_FLAG=0x0080;
    private final int DATA_CHECKSUM_ERROR=0x0100;
            
    
    private int mapProtocolStatus2EiStatus(int protocolStatus) {
        int eiStatus=0;
        
        if ((protocolStatus & ERROR_READING_REGISTER) == ERROR_READING_REGISTER) {
            eiStatus |= IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & MISSING_DATA) == MISSING_DATA) {
            eiStatus |= IntervalStateBits.MISSING;
        }
        if ((protocolStatus & POWER_FAILED_DURING_INTERVAL) == POWER_FAILED_DURING_INTERVAL) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & INCOMPLETE_INTERVAL) == INCOMPLETE_INTERVAL) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & CALIBRATION_LOST) == CALIBRATION_LOST) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & SVFRM_FAILURE) == SVFRM_FAILURE) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & EFA_FAILURE_USER_FLAG) == EFA_FAILURE_USER_FLAG) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & DATA_CHECKSUM_ERROR) == DATA_CHECKSUM_ERROR) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        
        return eiStatus;
    }
    
    
    private List buildMeterEvents(LoadSurveyData eventLogData) throws IOException {
        List meterEvents = new ArrayList();
        for (int interval=0; interval<eventLogData.getNumberOfRecords(); interval++) {
            Date date = eventLogData.getChannelValues(interval)[1].getDate();
            String message = eventLogData.getChannelValues(interval)[2].getString();
            MeterEvent me = new MeterEvent(date,mapEventLogMessage2MeterEventEICode(message),message);
            meterEvents.add(me);
        } // for (int interval=0; interval<loadSurveyData.getNumberOfRecords(); interval++)
        return meterEvents;
    } // private List buildMeterEvents(LoadSurveyData eventLogData) 
 
    private int mapEventLogMessage2MeterEventEICode(String message) {
        
        if (message.indexOf("Power Off")>=0)
            return MeterEvent.POWERDOWN;
        if (message.indexOf("Power On")>=0)
            return MeterEvent.POWERUP;
        if (message.indexOf("Changing System Time")>=0)
            return MeterEvent.SETCLOCK_BEFORE;
        if (message.indexOf("System Time Changed")>=0)
            return MeterEvent.SETCLOCK_AFTER;
        
        return MeterEvent.OTHER;
    }
}
