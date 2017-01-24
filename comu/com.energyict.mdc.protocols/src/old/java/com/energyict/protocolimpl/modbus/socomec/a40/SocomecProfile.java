package com.energyict.protocolimpl.modbus.socomec.a40;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Contains all relevant information about the LoadProfile of the current ModBus meter
 *
 * @author gna
 *
 */
public class SocomecProfile {

	/** The used ModBus meter */
	private final A40 modbus;
	/** The used profileInterval */
	private Integer profileInterval;
	/** Points to the current memoryPosition of the ActiveEnergy profile */
	private Integer activeEnergyPointer;
	/** Points to the current memoryPosition of the ReactiveEnergy profile */
	private Integer reActiveEnergyPointer;

	private static final int p1StartAddress = Integer.parseInt("12293");	//TODO this is probably 12293 (see doc. the previous object has 3 words and starts form 12290 ...)
	private static final int p2StartAddress = Integer.parseInt("16793");
	private static final int q1StartAddress = Integer.parseInt("21293");
	private static final int q2StartAddress = Integer.parseInt("25793");
	private static final int[] startAddresses = new int[]{p1StartAddress, p2StartAddress, q1StartAddress, q2StartAddress};
	private static final int maxProfileBlockSize = Integer.parseInt("4500");

	private static final int normalReadState = 0;
	private static final int memoryOverFlowedState = 1;

	private int[] startMemoryPointer;
	private int[] channelInfoRegisters;
	private List profileIntervalData;

	/** Maximum allowed blocks to read in 1 readAction */
	private static final int maxReadBlockSize = 100;

	/** The used profileParser */
	private SocomecProfileParser profileParser;

	/**
	 * Creates a new SocomecProfile
	 * @param modbus - the modbus instance
	 */
	SocomecProfile(Modbus modbus){
		this.modbus = (A40) modbus;
	}

	final SocomecProfileParser getProfileParser(){
		if(this.profileParser == null){
			this.profileParser = new SocomecProfileParser();
		}
		return this.profileParser;
	}

	/**
	 * @return the number of channels
	 * @throws IOException if we couldn't read the necessary register
	 */
	public int getNumberOfChannels() throws IOException{
		int counter = 0;
		for(int i = 0; i < getChannelInforRegisters().length; i++){
			if(getChannelInforRegisters()[i] == 1){
				counter++;
			}
		}
		return counter;
	}

	/**
	 * Find the requested register
	 * @param registerName - the name of the register
	 * @return the AbstractRegister
	 * @throws IOException if the register could not be found
	 */
	protected AbstractRegister findRegister(String registerName) throws IOException{
		return this.modbus.getRegisterFactory().findRegister(registerName);
	}

	/**
     * Check whether the loadProfile is supported.
     * The loadprofile can only be supported if one of the optionSlots contain a memoryModule
     * @return true if profileData is supported, false otherwise
     * @throws IOException if we can't get the info from the optionSlots
	 */
	public boolean isSupported() throws IOException {
		int[] optionSlot = findRegister(RegisterFactory.slotInfo).getReadHoldingRegistersRequest().getRegisters();
		OptionSlot[] optionsSlots = getProfileParser().parseOptionSlots(optionSlot);
    	for(int i = 0; i < optionsSlots.length; i++){
    		if(optionsSlots[i].hasMemoryOption()){
    			return true;
    		}
    	}
    	return false;
	}

	/**
	 * Getter for the profileInterval.
	 *
	 * @return the profileInterval
	 * @throws UnsupportedException if the register isn't supported
	 * @throws IOException if the registername isn't known
	 */
	public int getProfileInterval() throws UnsupportedException, IOException {
		if(profileInterval == null){
			profileInterval = getProfileParser().parseProfileInterval(findRegister(RegisterFactory.profileInterval).getReadHoldingRegistersRequest().getRegisters());
		}
		return profileInterval.intValue();
	}

	/**
	 * Build the profile channelInfos
	 * @return the list of channelInfos
	 * @throws IOException if the register could not be found
	 */
	public List getChannelInfos() throws IOException {
		BigDecimal multiplier = this.modbus.getMultiplierFactory().getMultiplier(MultiplierFactory.CT);
		List channelInfos = getProfileParser().parseChannelInfos(getChannelInforRegisters(), multiplier);
		return channelInfos;
	}

	/**
	 * @return the list of channelInfoRegisters
	 * @throws IOException if the register could not be found
	 */
	private int[] getChannelInforRegisters() throws IOException{
		if(this.channelInfoRegisters == null){
			this.channelInfoRegisters = findRegister(RegisterFactory.channelInfos).getReadHoldingRegistersRequest().getRegisters();
		}
		return this.channelInfoRegisters;
	}

	/**
	 * Get the IntervalDatas from the given date to now
	 * @param lastReading the startDate from the interval
	 * @return a list of {@link IntervalData} objects
	 * @throws IOException
	 * @throws UnsupportedException
	 */
	public List getIntervalDatas(Date lastReading) throws UnsupportedException, IOException {
		boolean dontExit = true;

		generateStartMemoryPointer();
		Date lastUpdate = getDateTimeLastProfileUpdate();

		for(int i = 0; i < getChannelInforRegisters().length; i++){
			if(getChannelInforRegisters()[i] == 1){	// the channel is enabled in the profile
				dontExit = true;
				int currentState = SocomecProfile.normalReadState;
				this.profileParser = new SocomecProfileParser();
				getProfileParser().setIntervalLength(getProfileInterval());
				int readFromPointer = 0;
				int readLength = 0;
				getProfileParser().setLastUpdate(lastUpdate);

				int currentPointer = startMemoryPointer[i];
				do{
					switch(currentState){
					case SocomecProfile.normalReadState:{

						if(((startAddresses[i] + currentPointer) - maxReadBlockSize) > startAddresses[i]){
							readFromPointer = (startAddresses[i] + currentPointer) - maxReadBlockSize;
							readLength = maxReadBlockSize;
							currentPointer -= readLength;
						} else {
							readLength = readFromPointer - startAddresses[i];
							readFromPointer = startAddresses[i];
							currentPointer = maxProfileBlockSize;
							currentState = SocomecProfile.memoryOverFlowedState;
						}

					}break;
					case SocomecProfile.memoryOverFlowedState:{
						if(((startAddresses[i] + currentPointer) - maxReadBlockSize) > (startAddresses[i] + startMemoryPointer[i])){
							readFromPointer = (startAddresses[i] + currentPointer) - maxReadBlockSize;
							readLength = maxReadBlockSize;
							currentPointer -= readLength;
						} else {
							readLength = readFromPointer - (startAddresses[i] + startMemoryPointer[i]);
							readFromPointer = (startAddresses[i] + startMemoryPointer[i]);
							dontExit = false;
						}
					}
					}

					getProfileParser().addMemoryPointer(readLength);
					getProfileParser().parseProfileDataBlock(this.modbus.readRawValue(readFromPointer, readLength));

				}while(((IntervalData) getProfileParser().getIntervalDatas().get(getProfileParser().getIntervalDatas().size()-1)).getEndTime().after(lastReading) && dontExit);

				checkForUnnecessaryIntervals(getProfileParser().getIntervalDatas(), lastReading);
				addValuesToIntervals(getProfileParser().getIntervalDatas());

			}
		}


		return this.profileIntervalData;

	}

	/**
	 * Add intervalData to the already existing intervalData
	 * @param intervals the intervalData's to add
	 */
	private void addValuesToIntervals(List intervals){
		if(this.profileIntervalData == null){
			this.profileIntervalData = new ArrayList();
			this.profileIntervalData = intervals;
		} else {
			ListIterator it = this.profileIntervalData.listIterator();
			while(it.hasNext()){
				IntervalData id = (IntervalData)it.next();
				ListIterator newIt = intervals.listIterator();
				while(newIt.hasNext()){
					IntervalData nid = (IntervalData)newIt.next();
					if(id.getEndTime().compareTo(nid.getEndTime()) == 0){
						Iterator iter = nid.getIntervalValues().iterator();
						while(iter.hasNext()){
							IntervalValue iv = ((IntervalValue)iter.next());
							id.addValue(iv.getNumber());
						}
					}
				}
			}
		}
	}

	/**
	 * Generate a helper object for memoryPointing to the start of the 4 profileBuffers
	 * @throws UnsupportedException if we couldn't read the pointers
	 * @throws IOException if we couldn't read the pointers
	 */
	private void generateStartMemoryPointer() throws UnsupportedException, IOException{
		int startedAEMemoryPointer = getActiveEnergyPointer();
		int startedREMemoryPointer = getReactiveEnergyPointer();
		startMemoryPointer = new int[4];
		startMemoryPointer[0] = startedAEMemoryPointer;
		startMemoryPointer[1] = startedAEMemoryPointer;
		startMemoryPointer[2] = startedREMemoryPointer;
		startMemoryPointer[3] = startedREMemoryPointer;

	}

	/**
	 * Remove unnecessary intervals from the intervalList
	 *
	 * @param intervals - the List with the intervals
	 * @param lastReading - the lastReading to check for
	 */
	private void checkForUnnecessaryIntervals(List intervals, Date lastReading){
		ListIterator it = intervals.listIterator();
		while(it.hasNext()){
			IntervalData id = (IntervalData)it.next();
			if(id.getEndTime().before(lastReading)) {
				it.remove();
			}
		}
	}

	/**
	 * @return the value of the ActiveEnergy Pointer
	 * @throws UnsupportedException if the register isn't supported
	 * @throws IOException if the read failed
	 */
	protected int getActiveEnergyPointer() throws UnsupportedException, IOException{
		if(this.activeEnergyPointer == null){
			int[] aePointer = findRegister(RegisterFactory.aePointer).getReadHoldingRegistersRequest().getRegisters();
			this.activeEnergyPointer = new Integer(getProfileParser().parseEnergyPointer(aePointer));
		}
		return this.activeEnergyPointer.intValue();
	}

	/**
	 * @return the value of the ReactiveEnergy pointer
 	 * @throws UnsupportedException if the register isn't supported
	 * @throws IOException if the read failed
	 */
	protected int getReactiveEnergyPointer() throws UnsupportedException, IOException{
		if(this.reActiveEnergyPointer == null){
			int[] rePointer = findRegister(RegisterFactory.rePointer).getReadHoldingRegistersRequest().getRegisters();
			this.reActiveEnergyPointer = new Integer(getProfileParser().parseEnergyPointer(rePointer));
		}
		return this.reActiveEnergyPointer.intValue();
	}

	/**
	 * @return the dateTime of the last profileUpdate
	 * @throws IOException if the read failed
	 */
	protected Date getDateTimeLastProfileUpdate() throws IOException{
		int[] dateTimeLastUpdate = findRegister(RegisterFactory.dtLastUpdate).getReadHoldingRegistersRequest().getRegisters();
		return getProfileParser().parseDateTimeLastUpdate(dateTimeLastUpdate);
	}


}
