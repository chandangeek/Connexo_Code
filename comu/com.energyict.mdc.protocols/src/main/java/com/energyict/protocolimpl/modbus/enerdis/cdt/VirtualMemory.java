/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.mdc.protocol.api.UnsupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMemory {

	public List memoryList = new ArrayList();

	private boolean debug = false;

	private final RecDigitCdtPr recDigitCdtPr;

	/**
	 * @param recDigitPower
	 * @throws IOException
	 * @throws UnsupportedException
	 */
	VirtualMemory(RecDigitCdtPr recDigitCdtPr) {
		this.recDigitCdtPr = recDigitCdtPr;

	}
    final int MEMORY_BLOCKS [][] =
	    {
    	{0x03FE, 0x16BE}
	    };

    final int MEMORY_SIZE = MEMORY_BLOCKS[0][1] - MEMORY_BLOCKS[0][0];

    public final int BLOCK_SIZE = 0x32;

	void initMemory( int type ) throws UnsupportedException, IOException{

		int step = BLOCK_SIZE;
		int stt = MEMORY_BLOCKS[type][0];
		int stp;

		for(int i = 0; i < ( MEMORY_BLOCKS[type][1] - MEMORY_BLOCKS[type][0])/BLOCK_SIZE; i++ ){
			stp = stt + step;
			MemoryInterval memInterval = new MemoryInterval( this.recDigitCdtPr, stt,stp );
			memoryList.add( memInterval );
			stt = stp;
		}

		if ( debug ){
			System.out.println("initMemory Test!");
		}
	}

	ByteArray read( int actualPointer, int length ) throws IOException {

		int startReadPointer = actualPointer + 4;
		if ( startReadPointer == MEMORY_SIZE ){
			startReadPointer = 0;
		}
		int blockPointer = searchPointer( startReadPointer );
		int readLength = 0;
		int subStart = 0;

		ByteArray memoArray = new ByteArray(), hulpArray  = new ByteArray();

		while ( length > 0 ){

			if ( blockPointer == 0 )
				System.out.println("Testing!");

			hulpArray = ((MemoryInterval)(this.memoryList.get(blockPointer))).getMemory();

			readLength = length;
			subStart = ( ( startReadPointer ) - BLOCK_SIZE * blockPointer );

			length = length - readLength;

			memoArray.add(hulpArray.sub( subStart , readLength ));
			memoArray = recDigitCdtPr.pivot((memoArray.size() - readLength), memoArray);
		}

		return memoArray;

	}

	public int searchPointer(int point){
		int virtPointer = point / BLOCK_SIZE;
		return virtPointer;
	}

}