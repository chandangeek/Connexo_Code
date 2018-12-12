/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.ReadCommand;
import com.energyict.protocolimpl.edmi.common.core.RegisterUnitParser;
import com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation;
import com.energyict.protocolimplv2.edmi.mk6.MK6;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author koen
 */
public class LoadSurvey implements Serializable {


    private CommandFactory commandFactory;
    private boolean preventCrossingIntervalBoundaryWhenReading;
    private int registerId;

    private int nrOfChannels;
    private LoadSurveyChannel[] loadSurveyChannels;
    private int profileInterval;
    private int nrOfEntries;
    private int entryWidth;
    private long storedEntries;
    private Date startTime;

    public LoadSurvey(CommandFactory commandFactory, int registerId) {
        this.setCommandFactory(commandFactory);
        this.setPreventCrossingIntervalBoundaryWhenReading(((MK6) commandFactory.getProtocol()).getProperties().preventCrossingIntervalBoundaryWhenReading());
        this.setRegisterId(registerId);
        init();
    }

    public boolean isEventLog() {
        return getProfileInterval()==0;
    }

    private void init() {
        setNrOfChannels(getReadCommand(registerId, MK6RegisterInformation.LOAD_SURVEY_NUMBER_OF_CHANNELS).getRegister().getBigDecimal().intValue()+1); // Always 1 extra channel with the status! Number of load survey channels, excluding the 0 channel.
        setProfileInterval(getReadCommand(registerId, MK6RegisterInformation.LOAD_SURVEY_INTERVAL_IN_SECONDS).getRegister().getBigDecimal().intValue()); // Seconds between readings, for fixed interval load surveys.
        setNrOfEntries(getReadCommand(registerId, MK6RegisterInformation.LOAD_SURVEY_NUMBER_OF_ENTRIES).getRegister().getBigDecimal().intValue()); // Max nr of entries in the load survey
        setEntryWidth(getReadCommand(registerId, MK6RegisterInformation.LOAD_SURVEY_LOAD_SURVEY_ENTRY_WIDTH).getRegister().getBigDecimal().intValue()); // The total entry width (including checksum/status word). This is the sum of the channel widths plus 2.
        setLoadSurveyChannels(new LoadSurveyChannel[getNrOfChannels()]);
        storedEntries = getReadCommand(registerId, MK6RegisterInformation.LOAD_SURVEY_NUMBER_OF_STORED_ENTRIES).getRegister().getBigDecimal().intValue();  // Holds the number of entries in the load survey. This is
                                                                                                                                //stored as a long, and MOD can be used with number of
                                                                                                                                //entries to find the current pointer into the load survey. This
                                                                                                                                //gives a continuous register number, useful when reading non
                                                                                                                                //fixed interval load surveys. If the number is bigger than
                                                                                                                                //number of entries the load survey is full and is wrapping.
        startTime =  getReadCommand(registerId, MK6RegisterInformation.LOAD_SURVEY_START_TIME).getRegister().getDate(); // The first time that was stored in the survey ever.

        for (int channel = 0; channel <  getLoadSurveyChannels().length; channel++) {
            LoadSurveyChannel lsc = new LoadSurveyChannel();
            lsc.setName(getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_NAME).getRegister().getString());
            lsc.setOffset(getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_RECORD_OFFSET).getRegister().getBigDecimal().intValue());
            lsc.setRegister(getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_REGISTER_ID).getRegister().getBigDecimal().intValue());
            lsc.setScalingFactor(getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_SCALING_FACTOR).getRegister().getBigDecimal());
            lsc.setType(getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_TYPE).getRegister().getBigDecimal().intValue());
            lsc.setUnit(RegisterUnitParser.parse((char)getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_UNIT).getRegister().getBigDecimal().intValue()));
            lsc.setWidth(getReadCommand(channel, registerId, MK6RegisterInformation.LOAD_SURVEY_CHANNEL_SIZE).getRegister().getBigDecimal().intValue());
            getLoadSurveyChannels()[channel]=lsc;
        }
    }

    public ReadCommand getReadCommand(int registerId, MK6RegisterInformation registerInformation) {
        return getCommandFactory().getReadCommand(registerId<<16| registerInformation.getRegisterId(), registerInformation.getDataType());
    }

    public ReadCommand getReadCommand(int channelNr, int registerId, MK6RegisterInformation registerInformation) {
        return getCommandFactory().getReadCommand(registerId<<16| registerInformation.getRegisterId() | channelNr, registerInformation.getDataType());
    }

    public LoadSurveyData readFile(Date from) throws ProtocolException {
        LoadSurveyData loadSurveyData = new LoadSurveyData(this);
        loadSurveyData.readFile(from);
        return loadSurveyData;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public boolean preventCrossingIntervalBoundaryWhenReading() {
        return preventCrossingIntervalBoundaryWhenReading;
    }

    private void setPreventCrossingIntervalBoundaryWhenReading(boolean preventCrossingIntervalBoundaryWhenReading) {
        this.preventCrossingIntervalBoundaryWhenReading = preventCrossingIntervalBoundaryWhenReading;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}