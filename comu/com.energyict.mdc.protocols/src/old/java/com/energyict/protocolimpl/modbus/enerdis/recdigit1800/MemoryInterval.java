/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * 
 */
package com.energyict.protocolimpl.modbus.enerdis.recdigit1800;

import java.io.IOException;

class MemoryInterval {
	
	/**
	 * 
	 */
	
	private final RecDigit1800 recDigit1800;
	private int startAddress;
	private int stopAddress;
	
	ByteArray memory;
	
	MemoryInterval(RecDigit1800 recDigit1800, int sstAddress, int stpAddress){
		this.recDigit1800 = recDigit1800;
        
		startAddress = sstAddress;
		stopAddress = stpAddress;
		memory = null;
	}

	ByteArray getMemory( ) throws IOException{
		
		if( memory == null ) {
			memory = new ByteArray(recDigit1800.readRawValue( startAddress, (stopAddress - startAddress)/2 ));
		}
		
		return memory;
	}

}

