package com.energyict.protocolimpl.modbus.socomec.a40;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;

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
	
	private static final int p1StartAddress = Integer.valueOf(12293);	//TODO this is probably 12293 (see doc. the previous object has 3 words and starts form 12290 ...)
	private static final int p2StartAddress = Integer.valueOf(16793);
	private static final int q1StartAddress = Integer.valueOf(21293);
	private static final int q2StartAddress = Integer.valueOf(25793);
	private static final int maxProfileBlockSize = Integer.valueOf(4500);
	
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
		return profileInterval;
	}
	
	/**
	 * Build the profile channelInfos
	 * @return the list of channelInfos
	 * @throws IOException if the register could not be found
	 */
	public List<ChannelInfo> getChannelInfos() throws IOException {
		int[] channelInfoRegisters = findRegister(RegisterFactory.channelInfos).getReadHoldingRegistersRequest().getRegisters();
		List<ChannelInfo> channelInfos = getProfileParser().parseChannelInfos(channelInfoRegisters);
		return channelInfos;
	}
	
	/**
	 * Get the IntervalDatas from the given date to now
	 * @param lastReading the startDate from the interval
	 * @return a list of {@link IntervalData} objects
	 */
	public List<IntervalData> getIntervalDatas(Date lastReading) {
		
		try {
			
			Date lastUpdate = getDateTimeLastProfileUpdate();
			int[] profileIntervalsPPlus = this.modbus.readRawValue(13750, 100);
			lastUpdate = getDateTimeLastProfileUpdate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @return the value of the ActiveEnergy Pointer
	 * @throws UnsupportedException if the register isn't supported
	 * @throws IOException if the read failed
	 */
	protected int getActiveEnergyPointer() throws UnsupportedException, IOException{
		if(this.activeEnergyPointer == null){
			int[] aePointer = findRegister(RegisterFactory.aePointer).getReadHoldingRegistersRequest().getRegisters();
			this.activeEnergyPointer = getProfileParser().parseEnergyPointer(aePointer);
		} 
		return this.activeEnergyPointer;
	}
	
	/**
	 * @return the value of the ReactiveEnergy pointer
 	 * @throws UnsupportedException if the register isn't supported
	 * @throws IOException if the read failed
	 */
	protected int getReactiveEnergyPointer() throws UnsupportedException, IOException{
		if(this.reActiveEnergyPointer == null){
			int[] rePointer = findRegister(RegisterFactory.rePointer).getReadHoldingRegistersRequest().getRegisters();
			this.reActiveEnergyPointer = getProfileParser().parseEnergyPointer(rePointer);
		}
		return this.reActiveEnergyPointer;
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
