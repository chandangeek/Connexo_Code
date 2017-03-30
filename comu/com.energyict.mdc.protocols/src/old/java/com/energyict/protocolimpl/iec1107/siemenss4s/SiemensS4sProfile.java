/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sIntegrationPeriod;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectFactory;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sRegisterConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object that handles the collecting and parsing of profileData.
 * All profile attributes (interval, number of channels) can be fetched with this object as well.
 * @author gna
 * @since 24/08/2009
 */
public class SiemensS4sProfile {

	private S4sObjectFactory s4sObjectFactory;
	private S4sIntegrationPeriod integrationPeriodObject;

	private boolean bufferOverFlow = false;
	private int smallerStep = 0;

	private static int PROFILE_READ_BLOCK_SIZE = 0x40;
	private static int PROFILE_MEMORY_START_ADDRESS = 0x2000;
	private static int PROFILE_MEMORY_STOP_ADDRESS = 0x5FFF;

	/**
	 * Creates a new instance of the Siemens Profile Object
	 * @param objectFactory
	 */
	public SiemensS4sProfile(S4sObjectFactory objectFactory) {
		this.s4sObjectFactory = objectFactory;
	}

	/**
	 * @return the profileInterval of the device
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public int getProfileInterval() throws FlagIEC1107ConnectionException, ConnectionException, IOException {
		return getIntegrationPeriodObject().getInterval();
	}

	/**
	 * Create the IntegrationPeriod object if it doesn't exist
	 * @return the IntegrationPeriod object
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	private S4sIntegrationPeriod getIntegrationPeriodObject() throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		if(this.integrationPeriodObject == null){	// lazy init because we only want to read it once
			this.integrationPeriodObject = getObjectFactory().getIntegrationPeriodObject();
		}
		return integrationPeriodObject;
	}

	private S4sObjectFactory getObjectFactory(){
		return this.s4sObjectFactory;
	}

	/**
	 * Retrieve and build the profileData
	 *
	 * <b>Note:</b>
	 * We retrieve the blocks in size of 40 bytes because MV-90 does it like this.
	 * TODO: Test if we can read bigger blocks, which will lead to less communication
	 *
	 * @param lastReading - the date from where to start reading
	 * @param includeEvents - indicates whether you need to read the events
	 * @return a fully build profileData object
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws FlagIEC1107ConnectionException, ConnectionException, IOException {
		List channelInfos = getChannelInfos();
		SiemensS4sProfileRecorder pRecorder = new SiemensS4sProfileRecorder(getProfileInterval());
		if(channelInfos.size() != 0){
			byte[] profilePart;
			byte[] preparedReadProfileCommand;
			Date lastIntervalDate = null;

			// The current pointer points to the interval being constructed, so we don't need that one but all the data before
			int offsetPointer = decreaseMemoryPointer(getObjectFactory().getProfilePointerObject().getCurrentPointer());

			pRecorder.setChannelInfos(channelInfos);
			pRecorder.setFirstIntervalTime(getObjectFactory().getDateTimeObject().getMeterTime());
			do{

				preparedReadProfileCommand = prepareReadProfilePartCommand(offsetPointer);
				profilePart = getObjectFactory().readRawMemoryBlock(preparedReadProfileCommand);
				pRecorder.addProfilePart(profilePart);
				offsetPointer = decreaseMemoryPointer(offsetPointer);
				lastIntervalDate = pRecorder.getLastIntervalDate();

			}while(lastReading.before(lastIntervalDate) && !this.bufferOverFlow);
		} else {
			Logger.global.log(Level.INFO, "Meter returned no channelInformation so no profileData is constructed.");
		}
		deleteUnwantedIntervals( pRecorder.getProfileData(), lastReading );
		return pRecorder.getProfileData();
	}

	public List getChannelInfos() throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		byte[] allChannelInfos = getObjectFactory().getAllChannelInfosRawData();
		return getChannelInfos(allChannelInfos);
	}

	/**
	 * Create a list of channelInfo objects. If a channel isn't used, then don't create an info for it.
	 * @param allChannelInfos a byteArray containing the rawBytes of the 4 channelInfoRegisters
	 * @return a List of channelInfos.
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	protected List getChannelInfos(byte[] allChannelInfos) throws FlagIEC1107ConnectionException, ConnectionException, IOException {

		List channelInfos = new ArrayList();

		S4sRegisterConfig chanConfig;
		ChannelInfo ci;
		int index = 0;
		for(int i = 0; i < 4; i++){
			chanConfig = new S4sRegisterConfig(ProtocolUtils.getSubArray2(allChannelInfos, i*2, 2));
			if(chanConfig.isValid()){
				// TODO check if you have to set a Multiplier or scaler to the channels
				ci = new ChannelInfo(index, "Channel_" + index + " - " + chanConfig.getType(), chanConfig.getUnit());
				channelInfos.add(ci);
				index++;
			}
		}

		return channelInfos;
	}

	/**
	 * Create a readCommand with the offset as a memoryAddress
	 * The memoryAddress must be in upperCase!!
	 * @param offset MemoryAddress of the profileMemory
	 * @return a byteArray containing a readCommand
	 * @throws IOException
	 */
	protected byte[] prepareReadProfilePartCommand(int offset) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int memoryLocation = PROFILE_MEMORY_START_ADDRESS + offset;
		baos.write(Integer.toHexString(memoryLocation).toUpperCase().getBytes());
		baos.write(("(" + Integer.toHexString(PROFILE_READ_BLOCK_SIZE - smallerStep) + ")").getBytes());
		return baos.toByteArray();
	}

	/**
	 * Decrease the current pointer with the readSize.
	 * Make extra checks for memoryBufferOverFlows so you don't keep on reading the complete buffer
	 * If you reach the start of the buffer, then only read the necessary bytes and continue at the end of the buffer
	 * @param offsetPointer - the pointer used for the latest profileReadPart
	 * @return the decreased pointer
	 * @throws FlagIEC1107ConnectionException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	private int decreaseMemoryPointer(int offsetPointer) throws FlagIEC1107ConnectionException, ConnectionException, IOException{

		// Check if you have a buffer OverFlow
		if((offsetPointer > getObjectFactory().getProfilePointerObject().getCurrentPointer()) &&
				(offsetPointer - PROFILE_READ_BLOCK_SIZE) < getObjectFactory().getProfilePointerObject().getCurrentPointer()){
			this.bufferOverFlow = true;
		}

		if(offsetPointer == 0){
			smallerStep = 0;
			offsetPointer = 0x3FFF - PROFILE_READ_BLOCK_SIZE;
		} else if((offsetPointer - PROFILE_READ_BLOCK_SIZE) < 0){
			smallerStep = PROFILE_READ_BLOCK_SIZE - offsetPointer;
			offsetPointer = 0;
		} else {
			smallerStep = 0;
			offsetPointer -= PROFILE_READ_BLOCK_SIZE;
		}
		return offsetPointer;
	}

	/**
	 * Because we read memoryBlocks, we get more data then needed.
	 * This method skips the unwanted intervals.
	 * @param pd is the ProfileData to shift
	 * @param lastReading is the first date we may find in the profileData
	 */
	private void deleteUnwantedIntervals(ProfileData pd, Date lastReading){
		Iterator it = pd.getIntervalDatas().iterator();
		while (it.hasNext()) {
			IntervalData ivdt = (IntervalData) it.next();
			if(ivdt.getEndTime().before(lastReading)){
				it.remove();
			}
		}
	}

}
