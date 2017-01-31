/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.modbus.enerdis.recdigit1800;

import com.energyict.mdc.protocol.api.UnsupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMemory {

	public List memoryList = new ArrayList();

	private final RecDigit1800 recDigit1800;

	/**
	 * @param recDigit1800
	 * @throws IOException
	 * @throws UnsupportedException
	 */
	VirtualMemory(RecDigit1800 recDigit1800) {
		this.recDigit1800 = recDigit1800;

	}
    final int MEMORY_BLOCKS [][] =
	{
		{0x03FE, 0x16BE},
		{0x2000, 0x32C0},
	};

    final int MEMORY_SIZE = MEMORY_BLOCKS[0][1] - MEMORY_BLOCKS[0][0];

    public final int BLOCK_SIZE = 0x64;

	void initMemory( int type ) throws UnsupportedException, IOException{

		int step = BLOCK_SIZE;
		int stt = MEMORY_BLOCKS[type][0];
		int stp;

		for(int i = 0; i < ( MEMORY_BLOCKS[type][1] - MEMORY_BLOCKS[type][0])/BLOCK_SIZE; i++ ){
			stp = stt + step;
			MemoryInterval memInterval = new MemoryInterval( this.recDigit1800, stt,stp );
			memoryList.add( memInterval );
			stt = stp;
		}
	}

	ByteArray read( int actualPointer, int length ) throws IOException {

		int blockPointer = searchPointer( actualPointer );
		int startReadPointer = (actualPointer + 1)*4;
		int readLength = 0;
		int subStart = 0;

		ByteArray memoArray = new ByteArray(), hulpArray  = new ByteArray();

		while ( length > 0 ){
			hulpArray = ((MemoryInterval)(this.memoryList.get(blockPointer))).getMemory();

			if ( ( startReadPointer - ( blockPointer*BLOCK_SIZE ) ) < length ){
				readLength = ( startReadPointer - ( blockPointer*BLOCK_SIZE ) );
				subStart = 0;

				blockPointer--;
				startReadPointer = startReadPointer - readLength;

				if ( blockPointer < 0 ){
					blockPointer = ( (MEMORY_BLOCKS[0][1] - MEMORY_BLOCKS[0][0])/BLOCK_SIZE ) - 1;
					startReadPointer = startReadPointer + blockPointer*BLOCK_SIZE;
				}
			}

			else{
				readLength = length;
				subStart = ( startReadPointer - readLength ) - BLOCK_SIZE * blockPointer;
			}

			length = length - readLength;

			memoArray.add(hulpArray.sub( subStart , readLength ));
			memoArray = recDigit1800.pivot((memoArray.size() - readLength), memoArray);
		}

		return memoArray;

	}

	public int searchPointer(int point){
		int virtPointer = point*4/BLOCK_SIZE;
		return virtPointer;
	}

}