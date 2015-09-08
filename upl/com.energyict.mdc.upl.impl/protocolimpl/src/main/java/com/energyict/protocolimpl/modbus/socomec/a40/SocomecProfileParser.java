package com.energyict.protocolimpl.modbus.socomec.a40;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ParseUtils;

/**
 * Parse for profile relevant objects.
 * @author gna
 *
 */
public class SocomecProfileParser {
	
	private static final int normalState = 0;
	private static final int timedValue = 1;
	private static final int powerUp = 2;
	private static final int powerDown = 3;

	/** ChannelInfoNames to construct the ChannelInfo List */
	//"Active Energy plus", "Active Energy minus", "Reactive Energy plus", "Reactive Energy minus"
	private static String[] channelInfoNames = new String[]{"0.1.128.0.0.255", "0.2.128.0.0.255",
															"0.3.128.0.0.255", "0.4.128.0.0.255"};
	
	/** ChannelInfoUnits to construct the ChannelInfo List */
	private static Unit[] channelInfoUnits = new Unit[]{Unit.get(BaseUnit.WATT, -1), Unit.get(BaseUnit.WATT, -1),
															Unit.get(BaseUnit.VOLTAMPEREREACTIVE, -1), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, -1)};
	
	/** The current intervalDate */
	private Calendar intervalDate;
	/** The current memoryPointer */
	private int memoryPointer;
	/** The current profileInterval */
	private Integer profileInterval;
	
	/** Contains the values for the meter */
	private int[] virtualMemory;
	
	/** The current profileParseState */
	private int currentState;
	
	/** The List of {@link IntervalData}s */
	private List intervalDatas;
	/** The list of {@link MeterEvent}s */
	private List meterEvents;
	
	/** Constructor */
	protected SocomecProfileParser(){
		this.memoryPointer = -1;
		this.currentState = normalState;
		this.intervalDatas = new ArrayList();
	}
	
	/**
	 * Parse the profileInterval register
	 * 
	 * @param registers the register read from the ModBus meter
	 * @return the profileInterval
	 * @throws UnsupportedException if the registers are empty
	 */
	Integer parseProfileInterval(int[] registers) throws UnsupportedException{
		if(registers.length == 1){
			setIntervalLength(registers[0]);
			return this.profileInterval;
		} else {
			throw new UnsupportedException("ProfileInterval not supported.");
		}
	}

	/**
	 * Parse the channelInfo registers.
	 * Maximum four channels (P+, P-, Q+, Q-) will be added according to there appearance.
	 * The Unit is fixed for all channels to W/10
	 * 
	 * @param channelInfoRegisters the registers read from the ModBus meter
	 * @return a List of ChannelInfo
	 */
	List parseChannelInfos(int[] channelInfoRegisters, BigDecimal multiplier) {
		List channelInfos = new ArrayList();
		int counter = Integer.parseInt("0");
		for(int i = 0; i < channelInfoRegisters.length; i++){
			if(channelInfoRegisters[i] == 1){
				int id = counter++;
				channelInfos.add(new ChannelInfo(id, channelInfoNames[i], channelInfoUnits[i], 1, id, multiplier));
			}
		}
		return channelInfos;
	}

	/**
	 * Parse the Pointer to the correct memoryLocation
	 * 
	 * @param pointer the registers read from the ModBus meter
	 * @return the value of the pointer
	 * @throws UnsupportedException if the value of pointer is corrupt
	 */
	int parseEnergyPointer(int[] pointer) throws UnsupportedException {
		if(pointer.length == 1){
			return pointer[0];
		} else {
			throw new UnsupportedException("ProfileInterval not supported.");
		}
	}

	/**
	 * Parse the OptionSlots
	 * 
	 * @param optionSlot the registers read from the ModBus meter
	 * @return an Array of OptionSlots
	 */
	OptionSlot[] parseOptionSlots(int[] optionSlot) {
		OptionSlot[] os = new OptionSlot[optionSlot.length];
		for(int i = 0; i < os.length; i++){
			os[i] = new OptionSlot((byte)optionSlot[i]);
		}
		return os;
	}
	
	/**
	 * Parse the dateTime of the last profileUpdate
	 * 
	 * @param dateTime - the registers read from the device
	 * @return a Date object translated from the input
	 */
	Date parseDateTimeLastUpdate(int[] dateTime){
		DateTime dt = DateTime.parseProfileDateTime(dateTime);
		setLastUpdate(dt.getMeterCalender().getTime());
		return this.intervalDate.getTime();
	}
	
	/**
	 * Parse a part of the profileDataMemory
	 * 
	 * We are making abuse of the meterEvents to log the intervalStatusses
	 * 
	 * @param memoryBlocks - the register read from the device
	 * @throws IOException when the intervalDate is empty, you need to give at least a startDate
	 */
	void parseProfileDataBlock(int[] memoryBlocks) throws IOException{
		boolean exit = false;
		if(this.intervalDate == null){
			throw new IOException("StartDate can not be empty.");
		}
		
		if(this.memoryPointer == -1){
			return;
		}
		
		addVirtualMemory(memoryBlocks);
		
		do{
			switch(this.currentState){
			case normalState:{
				if(dateTimeStamp()){
					this.currentState = timedValue;
				} else {
					addAnInterval();
				}
			}break;
			case timedValue:{
				if(this.memoryPointer-5 >= 0){
					if(isItAPowerUpPowerDownSequence()){
						this.currentState = powerUp;
					} else { // if it's not a PU/PD sequence then its a timeStamped value
						setLastUpdate(DateTime.parseProfileDateTime(ParseUtils.getSubArray(this.virtualMemory, this.memoryPointer-2, 3)).getMeterCalender().getTime());
						this.memoryPointer -= 3; //This can cause a negative value
						if(this.memoryPointer < 0){
							this.memoryPointer += 3;
							exit = true;
							break;
						}
						addAnInterval();
						this.currentState = normalState;
					}
				} else {
					exit = true;
					break;
				}
			}break;
			case powerUp:{
				//Add a powerUp event
				Calendar eventDate = DateTime.parseProfileDateTime(ParseUtils.getSubArray(this.virtualMemory, this.memoryPointer-2, 3)).getMeterCalender();
				addMeterEvents(new MeterEvent(eventDate.getTime(), MeterEvent.POWERUP));
				ParseUtils.roundUp2nearestInterval(eventDate, this.profileInterval.intValue());
				setLastUpdate(eventDate.getTime());
				
				this.memoryPointer -= 3; //This can cause a negative value
				if(isItAPowerDown()){
					this.currentState = powerDown;
				} else {
					this.currentState = normalState;
				}
			}break;
			case powerDown:{
				//Add a powerDown event
				Calendar eventDate = DateTime.parseProfileDateTime(ParseUtils.getSubArray(this.virtualMemory, this.memoryPointer-2, 3)).getMeterCalender();
				addMeterEvents(new MeterEvent(eventDate.getTime(), MeterEvent.POWERDOWN));
				ParseUtils.roundDown2nearestInterval(eventDate, this.profileInterval.intValue());
				setLastUpdate(eventDate.getTime());
				
				this.memoryPointer -= 3; //This can cause a negative value
				this.currentState = normalState;
			}break;
			default:{
			throw new IOException("Invalid parseState");	
			}
			}
		}while((this.memoryPointer >= 2) && (!exit) );
		
		// Apply the meterEvents so we get the statusses
		applyEvents();
		
	}
	
    /** <p> Set the interval status based on the {@link MeterEvent}s form the meter </p>
     * @param idList - the IntervalData list
     */    
    private void applyEvents() {
        Iterator eventIterator = getMeterEvents().iterator();
        while (eventIterator.hasNext()) {
            applyEvent((MeterEvent) eventIterator.next());
        }
    }
    
    /** <p>Updates the interval status based on the information of a single event. </p>
     * @param event - the event to convert to intervalStatus
     * @param idList - the IntervalData list
     */    
    private void applyEvent(MeterEvent event) {
        Iterator intervalIterator = this.intervalDatas.iterator();
        while (intervalIterator.hasNext()) {
            ((IntervalData) intervalIterator.next()).apply(event, this.profileInterval.intValue()/60);
        }
    }
	
	/**
	 * Add an interval to the IntervalDataList
	 */
	private void addAnInterval(){
		if(!intervalExists()){
			int value = this.virtualMemory[this.memoryPointer];
			IntervalData id = new IntervalData(this.intervalDate.getTime());
			id.addValue(new BigDecimal(value));
			this.intervalDatas.add(id);
		}
		this.memoryPointer--;
		this.intervalDate.add(Calendar.SECOND, -this.profileInterval.intValue());
	}
	
	/**
	 * Check if an interval already exists
	 * @return true if it's so, false otherwise
	 */
	private boolean intervalExists(){
		ListIterator listIt = this.intervalDatas.listIterator();
		while(listIt.hasNext()){
			IntervalData id = (IntervalData)listIt.next();
			if(id.getEndTime().compareTo(this.intervalDate.getTime()) == 0 ){
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the memoryPointer currently points to the start of a dateTimeStamp (not a value)
	 * @return true if it is, false otherwise
	 */
	private boolean dateTimeStamp() {
		if((this.memoryPointer-2 >= 0) && (this.virtualMemory[this.memoryPointer-2]&0xF000) == 0xF000){
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the memoryPointer currently points to the start of a PowerUp/PowerDown sequence
	 * @return true if it is, false otherwise
	 */
	private boolean isItAPowerUpPowerDownSequence(){
		if(((this.virtualMemory[this.memoryPointer-2]&0xF000) == 0xF000) && (this.virtualMemory[this.memoryPointer-5]&0xE000) == 0xE000){
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the memoryPointer currently points to the start of a PowerDown dateTime
	 * @return true if it is, false otherwise
	 */
	private boolean isItAPowerDown(){
		if((this.memoryPointer-2 >= 0) && (this.virtualMemory[this.memoryPointer-2]&0xE000) == 0xE000){
			return true;
		}
		return false;
	}

	/**
	 * Add the memoryBlocks to the virtualMemory
	 * @param memoryBlocks - the memoryBlocks to add
	 */
	private void addVirtualMemory(int[] memoryBlocks) {
		if(this.virtualMemory == null){
			this.virtualMemory = memoryBlocks;
		} else {
			/* It's a bit nasty thing to do but we need to add the memoryBlocks at the start
			 * of the virtualMemory.
			 */
			int[] tempVirtualMemory = (int[])this.virtualMemory.clone();
			this.virtualMemory = new int[memoryBlocks.length + tempVirtualMemory.length];
			System.arraycopy(memoryBlocks, 0, this.virtualMemory, 0, memoryBlocks.length);
			System.arraycopy(tempVirtualMemory, 0, this.virtualMemory, memoryBlocks.length, tempVirtualMemory.length);
		}
	}

	/**
	 * Set the lastUpdate date of the profile
	 * @param lastUpdate the last Update
	 */
	void setLastUpdate(Date lastUpdate) {
		if(this.intervalDate == null){
			this.intervalDate = ProtocolUtils.getCleanGMTCalendar();
		}
		this.intervalDate.setTime(lastUpdate);
	}

	/**
	 * Add a value to the memoryPointer
	 * @param memoryPointer - the value to add
	 */
	void addMemoryPointer(int memoryPointer) {
		this.memoryPointer += memoryPointer;
	}

	/**
	 * Set the profileInterval
	 * @param profileInterval - the current profileInterval
	 */
	void setIntervalLength(int profileInterval) {
		this.profileInterval = new Integer(profileInterval);
	}
	
	/**
	 * Getter for the virtualMemory
	 * @return the virtualMemory
	 */
	protected int[] getVirtualMemory(){
		return this.virtualMemory;
	}

	/**
	 * Add a {@link MeterEvent} to the meterEventList
	 * @param meterEvent - the given MeterEvent
	 */
	private void addMeterEvents(MeterEvent meterEvent){
		if(this.meterEvents == null){
			this.meterEvents = new ArrayList();
		}
		this.meterEvents.add(meterEvent);
	}
	
	/**
	 * @return the occurred meterEvents
	 */
	public List getMeterEvents() {
		if(this.meterEvents == null){
			this.meterEvents = new ArrayList();
		}
		return this.meterEvents;
	}
	
	/**
	 * @return all the IntervalDatas calculated from the memoryBlocks
	 */
	public List getIntervalDatas(){
		return this.intervalDatas;
	}
}
