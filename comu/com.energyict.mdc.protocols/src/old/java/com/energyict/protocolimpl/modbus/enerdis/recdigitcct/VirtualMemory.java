/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.modbus.enerdis.recdigitcct;

import com.energyict.mdc.protocol.api.UnsupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMemory {

	public List memoryList = new ArrayList();

	private boolean debug = false;
	private final int STARTADD = 3;

	private final RecDigitCct recDigitCct;

	/**
	 * @param recDigitCct
	 * @throws IOException
	 * @throws UnsupportedException
	 */
	VirtualMemory(RecDigitCct recDigitCct) {
		this.recDigitCct = recDigitCct;

	}

    final static int MEMORY_BLOCKS [][] =
    {
     {0x00E7, 0x00E8, 0x0272, 0x0273, 0x21F3, 0x00E9, 0x00ED, 0x00F1 },
     {0x00F8, 0x00F9, 0x21F3, 0x21F4, 0x4174, 0x00FA, 0x00FE, 0x0102 },
     {0x0109, 0x010A, 0x4174, 0x4175, 0x60F5, 0x010B, 0x010F, 0x0113 },
     {0x011A, 0x011B, 0x60F5, 0x60F6, 0x8076, 0x011C, 0x0120, 0x0124 },
     {0x012B, 0x012C, 0x8076, 0x8077, 0x9FF7, 0x012D, 0x0131, 0x0135 },
     {0x013C, 0x013D, 0x9FF7, 0x9FF8, 0xBF78, 0x013E, 0x0142, 0x0146 },
     {0x014D, 0x014E, 0xBF78, 0xBF79, 0xDEF9, 0x014F, 0x0153, 0x0157 },
     {0x015E, 0x015F, 0xDEF9, 0xDEFA, 0xFE7A, 0x0160, 0x0164, 0x0168 }
    };

    final static int MEMORY_SIZE = MEMORY_BLOCKS[0][4] - MEMORY_BLOCKS[0][3];

    public final int BLOCK_SIZE = 0x60;

	void initMemory( int type ) throws UnsupportedException, IOException{

		int step = BLOCK_SIZE;
		int stt = MEMORY_BLOCKS[type][STARTADD];
		int stp;

		for(int i = 0; i < MEMORY_SIZE/BLOCK_SIZE; i++ ){
			stp = stt + step;
			MemoryInterval memInterval = new MemoryInterval( this.recDigitCct, stt,stp );
			memoryList.add( memInterval );
			stt = stp;
		}

		if ( debug ){
			System.out.println("initMemory Test!");
		}
	}

	ByteArray read( int actualPointer, int length ) throws IOException {

		int startReadPointer = actualPointer + 2;
		if ( startReadPointer == MEMORY_SIZE ){
			startReadPointer = 0;
		}
		int blockPointer = searchPointer( startReadPointer );
		int readLength = 0;
		int subStart = 0;

		ByteArray memoArray = new ByteArray(), hulpArray  = new ByteArray();

		while ( length > 0 ){

			hulpArray = ((MemoryInterval)(this.memoryList.get(blockPointer))).getMemory();

			readLength = length;
			subStart = ( ( startReadPointer ) - BLOCK_SIZE * blockPointer ) * 2;

			length = length - readLength;

			memoArray.add(hulpArray.sub( subStart , readLength ));
			memoArray = recDigitCct.pivot((memoArray.size() - readLength), memoArray);
		}

		return memoArray;

	}

	public int searchPointer(int point){
		int virtPointer = point / BLOCK_SIZE ;
		return virtPointer;
	}

}