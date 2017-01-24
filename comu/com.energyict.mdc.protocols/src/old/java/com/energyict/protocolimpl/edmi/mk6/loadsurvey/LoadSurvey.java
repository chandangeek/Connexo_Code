/*
 * LoadSurvey.java
 *
 * Created on 31 maart 2006, 14:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.loadsurvey;

import com.energyict.protocolimpl.edmi.mk6.command.CommandFactory;
import com.energyict.protocolimpl.edmi.mk6.core.RegisterUnitParser;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
/**
 *
 * @author koen
 */
public class LoadSurvey implements Serializable {
    
    /** Generated SerailVersionUID */
	private static final long serialVersionUID = 1342735526534898542L;
	private CommandFactory commandFactory;
    private int registerId;
    
    private int nrOfChannels;
    private LoadSurveyChannel[] loadSurveyChannels;
    private int profileInterval;
    private int nrOfEntries;
    private int entryWidth;
    private long storedEntries;
    private Date startTime;
    
    /** Creates a new instance of LoadSurvey */
    public LoadSurvey(CommandFactory commandFactory, int registerId) throws IOException {
        this.setCommandFactory(commandFactory);
        this.setRegisterId(registerId);
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
        setNrOfChannels(getCommandFactory().getReadCommand((registerId<<16)|0x5F012).getRegister().getBigDecimal().intValue()+1); // steeds 1 extra channel with the status! Number of load survey channels, excluding the 0 channel.
        setProfileInterval(getCommandFactory().getReadCommand((registerId<<16)|0x5F014).getRegister().getBigDecimal().intValue()); // Seconds between readings, for fixed interval load surveys.
        setNrOfEntries(getCommandFactory().getReadCommand((registerId<<16)|0x5F013).getRegister().getBigDecimal().intValue()); // Max nr of entries in the load survey
        setEntryWidth(getCommandFactory().getReadCommand((registerId<<16)|0x5F018).getRegister().getBigDecimal().intValue()); // The total entry width (including checksum/status word). This is the sum of the channel widths plus 2.
        setLoadSurveyChannels(new LoadSurveyChannel[getNrOfChannels()]);
        storedEntries = getCommandFactory().getReadCommand((registerId<<16)|0x5F021).getRegister().getBigDecimal().intValue();  // Holds the number of entries in the load survey. This is
                                                                                                                                //stored as a long, and MOD can be used with number of
                                                                                                                                //entries to find the current pointer into the load survey. This
                                                                                                                                //gives a continuous register number, useful when reading non
                                                                                                                                //fixed interval load surveys. If the number is bigger than
                                                                                                                                //number of entries the load survey is full and is wrapping.
        startTime =  getCommandFactory().getReadCommand((registerId<<16)|0x5F020).getRegister().getDate(); // The first time that was stored in the survey ever.
       
        for (int channel = 0; channel <  getLoadSurveyChannels().length; channel++) {
            LoadSurveyChannel lsc = new LoadSurveyChannel();
            lsc.setName(getCommandFactory().getReadCommand((registerId<<16)|0x5E400|channel).getRegister().getString());
            lsc.setOffset(getCommandFactory().getReadCommand((registerId<<16)|0x5E500|channel).getRegister().getBigDecimal().intValue());
            lsc.setRegister(getCommandFactory().getReadCommand((registerId<<16)|0x5E000|channel).getRegister().getBigDecimal().intValue());
            lsc.setScaling(getCommandFactory().getReadCommand((registerId<<16)|0x5E600|channel).getRegister().getBigDecimal().intValue());
            lsc.setScalingFactor(getCommandFactory().getReadCommand((registerId<<16)|0x5E800|channel).getRegister().getBigDecimal());
            lsc.setType(getCommandFactory().getReadCommand((registerId<<16)|0x5E200|channel).getRegister().getBigDecimal().intValue());
            RegisterUnitParser rup = new RegisterUnitParser();
            lsc.setUnit(rup.parse((char)getCommandFactory().getReadCommand((registerId<<16)|0x5E300|channel).getRegister().getBigDecimal().intValue()));
            lsc.setWidth(getCommandFactory().getReadCommand((registerId<<16)|0x5E100|channel).getRegister().getBigDecimal().intValue());
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

    public int getRegisterId() {
        return registerId;
    }

    private void setRegisterId(int registerId) {
        this.registerId = registerId;
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

    /**
     * Read the storedEntries from the device again
     * @return the number of the stored Entries
     * @throws IOException
     */
    public long getUpdatedStoredEntries() throws IOException{
    	return getCommandFactory().getReadCommand((registerId<<16)|0x5F021).getRegister().getBigDecimal().intValue();
    }
    
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

}
