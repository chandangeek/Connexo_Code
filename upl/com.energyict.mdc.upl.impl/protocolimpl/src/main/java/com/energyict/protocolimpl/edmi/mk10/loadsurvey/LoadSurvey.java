/*
 * LoadSurvey.java
 *
 * Created on 31 maart 2006, 14:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.loadsurvey;

import com.energyict.protocolimpl.edmi.mk10.core.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.protocolimpl.edmi.mk10.command.*;
/**
 *
 * @author koen
 */
public class LoadSurvey {
    
    private static final int BASE_REGISTER_ID = 0xD800;

    private CommandFactory commandFactory;
    private int LoadSurveyNumber;
    private int registerId;
    private int nrOfChannels;
    private LoadSurveyChannel[] loadSurveyChannels;
    private int profileInterval;
    private int nrOfEntries;
    private int entryWidth;
    private long storedEntries;
    private Date startTime;
    
    /** Creates a new instance of LoadSurvey */
    public LoadSurvey(CommandFactory commandFactory, int LoadSurveyNumber) throws IOException {
        this.setCommandFactory(commandFactory);
        this.setLoadSurveyNumber(LoadSurveyNumber);
        this.genRegisterId();
        init();
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadSurvey:\n");
        strBuff.append("registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("nrOfChannels="+nrOfChannels+"\n");
        strBuff.append("profileInterval="+getProfileInterval()+"\n");
        strBuff.append("nrOfEntries="+getNrOfEntries()+"\n");
        strBuff.append("entryWidth="+getEntryWidth()+"\n");
        strBuff.append("storedEntries="+getStoredEntries()+"\n");
        strBuff.append("startTime="+getStartTime()+"\n");
        
        for (int channel = 0; channel <  getLoadSurveyChannels().length; channel++) {
            strBuff.append("channel "+channel+": "+getLoadSurveyChannels()[channel]);
        }
        return strBuff.toString();
    }
    
    public boolean isEventLog() {
        return getProfileInterval()==0;
    }  
    
    private void init() throws IOException {
        // Channels and Interval are in the same register (Base registerId + 6)
    	// Number of channels -> Bit 0 to 5
    	// Interval           -> Bit 6 to 11 (The interval is stored in minutes)
    	int ChannelsIntervalRegister = getCommandFactory().getReadCommand(registerId + 6).getRegister().getBigDecimal().intValue();
    	int channels = (ChannelsIntervalRegister & 0x001F);
    	int interval = ((ChannelsIntervalRegister & 0x0FC0) >> 6) * 60;
    	
        long firstentry = getCommandFactory().getReadCommand(registerId + 2).getRegister().getBigDecimal().longValue();
        long lastentry = getCommandFactory().getReadCommand(registerId + 3).getRegister().getBigDecimal().longValue();

    	setNrOfChannels(channels + 1); 	// Always 1 additional channel with the status! Number of load survey channels, excluding the 0 channel.
        setProfileInterval(interval); 	// Seconds between readings, for fixed interval load surveys.        
        setStoredEntries(lastentry-firstentry);
        setLoadSurveyChannels(new LoadSurveyChannel[getNrOfChannels()]);
        setStartTime(getCommandFactory().getReadCommand(registerId).getRegister().getDate()); // The first time that was stored in the survey ever.

        setNrOfEntries(0xFFFF); // Max nr of entries in the load survey. The MK10 meter only supports 32 entries per channel.
        setEntryWidth(3); // The total entry width (including checksum/status word). This is the sum of the channel widths plus 2.        
       

        for (int channel = 0; channel <  getLoadSurveyChannels().length; channel++) {
            LoadSurveyChannel lsc = new LoadSurveyChannel();
            int ChannelDef = getCommandFactory().
									  getReadCommand((BASE_REGISTER_ID + channel + (0x0020 * getLoadSurveyNumber()))).
									  getRegister().
									  getBigDecimal().
									  intValue(); 

        	ChannelTypeParser ctp = new ChannelTypeParser(ChannelDef);

        	
            lsc.setName("CH" + String.valueOf(channel));
            lsc.setScaling(ctp.getDecimalPointScaling());
            lsc.setScalingFactor(ctp.getScalingFactor());
            lsc.setType(ctp.getType());
            lsc.setUnit(ctp.getUnit());

            //lsc.setWidth(getCommandFactory().getReadCommand((registerId<<16)|0x5E100|channel).getRegister().getBigDecimal().intValue());
            getLoadSurveyChannels()[channel]=lsc;
        }

        
    }
    
    public LoadSurveyData readFile(Date from) throws IOException {
        LoadSurveyData lsd = new LoadSurveyData(this,from);
        return lsd;
    }
    
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public int getLoadSurveyNumber() {
		return LoadSurveyNumber;
	}

	public void setLoadSurveyNumber(int loadSurveyNumber) {
		LoadSurveyNumber = loadSurveyNumber;
	}

	public int getRegisterId() {
        return registerId;
    }

    private void genRegisterId() {
        this.registerId = BASE_REGISTER_ID + getLoadSurveyNumber();
    }

    public int getNrOfChannels() {
        return nrOfChannels;
    }

    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }

    public LoadSurveyChannel[] getLoadSurveyChannels() {
        return loadSurveyChannels;
    }

    public void setLoadSurveyChannels(LoadSurveyChannel[] loadSurveyChannels) {
        this.loadSurveyChannels = loadSurveyChannels;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    public int getNrOfEntries() {
        return nrOfEntries;
    }

    public void setNrOfEntries(int nrOfEntries) {
        this.nrOfEntries = nrOfEntries;
    }

    public int getEntryWidth() {
        return entryWidth;
    }

    public void setEntryWidth(int entryWidth) {
        this.entryWidth = entryWidth;
    }

    public long getStoredEntries() {
        return storedEntries;
    }

    public void setStoredEntries(long storedEntries) {
        this.storedEntries = storedEntries;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    
    
    
    
}
