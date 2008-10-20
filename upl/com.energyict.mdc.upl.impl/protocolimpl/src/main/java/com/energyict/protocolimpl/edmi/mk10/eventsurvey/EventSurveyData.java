/*
 * LoadSurveyData.java
 *
 * Created on 3 april 2006, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.eventsurvey;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.edmi.mk10.core.*;
import java.io.*;
import java.math.*;
import java.util.*;

import com.energyict.protocolimpl.edmi.mk10.command.*;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SetSourceId;

/**
 *
 * @author koen
 */
public class EventSurveyData {
    
    private final int DEBUG=1;
    private final int READBUFFER = 0xFF;
    
    private EventSurvey loadSurvey;
    
    private byte[] data;
    private Date firstTimeStamp;
    private int numberOfRecords;
    
    /** Creates a new instance of LoadSurveyData */
    public EventSurveyData(EventSurvey loadSurvey,Date from) throws IOException {
        this.setLoadSurvey(loadSurvey);
        init(from);
    }
    
    
    private void init(Date from) throws IOException {
        FileAccessReadCommand farc;
        long startRecord;

        long first = getLoadSurvey().getFirstEntry();
        Date firstdate = getLoadSurvey().getStartTime();
        long interval = getLoadSurvey().getProfileInterval();
        long seconds_div = (from.getTime() - firstdate.getTime()) / 1000;
        int records = 0;
        
        if (DEBUG>=1) System.out.println("KV_DEBUG> LoadSurveyData, init() getLoadSurvey()="+getLoadSurvey());
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.reset();
        
        if (DEBUG>=1) loadSurvey.getCommandFactory().getMk10().sendDebug("From date:             " + from.toGMTString());
        if (DEBUG>=1) loadSurvey.getCommandFactory().getMk10().sendDebug("Survey start date:     " + firstdate.toGMTString());
        if (DEBUG>=1) loadSurvey.getCommandFactory().getMk10().sendDebug("Survey first entry:    " + first);
        if (DEBUG>=1) loadSurvey.getCommandFactory().getMk10().sendDebug("Survey entry interval: " + interval);
        if (DEBUG>=1) loadSurvey.getCommandFactory().getMk10().sendDebug("seconds_div:           " + seconds_div);

        if (seconds_div < 0) startRecord = first;
        	else startRecord = first + (seconds_div / interval) + 1;
        
        farc = getLoadSurvey().getCommandFactory().getFileAccessReadCommand(getLoadSurvey().getLoadSurveyNumber(), startRecord, 0x0001);
        startRecord = farc.getStartRecord();
               
        do {
            farc = getLoadSurvey().getCommandFactory().getFileAccessReadCommand(getLoadSurvey().getLoadSurveyNumber(), startRecord, READBUFFER);
            startRecord += farc.getNumberOfRecords();
            records += farc.getNumberOfRecords();
            byteArrayOutputStream.write(farc.getData(),0,farc.getData().length);
            if (DEBUG>=1) getLoadSurvey().getCommandFactory().getMk10().sendDebug(farc.toString());        	
        } while((getLoadSurvey().getStoredEntries()  - (farc.getStartRecord() + farc.getNumberOfRecords())) > 0);
        
        setNumberOfRecords(records);
        setData(byteArrayOutputStream.toByteArray());

        Calendar cal = ProtocolUtils.getCleanCalendar(getLoadSurvey().getCommandFactory().getMk10().getTimeZone());
        cal.setTime(getLoadSurvey().getStartTime());
        cal.add(Calendar.SECOND,(int)(((getLoadSurvey().getStoredEntries() - 1) - getNumberOfRecords()) * interval));
        setFirstTimeStamp(cal.getTime());

    } // private void init(Date from) throws IOException

    public String toString() {
        try {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("LoadSurveyData:\n");
            strBuff.append("    file search:\n");
            strBuff.append("        first record TimeStamp="+getFirstTimeStamp()+"\n");
            strBuff.append("    file read:\n");
            strBuff.append("        actual nr of records read:"+getNumberOfRecords()+"\n");
            for (int interval=0;interval<getNumberOfRecords();interval++) {
               strBuff.append("        interval "+interval+": ");
               for (int channel=0;channel<loadSurvey.getNrOfChannels();channel++) {
                   if (getLoadSurvey().isEventLog())
                      strBuff.append(getChannelValues(interval)[channel].getString()+" ");   
                   else
                      strBuff.append(getChannelValues(interval)[channel].getBigDecimal().multiply(loadSurvey.getLoadSurveyChannels()[channel].getScalingFactor())+"("+loadSurvey.getLoadSurveyChannels()[channel].getUnit()+") ");   
               }
               strBuff.append("\n");
            }
            return strBuff.toString();
        }
        catch(IOException e) {
            return e.toString();
        }
        
    } // public String toString()
    
    // loadSurvey.getProfileInterval() == 0 --> event log!, timestamp as separate field
    
    public int getStatus(int intervalIndex) throws IOException {
        int offset = intervalIndex * loadSurvey.getEntryWidth();
        return ProtocolUtils.getInt(getData(),offset,2);
    }
    
    private byte[] getData(int intervalIndex, int channelIndex) throws IOException {
    	int offset = (intervalIndex * getLoadSurvey().getEntryWidth()) + (channelIndex * 2);
        return ProtocolUtils.getSubArray2(getData(), offset, getLoadSurvey().getLoadSurveyChannels()[channelIndex].getWidth());
    }
    
    public AbstractRegisterType[] getChannelValues(int intervalIndex) throws IOException {
        AbstractRegisterType[] channelValues = new AbstractRegisterType[loadSurvey.getNrOfChannels()];
        RegisterTypeParser rtp = new RegisterTypeParser(loadSurvey.getCommandFactory().getMk10().getTimeZone());
        AbstractRegisterType channelValue;
        for (int channel=0;channel<loadSurvey.getNrOfChannels();channel++) {
            char chan_type = (char)loadSurvey.getLoadSurveyChannels()[channel].getType();
        	int chan_scaler = loadSurvey.getLoadSurveyChannels()[channel].getScaling();
            
            if (channel == (loadSurvey.getNrOfChannels() - 1)) channelValue = rtp.parse2Internal('C', getData(intervalIndex, channel));
            	else channelValue = rtp.parseFromRaw(chan_type, getData(intervalIndex, channel), chan_scaler);
        	channelValues[channel] = channelValue;
        }
        return channelValues;
    }
    
    public EventSurvey getLoadSurvey() {
        return loadSurvey;
    }

    public void setLoadSurvey(EventSurvey loadSurvey) {
        this.loadSurvey = loadSurvey;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Date getFirstTimeStamp() {
        return firstTimeStamp;
    }

    public void setFirstTimeStamp(Date firstTimeStamp) {
        this.firstTimeStamp = firstTimeStamp;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }
}
