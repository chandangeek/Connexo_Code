/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.modbus.enerdis.recdigitpower;

import com.energyict.mdc.protocol.api.UnsupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMemory {

	public List memoryList = new ArrayList();

	private boolean debug = false;

	private final RecDigitPower recDigitPower;

	/**
	 * @param recDigitPower
	 * @throws IOException
	 * @throws UnsupportedException
	 */
	VirtualMemory(RecDigitPower recDigitPower) {
		this.recDigitPower = recDigitPower;

	}
    final int MEMORY_BLOCKS [][] =
	    {
	    {0x0901, 0x1261},
	    {0x1261, 0x1BC1},
	    };

    final int MEMORY_SIZE = MEMORY_BLOCKS[0][1] - MEMORY_BLOCKS[0][0];

    public final int BLOCK_SIZE = 0x32;

	void initMemory( int type ) throws UnsupportedException, IOException{

		int step = BLOCK_SIZE;
		int stt = MEMORY_BLOCKS[type][0];
		int stp;

		for(int i = 0; i < ( MEMORY_BLOCKS[type][1] - MEMORY_BLOCKS[type][0])/BLOCK_SIZE; i++ ){
			stp = stt + step;
			MemoryInterval memInterval = new MemoryInterval( this.recDigitPower, stt,stp );
			memoryList.add( memInterval );
			stt = stp;
		}

		if ( debug ){
			System.out.println("initMemory Test!");
		}
	}

	ByteArray read( int actualPointer, int length ) throws IOException {

		int startReadPointer = actualPointer - length;
		if ( startReadPointer < 0 ){
			startReadPointer = startReadPointer + ( MEMORY_SIZE * 2 );
		}
		int blockPointer = searchPointer( startReadPointer );
		int readLength = 0;
		int subStart = 0;

		ByteArray memoArray = new ByteArray(), hulpArray  = new ByteArray();

		while ( length > 0 ){
			hulpArray = ((MemoryInterval)(this.memoryList.get(blockPointer))).getMemory();

			if ( ( startReadPointer - ( blockPointer * 2 * BLOCK_SIZE ) ) < 0 ){
				readLength = ( startReadPointer - ( blockPointer * 2 * BLOCK_SIZE ) );
				subStart = 0;

				blockPointer--;
				startReadPointer = startReadPointer - readLength;

				if ( blockPointer < 0 ){
					blockPointer = ( (MEMORY_BLOCKS[0][1] - MEMORY_BLOCKS[0][0])/BLOCK_SIZE ) - 1;
					startReadPointer = startReadPointer + ( blockPointer + 0 ) * 2 * BLOCK_SIZE;
				}


			}

			else{
				readLength = length;
				subStart = ( ( startReadPointer ) - BLOCK_SIZE * 2 * blockPointer );
			}

			length = length - readLength;

			memoArray.add(hulpArray.sub( subStart , readLength ));
			memoArray = recDigitPower.pivot((memoArray.size() - readLength), memoArray);
		}

		return memoArray;

	}

	public int searchPointer(int point){
//		int virtPointer = ( point + 1 )/BLOCK_SIZE;
		int virtPointer = ( point ) / ( BLOCK_SIZE * 2 );
		return virtPointer;
	}

}