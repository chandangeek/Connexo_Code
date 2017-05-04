/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * 
 */
package com.energyict.protocolimpl.modbus.enerdis.recdigitpower;

import java.io.IOException;

class MemoryInterval {
	
	/**
	 * 
	 */
	
	private final RecDigitPower recDigitPower;
	private int startAddress;
	private int stopAddress;
	
	ByteArray memory;
	
	MemoryInterval(RecDigitPower recDigitPower, int sstAddress, int stpAddress){
		this.recDigitPower = recDigitPower;
        
		startAddress = sstAddress;
		stopAddress = stpAddress;
		memory = null;
	}

	ByteArray getMemory( ) throws IOException{
		
		if( memory == null ) {
			memory = new ByteArray(recDigitPower.readRawValue( startAddress, (stopAddress - startAddress) ) );
//			memory = new ByteArray(recDigitPower.readRawValue( startAddress, (stopAddress - startAddress)/4 ));
			
//			memory = new ByteArray(recDigitPower.readDataValue( startAddress, (stopAddress - startAddress)/2 ) );
		}
		
		return memory;
	}

}

