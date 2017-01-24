package com.energyict.protocolimpl.modbus.socomec.a40;

/**
 * Contains the optionSlot information
 * 
 * @author gna
 *
 */
public class OptionSlot {

	private byte option;
	
	OptionSlot(byte option){
		this.option = option;
	}
	
	/**
	 * @return true if slot has no options, false otherwise
	 */
	boolean hasNoOptions(){
		return this.option == 0xFF;
	}
	
	/**
	 * @return true if slot is communication module, false otherwise
	 */
	boolean hasCommunicationOption(){
		return this.option == 0x00;
	}
	
	/**
	 * @return true if slot is counter module, false otherwise
	 */
	boolean hasCounterOption(){
		return this.option == 0x01;
	}
	
	/**
	 * @return true if slot is counter/Harmonics module, false otherwise
	 */
	boolean hasCounterAndHarmonicasOption(){
		return this.option == 0x03;
	}
	
	/**
	 * @return true if slot is input/output module, false otherwise
	 */
	boolean hasInputOutputOption(){
		return this.option == 0x20;
	}
	
	/**
	 * @return true if slot is analoge output module, false otherwise
	 */
	boolean hasAnalogOutputOption(){
		return this.option == 0x30;
	}
	
	/**
	 * @return true if slot is memory module(LoadProfile), false otherwise
	 */
	boolean hasMemoryOption(){
		return this.option == 0x40;
	}

	/**
	 * @return true if slot is profibusModule, false otherwise
	 */
	boolean hasProfiBusOption(){
		return this.option == 0x50;
	}
	
	/**
	 * @return true if slot is an input measurement module, false otherwise
	 */
	boolean hasInputMeasurementOption(){
		return this.option == 0xF0;
	}
}
