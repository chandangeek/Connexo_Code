package com.energyict.protocolimpl.iec1107.siemenss4s;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectUtils;


public class SiemensS4sProfileRecorder {
	
	private ProfileData profileData;
	private Calendar intervalTime;
	private Calendar lastIntervalTime;
	
	private byte[] profileBuffer = new byte[]{};
	private int offset;
	private int profilePeriod;
	
	private int intervalRecordSize;
	private int intervalDateRecordSize;
	
	public SiemensS4sProfileRecorder(int profilePeriod){
		this.profileData = new ProfileData();
//		this.offset = 56;	// defend this init
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
	 * TODO check the size of the profilePart, if it's not a complete record, than wait for a complete record!
	 * @param profilePart
	 * @throws IOException 
	 */
	public void addProfilePart(byte[] profilePart) throws IOException {
		
		byte[] reversedBuffer = S4sObjectUtils.revertByteArray(profilePart);
		
		this.profileBuffer = ProtocolUtils.concatByteArrays(this.profileBuffer, reversedBuffer);
		
		//TODO do this as much as you can
		byte[] temp = getDataRecordArray();
		while(temp.length != 0){
			SiemensS4sProfileRecord record = new SiemensS4sProfileRecord(temp, this.intervalTime, this.profileData.getNumberOfChannels());
			this.profileData.addInterval(record.getIntervalData());
			this.lastIntervalTime = this.intervalTime;
			this.intervalTime.add(Calendar.SECOND, -this.profilePeriod);
			temp = getDataRecordArray();
		}
	}
	
	private byte[] getDataRecordArray() throws IOException{
		byte[] data;
		if((this.offset + intervalRecordSize) <= this.profileBuffer.length){	// can we take a piece thats large enough
			data = ProtocolUtils.getSubArray2(this.profileBuffer, this.offset, intervalRecordSize);
			if(itsActuallyADateIntervalRecord(data)){
				//check if we can take a piece as large as the dateInterval
				if((this.offset + this.intervalDateRecordSize) <= this.profileBuffer.length){
					data = ProtocolUtils.getSubArray2(this.profileBuffer, this.offset, this.intervalDateRecordSize);
					this.offset += this.intervalDateRecordSize;
				} else {
					data = new byte[0];	// indicating we need more data!
				}
			} else {
				this.offset += this.intervalRecordSize;
			}
		} else {
			data = new byte[0];		// indicating we need more data!
		}
		return data;
	}
	
	private boolean itsActuallyADateIntervalRecord(byte[] recordData) throws IOException{
		if((ProtocolUtils.hex2nibble(recordData[recordData.length-4])&0x01) == 1){
			return true;
		} else {
			return false;
		}
	}

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

	public void setFirstIntervalTime(Calendar meterTime) throws IOException {
		this.intervalTime = meterTime;
		ParseUtils.roundDown2nearestInterval(this.intervalTime, this.profilePeriod);
		this.lastIntervalTime = this.intervalTime;
	}
}
