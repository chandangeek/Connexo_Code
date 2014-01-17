package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * The profileRecorder is a type of buffer containing the raw profileData form the device.
 * @author gna
 *
 */
public class SiemensS4sProfileRecorder {

	private ProfileData profileData;				// The EICT profileObject
	private Calendar intervalTime;					// Calendar containing the value of the next interval
	private Calendar lastIntervalTime;				// Calendar containing the value of the last interval

	private byte[] profileBuffer = new byte[]{};	// A buffer contain the rawData from the meters memory
	private int offset;								// A pointer pointing to the last treated data
	private int profilePeriod;						// The interval of the profile

	private int intervalRecordSize;					// Contains the size of ONE record(dependent of the number of channels)
	private int intervalDateRecordSize;				// Contains the size of a record with a dateTime included

	/**
	 * Creates a new instance of the ProfileRecorder
	 * @param profilePeriod the period of the loadProfile
	 */
	public SiemensS4sProfileRecorder(int profilePeriod){
		this.profileData = new ProfileData();
		this.offset = 0;
		this.profilePeriod = profilePeriod;
	}

	/**
	 * Set the list of channelInfos to the profileDataObject in this RecoderObject
	 * Also initialize the size of the intervalRecords
	 * @param channelInfos
	 */
	public void setChannelInfos(List channelInfos) {
		this.profileData.setChannelInfos(channelInfos);
		this.intervalRecordSize = 4 + 4*(this.profileData.getNumberOfChannels());	// 4 for the header and 4 for each channel
		this.intervalDateRecordSize = this.intervalRecordSize + 8;					// 8 being the size of a date and time
	}

	/**
	 * Method to add a part to raw buffer and calculate the intervals
	 * @param profilePart the raw intervalData
	 * @throws IOException if parsing of the rawData fails
	 */
	public void addProfilePart(byte[] profilePart) throws IOException {

		byte[] reversedBuffer = S4sObjectUtils.revertByteArray(profilePart);
		this.offset += reversedBuffer.length;

//		this.profileBuffer = ProtocolUtils.concatByteArrays(this.profileBuffer, reversedBuffer);
		this.profileBuffer = ProtocolUtils.concatByteArrays(reversedBuffer, this.profileBuffer);

		List intervalBuffer = createIntervalBuffer();

		for(int i = intervalBuffer.size()-1; i >= 0; i--){
			SiemensS4sProfileRecord record = new SiemensS4sProfileRecord((byte[])intervalBuffer.get(i), this.intervalTime, this.profileData.getNumberOfChannels());
			IntervalData id = record.getIntervalData();
			if(record.possibleDelete()){
				Iterator it = this.profileData.getIntervalDatas().iterator();
				while (it.hasNext()) {
					IntervalData ivdt = (IntervalData) it.next();
					if(ivdt.getEndTime().compareTo(id.getEndTime()) == 0){
						it.remove();
					}
				}
			}
			this.profileData.addInterval(id);
			this.intervalTime = record.getLastIntervalCalendar();
			this.lastIntervalTime = this.intervalTime;
			setToNextInterval();
		}
	}

	/**
	 * Set the time to the next interval
	 * @throws IOException
	 */
	private void setToNextInterval() throws IOException{
		this.intervalTime.add(Calendar.SECOND, -this.profilePeriod);
		ParseUtils.roundUp2nearestInterval(this.intervalTime, this.profilePeriod);
	}

	/**
	 * Creates a list containing the byteArrays per interval. We create a list for this because the
	 * arrays have a different size if they contain a dateTime in it.
	 * @return a list with a number of raw byteArrays
	 * @throws IOException if parsing of the tempDatapart failed
	 */
	private List createIntervalBuffer() throws IOException{
		List buffer = new ArrayList();
		byte[] tempDataPart;
//		int tempOffset = this.profileBuffer.length;

//		tempOffset -= intervalRecordSize;
//		while(tempOffset >= this.offset){
//			tempDataPart = ProtocolUtils.getSubArray2(this.profileBuffer, tempOffset, intervalRecordSize);
//			if(S4sObjectUtils.itsActuallyADateIntervalRecord(tempDataPart)){
//				tempOffset += intervalRecordSize;
//				tempOffset -= intervalDateRecordSize;
//				tempDataPart = ProtocolUtils.getSubArray2(this.profileBuffer, tempOffset, intervalDateRecordSize);
//			}
//			new String(tempDataPart);
//			buffer.add(tempDataPart);
//			tempOffset -= intervalRecordSize;
//		}

		while((this.offset - intervalRecordSize) >= 0 ){
			this.offset -= intervalRecordSize;
			tempDataPart = ProtocolUtils.getSubArray2(this.profileBuffer, this.offset, intervalRecordSize);
			if(S4sObjectUtils.itsActuallyADateIntervalRecord(tempDataPart)){
				this.offset += intervalRecordSize;
				if((this.offset - intervalDateRecordSize) >=0){
					this.offset -= intervalDateRecordSize;
					tempDataPart = ProtocolUtils.getSubArray2(this.profileBuffer, this.offset, intervalDateRecordSize);
					buffer.add(tempDataPart);
				} else {
					this.offset -= intervalRecordSize;
				}
			} else {
				buffer.add(tempDataPart);
			}
		}

//		this.offset = this.profileBuffer.length;
		return buffer;
	}

	/**
	 * @return the date of the last interval
	 */
	public Date getLastIntervalDate() {
		return this.lastIntervalTime.getTime();
	}

	/**
	 * Return the profileData object
	 * @return
	 */
	public ProfileData getProfileData() {
		return this.profileData;
	}

	/**
	 * Setter for last intervalTime.
	 * We use the currentMeterTime and calculate the latest intervalDate
	 * @param meterTime
	 * @throws IOException
	 */
	public void setFirstIntervalTime(Calendar meterTime) throws IOException {
		this.intervalTime = meterTime;
		ParseUtils.roundDown2nearestInterval(this.intervalTime, this.profilePeriod);
		this.lastIntervalTime = this.intervalTime;
	}
}
