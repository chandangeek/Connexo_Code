/*
 * LoadSurvey.java
 *
 * Created on 31 maart 2006, 14:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimplv2.edmi.mk10.profiles;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.core.DataType;
import com.energyict.protocolimpl.edmi.common.core.SurveyChannelTypeParser;
import com.energyict.protocolimpl.edmi.mk10.registermapping.MK10RegisterInformation;
import com.energyict.protocolimplv2.edmi.mk10.MK10;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author koen
 *         Changes:
 *         GNA |26022009| Added the extra scaler for instantaneous values
 */
public class LoadSurvey {

    private String meterSerialNumber;
    private TimeZone timeZone;
    private CommandFactory commandFactory;
    private boolean preventCrossingIntervalBoundaryWhenReading;
    private LoadProfileDescription loadProfileDescription;
    private int baseRegisterId;
    private int registerId;

    private int nrOfChannels;
    private LoadSurveyChannel[] loadSurveyChannels;
    private int profileInterval;

    private long storedEntries;
    private long firstEntry;
    private long lastEntry;
    private Date startTime;

    /**
     * Creates a new instance of LoadSurvey
     */
    public LoadSurvey(CommandLineProtocol protocol, LoadProfileDescription loadProfileDescription) throws ProtocolException {
        this.setMeterSerialNumber(protocol.getConfiguredSerialNumber());
        this.setTimeZone(protocol.getTimeZone());
        this.setCommandFactory(protocol.getCommandFactory());
        this.setPreventCrossingIntervalBoundaryWhenReading(((MK10) protocol).getProperties().preventCrossingIntervalBoundaryWhenReading());
        this.setLoadProfileDescription(loadProfileDescription);
        this.setBaseRegisterId(MK10RegisterInformation.SURVEY_BASE_REGISTER.getRegisterId());
        this.calculateRegisterId();
        this.initialize();
    }

    /**
     * Reads out all load survey parameters
     */
    private void initialize() throws ProtocolException {
        // Channels and Interval are in the same register (Base registerId + 6)
        // Number of channels -> Bit 0 to 5
        // Interval           -> Bit 6 to 11 (The interval is stored in minutes)
        int ChannelsIntervalRegister = getCommandFactory().getReadCommand(getRegisterId() + 6, DataType.I_SHORT).getRegister().getBigDecimal().intValue();
        setNrOfChannels((ChannelsIntervalRegister & 0x003F) + 1);   // Always 1 additional channel with the status! Number of load survey channels, excluding the 0 channel.
        setProfileInterval(((ChannelsIntervalRegister & 0x0FC0) >> 6) * 60);

        setFirstEntry(getCommandFactory().getReadCommand(getRegisterId() + 2, DataType.L_LONG).getRegister().getBigDecimal().longValue());
        setLastEntry(getCommandFactory().getReadCommand(getRegisterId() + 4, DataType.L_LONG).getRegister().getBigDecimal().longValue());
        setStoredEntries(getLastEntry() - getFirstEntry());
        setStartTime(getCommandFactory().getReadCommand(getRegisterId() + 0x00B0, DataType.T_TIME_DATE_SINCE__1_97).getRegister().getDate()); // The start time of the load survey

        setLoadSurveyChannels(new LoadSurveyChannel[getNrOfChannels()]);
        for (int channel = 0; channel < getNrOfChannels() - 1; channel++) {
            LoadSurveyChannel lsc = new LoadSurveyChannel();
            int tempReg = (getBaseRegisterId() + (getLoadProfileDescription().equals(LoadProfileDescription.REGULAR_PROFILE) ? 0x0040 : 0x0060) + channel);
            int ChannelDef = getCommandFactory().getReadCommand(tempReg, DataType.I_SHORT).getRegister().getBigDecimal().intValue();

            SurveyChannelTypeParser ctp = new SurveyChannelTypeParser(ChannelDef);
            lsc.setDecimalPointPositionScaling(ctp.getDecimalPointPosition());
            lsc.setUnit(ctp.getUnit());
            lsc.setWidth(2);
            lsc.setObisCode(ctp.getChannelObisCode());

            if (ctp.isInstantaneous()) {
                tempReg = (getBaseRegisterId() + (0x0008 + ctp.getInstantaneousType()));
                BigDecimal bdScalingFactor = getCommandFactory().getReadCommand(tempReg, DataType.F_FLOAT).getRegister().getBigDecimal();
                lsc.setScalingFactor(bdScalingFactor);
                lsc.markAsInstantaneousChannel();
            } else {
                lsc.setScalingFactor(ctp.getScalingFactor());
                lsc.setDecimalPointPositionScaling(ctp.getDecimalPointPosition());
            }
            getLoadSurveyChannels()[channel] = lsc;
        }

        //Last channel in load survey is status channel
        LoadSurveyChannel lsc = new LoadSurveyChannel();
        lsc.setDecimalPointPositionScaling(0);
        lsc.setScalingFactor(new BigDecimal(0));
        lsc.setUnit(Unit.getUndefined());
        lsc.setWidth(1);
        lsc.markAsStatusChannel();
        getLoadSurveyChannels()[getNrOfChannels() - 1] = lsc;
    }

    public LoadSurveyData readFile(Date from) throws ProtocolException {
        LoadSurveyData loadSurveyData = new LoadSurveyData(this);
        loadSurveyData.readFile(from);
        return loadSurveyData;
    }

    public boolean isEventLog() {
        return getProfileInterval() == 0;
    }

    public long getFirstEntry() {
        return firstEntry;
    }

    private void setFirstEntry(long firstEntry) {
        this.firstEntry = firstEntry;
    }

    public long getLastEntry() {
        return lastEntry;
    }

    private void setLastEntry(long lastEntry) {
        this.lastEntry = lastEntry;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    private void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    private void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
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

    public LoadProfileDescription getLoadProfileDescription() {
        return loadProfileDescription;
    }

    private void setLoadProfileDescription(LoadProfileDescription loadSurveyNumber) {
        loadProfileDescription = loadSurveyNumber;
    }

    public int getBaseRegisterId() {
        return baseRegisterId;
    }

    private void setBaseRegisterId(int baseRegisterId) {
        this.baseRegisterId = baseRegisterId;
    }

    public int getRegisterId() {
        return registerId;
    }

    private void calculateRegisterId() {
        this.registerId = getBaseRegisterId() + getLoadProfileDescription().getSurveyNr();
    }

    public int getNrOfChannels() {
        return nrOfChannels;
    }

    private void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }

    public LoadSurveyChannel[] getLoadSurveyChannels() {
        return loadSurveyChannels;
    }

    public List<ChannelInfo> getChannelInfos() {
        LoadSurveyChannel[] loadSurveyChannelsWithoutStatus = Arrays.copyOfRange(getLoadSurveyChannels(), 0, getLoadSurveyChannels().length - 1);
        List<ChannelInfo> channelInfos = new ArrayList<>(loadSurveyChannelsWithoutStatus.length);
        for (LoadSurveyChannel loadSurveyChannel : loadSurveyChannelsWithoutStatus) {
            ChannelInfo channelInfo = new ChannelInfo(
                    channelInfos.size(),
                    loadSurveyChannel.getObisCode().toString(),
                    loadSurveyChannel.getUnit(),
                    getMeterSerialNumber()
            );
            channelInfo.setMultiplier(loadSurveyChannel.getScalingFactor());
            channelInfos.add(channelInfo);
        }
        return channelInfos;
    }

    private void setLoadSurveyChannels(LoadSurveyChannel[] loadSurveyChannels) {
        this.loadSurveyChannels = loadSurveyChannels;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    private void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    public long getStoredEntries() {
        return storedEntries;
    }

    private void setStoredEntries(long storedEntries) {
        this.storedEntries = storedEntries;
    }

    public Date getStartTime() {
        return startTime;
    }

    private void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Getter for the total record size.
     *
     * @return
     */
    public int getRecordSize() {
        int size = 0;
        if (loadSurveyChannels != null) {
            for (int i = 0; i < loadSurveyChannels.length; i++) {
                LoadSurveyChannel loadSurveyChannel = loadSurveyChannels[i];
                size += loadSurveyChannel.getWidth();
            }
        }
        return size;
    }

    /**
     * Read the firstEntry from the device again
     * @return the first entry from the buffer
     */
    public long getUpdatedFirstEntry() {
        return getCommandFactory().getReadCommand(getRegisterId() + 2).getRegister().getBigDecimal().longValue();
    }
}