/**
 * 
 */
package com.energyict.protocolimpl.modbus.enerdis.cdt;

import java.io.IOException;

class MemoryInterval {
	
	/**
	 * 
	 */
	
	private final RecDigitCdtPr recDigitCdtPr;
	private int startAddress;
	private int stopAddress;
	
	ByteArray memory;
	
	MemoryInterval(RecDigitCdtPr recDigitCdtPr, int sstAddress, int stpAddress){
		this.recDigitCdtPr = recDigitCdtPr;
        
		startAddress = sstAddress;
		stopAddress = stpAddress;
		memory = null;
	}

	ByteArray getMemory( ) throws IOException{
		
		if( memory == null ) {
			memory = new ByteArray(recDigitCdtPr.readRawValue( startAddress, (stopAddress - startAddress) ) );
//			memory = new ByteArray(recDigitPower.readRawValue( startAddress, (stopAddress - startAddress)/4 ));
			
//			memory = new ByteArray(recDigitPower.readDataValue( startAddress, (stopAddress - startAddress)/2 ) );
		}
		
		return memory;
	}

}

