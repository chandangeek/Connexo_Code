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

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.edmi.mk10.command.CommandFactory;
import com.energyict.protocolimpl.edmi.mk10.core.SurveyChannelTypeParser;
import com.energyict.protocolimpl.edmi.mk10.registermapping.MK10Register;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author koen
 * Changes:
 * GNA |26022009| Added the extra scaler for instantaneous values
 */
public class LoadSurvey {

	private static final String	LF	= "\n";
	private static final int BASE_REGISTER_ID = MK10Register.SURVEY1_STARTDATE;
	private static final int DEBUG = 0;

	private CommandFactory commandFactory;
	private int LoadSurveyNumber;
	private int registerId;
	private int nrOfChannels;
	private LoadSurveyChannel[] loadSurveyChannels;
	private int profileInterval;
	private int nrOfEntries;
	private int entryWidth;
	private long storedEntries;
	private long firstEntry;
	private long lastEntry;
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
		strBuff.append("LoadSurvey:" + LF);
		strBuff.append("  registerId=0x" + Integer.toHexString(getRegisterId()) + LF);
		strBuff.append("  nrOfChannels=" + nrOfChannels + LF);
		strBuff.append("  profileInterval=" + getProfileInterval() + LF);
		strBuff.append("  nrOfEntries=" + getNrOfEntries() + LF);
		strBuff.append("  firstEntry=" + getFirstEntry() + LF);
		strBuff.append("  lastEntry=" + getLastEntry() + LF);
		strBuff.append("  entryWidth=" + getEntryWidth() + LF);
		strBuff.append("  storedEntries=" + getStoredEntries() + LF);
		strBuff.append("  startTime=" + getStartTime() + LF);
		for (int channel = 0; channel < getLoadSurveyChannels().length; channel++) {
			strBuff.append("channel " + channel + ": " + getLoadSurveyChannels()[channel]);
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

		setFirstEntry(getCommandFactory().getReadCommand(registerId + 2).getRegister().getBigDecimal().longValue());
		setLastEntry(getCommandFactory().getReadCommand(registerId + 4).getRegister().getBigDecimal().longValue());
		setStoredEntries(getLastEntry() - getFirstEntry());

		setNrOfChannels(channels + 1); 					// Always 1 additional channel with the status! Number of load survey channels, excluding the 0 channel.
		setProfileInterval(interval); 					// Seconds between readings, for fixed interval load surveys.
		setLoadSurveyChannels(new LoadSurveyChannel[getNrOfChannels()]);
		setStartTime(getCommandFactory().getReadCommand(registerId + 0x00B0).getRegister().getDate()); // The first time that was stored in the survey ever.

		setNrOfEntries(0xFFFF); 						// Max nr of entries in the load survey. The MK10 meter only supports 32 entries per channel.
		setEntryWidth((getNrOfChannels() * 2) - 1); 	// The total entry width (including status word). This is the sum of the channel widths minus 1 for the status width.

		for (int channel = 0; channel <  getLoadSurveyChannels().length; channel++) {
			LoadSurveyChannel lsc = new LoadSurveyChannel();

			if ((channel+1) == nrOfChannels) {
				lsc.setName("Status channel"); //Last channel in loadsurvey is statuschannel.
				lsc.setScaling(0);
				lsc.setScalingFactor(new BigDecimal(0));
				lsc.setUnit(Unit.get(""));
				lsc.setType('C');
				lsc.setWidth(1);
			}
			else {
				int tempreg = (BASE_REGISTER_ID + 0x0040 + channel + (0x0020 * getLoadSurveyNumber()));
				int ChannelDef = getCommandFactory().getReadCommand(tempreg).getRegister().getBigDecimal().intValue();
				String registeridstr = "0x" + ProtocolUtils.buildStringHex(tempreg, 4);
				if (DEBUG == 1) {
					this.commandFactory.getMk10().sendDebug("Channel " + String.valueOf(channel) + " RegisterID: " + registeridstr + " Value: 0x" + ProtocolUtils.buildStringHex(ChannelDef, 4));
				}

				SurveyChannelTypeParser ctp = new SurveyChannelTypeParser(ChannelDef);

				lsc.setName(ctp.getName());
				lsc.setType(ctp.getType());
				lsc.setScaling(ctp.getDecimalPointScaling());
				lsc.setUnit(ctp.getUnit());
				lsc.setWidth(2);

				if(ctp.isInstantaneous()){
					tempreg = (BASE_REGISTER_ID + (0x0008 + ctp.getInstantaneousType()));
					BigDecimal bdScalingFactor = getCommandFactory().getReadCommand(tempreg).getRegister().getBigDecimal();
					lsc.setScalingFactor(bdScalingFactor);
				} else {
					lsc.setScalingFactor(ctp.getScalingFactor());
				}

			}
			getLoadSurveyChannels()[channel]=lsc;
		}
	}

	public LoadSurveyData readFile(Date from) throws IOException {
		LoadSurveyData lsd = new LoadSurveyData(this,from);
		return lsd;
	}

	public long getFirstEntry() {
		return firstEntry;
	}

	public void setFirstEntry(long firstEntry) {
		this.firstEntry = firstEntry;
	}

	public long getLastEntry() {
		return lastEntry;
	}

	public void setLastEntry(long lastEntry) {
		this.lastEntry = lastEntry;
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

    /**
     * Getter for the total record size.
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
     * Read the storedEntries from the device again
     * @return the number of the stored Entries
     * @throws IOException
     */
	public long getUpdatedStoredEntries() throws IOException {
		return getUpdatedLastEntry() - getUpdatedFirstEntry();
	}
	
	/**
	 * Read the firstEntry from the device again
	 * @return the first entry from the buffer
	 * @throws IOException
	 */
	public long getUpdatedFirstEntry() throws IOException {
		return getCommandFactory().getReadCommand(registerId + 2).getRegister().getBigDecimal().longValue();
	}
	
	/**
	 * Read the lastEntry from the device again
	 * @return the last entry from the buffer
	 * @throws IOException
	 */
	public long getUpdatedLastEntry() throws IOException {
		return getCommandFactory().getReadCommand(registerId + 4).getRegister().getBigDecimal().longValue();
	}

}
