/**
 * 
 */
package com.energyict.protocolimpl.modbus.enerdis.recdigitcct;

import java.io.IOException;

class MemoryInterval {
	
	/**
	 * 
	 */
	
	private final RecDigitCct recDigitCct;
	private int startAddress;
	private int stopAddress;
	
	ByteArray memory;
	
	MemoryInterval(RecDigitCct recDigitCct, int sstAddress, int stpAddress){
		this.recDigitCct = recDigitCct;
        
		startAddress = sstAddress;
		stopAddress = stpAddress;
		memory = null;
	}

	ByteArray getMemory( ) throws IOException{
		
		if( memory == null ) {
			memory = new ByteArray(recDigitCct.readRawValue( startAddress, (stopAddress - startAddress) ) );
		}
		
		return memory;
	}

}

