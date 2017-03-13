/*
 * LoadSurveyData.java
 *
 * Created on 3 april 2006, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.loadsurvey;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.common.command.Atlas1FileAccessReadCommand;
import com.energyict.protocolimpl.edmi.common.core.AbstractRegisterType;
import com.energyict.protocolimpl.edmi.common.core.DataType;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeFloat;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeParser;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author koen
 */
public class LoadSurveyData {

    private static final int MAX_PACKET_SIZE = 1024;
    private static final int DEFAULT_MAX_ENTRIES = 0x10;
    private static final int HEADER_OVERHEAD = 30; //Raw assumption

	private LoadSurvey loadSurvey;

	private byte[] data;
	private Date firstTimeStamp;
	private int numberOfRecords;
    private int maxNrOfEntries = -1;

    /** Creates a new instance of LoadSurveyData */
	public LoadSurveyData(LoadSurvey loadSurvey,Date from) throws IOException {
		this.setLoadSurvey(loadSurvey);
		init(from);
	}


	private void init(Date from) throws IOException {

        long beforeUpdatedFirstEntry = 0;
        long afterUpdatedFirstEntry = 0;
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        int records = 0;
        
		Atlas1FileAccessReadCommand farc;
		long startRecord;

		long interval = getLoadSurvey().getProfileInterval();
		long first = getLoadSurvey().getFirstEntry();
		Date loadSurveyStartDate = getLoadSurvey().getStartTime();
		Date firstdate = new Date();
		
		do{
            records = 0;
			
			byteArrayOutputStream = new ByteArrayOutputStream();
	        byteArrayOutputStream.reset();
	        first = getLoadSurvey().getUpdatedFirstEntry();
			firstdate = new Date(loadSurveyStartDate.getTime() + (first * (interval * 1000)));
			
			long seconds_div = (from.getTime() - firstdate.getTime()) / 1000;
			
			if (seconds_div < 0) {
				startRecord = first;
			} else {
				startRecord = first + (seconds_div / interval) + 1;
			}
			
			beforeUpdatedFirstEntry = getLoadSurvey().getUpdatedFirstEntry();
			
			farc = getLoadSurvey().getCommandFactory().getAtlas1FileAccessReadCommand(getLoadSurvey().getLoadSurveyNumber(), startRecord, 0x0001);
			startRecord = farc.getStartRecord();

			do {
				farc = getLoadSurvey().getCommandFactory().getAtlas1FileAccessReadCommand(getLoadSurvey().getLoadSurveyNumber(), startRecord, getMaximumEntries());
				startRecord += farc.getNumberOfRecords();
				records += farc.getNumberOfRecords();
				byteArrayOutputStream.write(farc.getData(),0,farc.getData().length);
			} while((getLoadSurvey().getLastEntry()  - (farc.getStartRecord() + farc.getNumberOfRecords())) > 0);

			afterUpdatedFirstEntry = getLoadSurvey().getUpdatedFirstEntry();
			
		}while(beforeUpdatedFirstEntry != afterUpdatedFirstEntry);

		setNumberOfRecords(records);
		setData(byteArrayOutputStream.toByteArray());

		Calendar cal = ProtocolUtils.getCleanCalendar(getLoadSurvey().getCommandFactory().getProtocol().getTimeZone());
		cal.setTime(firstdate);
		cal.add(Calendar.SECOND,(int)((getLoadSurvey().getStoredEntries() - (getNumberOfRecords() - 1)) * interval));
		setFirstTimeStamp(cal.getTime());

	} // private void init(Date from) throws IOException

    private int getMaximumEntries() {
        if (maxNrOfEntries == -1) {
            if (getLoadSurvey().getEntryWidth() == 0) {
                this.maxNrOfEntries = DEFAULT_MAX_ENTRIES;
            } else {
                this.maxNrOfEntries = (MAX_PACKET_SIZE - HEADER_OVERHEAD) / getLoadSurvey().getEntryWidth();
                if (this.maxNrOfEntries < 0) {
                    this.maxNrOfEntries = DEFAULT_MAX_ENTRIES;
                }
            }
        }
        return maxNrOfEntries;
    }

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
					if (getLoadSurvey().isEventLog()) {
						strBuff.append(getChannelValues(interval)[channel].getString()+" ");
					} else {
						strBuff.append(getChannelValues(interval)[channel].getBigDecimal().multiply(loadSurvey.getLoadSurveyChannels()[channel].getScalingFactor())+"("+loadSurvey.getLoadSurveyChannels()[channel].getUnit()+") ");
					}
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
		byte[] bytes = ProtocolUtils.getSubArray2(getData(), offset, getLoadSurvey().getLoadSurveyChannels()[channelIndex].getWidth());
        ArrayUtils.reverse(bytes); // Convert little endian to big endian
        return bytes;
	}

	public AbstractRegisterType[] getChannelValues(int intervalIndex) throws IOException {
		AbstractRegisterType[] channelValues = new AbstractRegisterType[loadSurvey.getNrOfChannels()];
		RegisterTypeParser rtp = new RegisterTypeParser(loadSurvey.getCommandFactory().getProtocol().getTimeZone());
		AbstractRegisterType channelValue;
		for (int channel=0;channel<loadSurvey.getNrOfChannels();channel++) {
			LoadSurveyChannel loadSurveyChannel = loadSurvey.getLoadSurveyChannels()[channel];
			int chan_scaler = loadSurveyChannel.getDecimalPointPosition();

			if (channel == (loadSurvey.getNrOfChannels() - 1)) {
				channelValue = rtp.parse2Internal(DataType.C_BYTE.getType(), getData(intervalIndex, channel));
			} else {
				channelValue = rtp.parse2External(loadSurveyChannel.isInstantaneousChannel() ? DataType.I_SHORT.getType() : DataType.H_HEX_SHORT.getType() , getData(intervalIndex, channel));
				if (chan_scaler != 1) { // 1 = don't shift left; 3 = shift left with 3 positions
					channelValue = new RegisterTypeFloat(channelValue.getBigDecimal().movePointLeft(chan_scaler).floatValue());
				}
			}
			channelValues[channel] = channelValue;
		}
		return channelValues;
	}

	public LoadSurvey getLoadSurvey() {
		return loadSurvey;
	}

	public void setLoadSurvey(LoadSurvey loadSurvey) {
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
