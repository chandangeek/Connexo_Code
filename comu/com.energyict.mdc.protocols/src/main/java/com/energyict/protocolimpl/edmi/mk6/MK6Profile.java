/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MK6ProfileData.java
 *
 * Created on 4 april 2006, 14:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.edmi.mk6.loadsurvey.ExtensionFactory;
import com.energyict.protocolimpl.edmi.mk6.loadsurvey.LoadSurvey;
import com.energyict.protocolimpl.edmi.mk6.loadsurvey.LoadSurveyData;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author koen
 */
public class MK6Profile implements Serializable{

	/** Generated SerialVersionUID */
	private static final long serialVersionUID = -249157060352419036L;
	private final int DEBUG=0;
	private LoadSurveyData loadSurveyData;
    private MK6 mk6;
    private LoadSurvey loadSurvey=null;
    private LoadSurvey eventLog=null;

    private ExtensionFactory extensionFactory=null;

    /** Creates a new instance of MK6ProfileData */
    public MK6Profile(MK6 mk6) {
        this.mk6=mk6;
    }

    private ExtensionFactory getExtensionFactory() throws IOException {
        if (extensionFactory == null) {
            extensionFactory = new ExtensionFactory(mk6.getCommandFactory());
        }
        return extensionFactory;
    }

    private LoadSurvey getLoadSurvey() throws IOException {
        if (loadSurvey==null) {
            loadSurvey = getExtensionFactory().findLoadSurvey(mk6.getLoadSurveyName());
        }
        return loadSurvey;
   }

    private LoadSurvey getEventLog() throws IOException {
        if (eventLog==null) {
            eventLog = getExtensionFactory().findLoadSurvey(mk6.getEventLogName());
        }
        return eventLog;
    }

    public int getProfileInterval() throws IOException {
        return getLoadSurvey().getProfileInterval();
    }

    public int getNumberOfChannels() throws IOException {
        return mk6.isStatusFlagChannel()?getLoadSurvey().getNrOfChannels():getLoadSurvey().getNrOfChannels()-1;
    }


    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData=new ProfileData();

        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> "+getLoadSurvey());
		}
        LoadSurveyData loadSurveyData = getLoadSurveyData(from);
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> "+loadSurveyData);
		}

        profileData.setChannelInfos(buildChannelInfos(loadSurveyData));
        profileData.setIntervalDatas(buildIntervalDatas(loadSurveyData));

        if (includeEvents) {
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> "+getEventLog());
			}
            LoadSurveyData eventLogData = getEventLog().readFile(from);
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> "+eventLogData);
			}
            profileData.setMeterEvents(buildMeterEvents(eventLogData));
            profileData.applyEvents(loadSurvey.getProfileInterval()/60);
        }

        return profileData;
    } // public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException

    /**
     * Get the {@link LoadSurveyData} if it's not available yet, then read if from the device
     * @param from - the date to start reading from
     * @return the LoadSurveyData
     * @throws IOException if something goes wrong during the read
     */
    protected LoadSurveyData getLoadSurveyData(Date from)throws IOException{
    	if(this.loadSurveyData == null){
    		this.loadSurveyData = getLoadSurvey().readFile(from);
    	}
    	return this.loadSurveyData;
    }

    /**
     * Setter for the LoadSurveyData (mainly for testing purposes)
     * @param loadSurveyData - the {@link LoadSurveyData} to set
     */
    protected void setLoadSurveydata(LoadSurveyData loadSurveyData){
    	this.loadSurveyData = loadSurveyData;
    }

    private List buildChannelInfos(LoadSurveyData loadSurveyData) {
        List channelInfos = new ArrayList();
        if (mk6.isStatusFlagChannel()) {
            for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++) {
               ChannelInfo channelInfo = new ChannelInfo(channel,"EDMI MK6 channel "+channel,loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getUnit());
               channelInfo.setMultiplier(loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getScalingFactor());
               channelInfos.add(channelInfo);
            } // for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++)
        }
        else {
            for (int channel=1; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++) {
               ChannelInfo channelInfo = new ChannelInfo(channel-1,"EDMI MK6 channel "+(channel-1),loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getUnit());
               channelInfo.setMultiplier(loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getScalingFactor());
               channelInfos.add(channelInfo);
            } // for (int channel=1; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++)
        }
        return channelInfos;
    } // private List buildChannelInfos(LoadSurveyData loadSurveyData)

    private List buildIntervalDatas(LoadSurveyData loadSurveyData) throws IOException {
        List intervalDatas = new ArrayList();
        Calendar cal = ProtocolUtils.getCleanCalendar(mk6.getTimeZone());
        cal.setTime(loadSurveyData.getFirstTimeStamp());
        for (int interval=0; interval<loadSurveyData.getNumberOfRecords(); interval++) {
            IntervalData intervalData= new IntervalData(new Date(cal.getTime().getTime()));
            for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++) {
                if (channel==0) {
                    int protocolStatus=loadSurveyData.getChannelValues(interval)[0].getBigDecimal().intValue();
                    int eiStatus=mapProtocolStatus2EiStatus(protocolStatus);
                    intervalData.setEiStatus(eiStatus);
                    intervalData.setProtocolStatus(protocolStatus);
                }
                if (((channel==0) && (mk6.isStatusFlagChannel())) || (channel>0)) {
                   intervalData.addValue(loadSurveyData.getChannelValues(interval)[channel].getBigDecimal());
                }
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

        if (message.indexOf("Power Off")>=0) {
			return MeterEvent.POWERDOWN;
		}
        if (message.indexOf("Power On")>=0) {
			return MeterEvent.POWERUP;
		}
        if (message.indexOf("Changing System Time")>=0) {
			return MeterEvent.SETCLOCK_BEFORE;
		}
        if (message.indexOf("System Time Changed")>=0) {
			return MeterEvent.SETCLOCK_AFTER;
		}

        return MeterEvent.OTHER;
    }
}
