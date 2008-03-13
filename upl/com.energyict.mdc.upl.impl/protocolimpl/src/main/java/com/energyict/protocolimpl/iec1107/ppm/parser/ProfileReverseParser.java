package com.energyict.protocolimpl.iec1107.ppm.parser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppm.Profile;

/** @author fbo */

public class ProfileReverseParser {

    boolean DBG = false;
    
	// Date currentMeterDate = null; // KV 22072005 unused code
	int dayNr = 0;
	int monthNr = 0;
	//TimeZone timeZone = null; // KV 22072005 unused code
	
	private boolean beginFound = false;
	int lastGoodIndex = 0;
	// int firstDay = 0; // KV 22072005 unused code
	// int firstMonth = 0;// KV 22072005 unused code

	//private int dayMinByteSize;// KV 22072005 unused code
	//private int dayMaxByteSize;// KV 22072005 unused code

	private int nrOfChannels;
	private int intervalLength;

	ByteAssembly byteAssembly = null;

	Assembler[] assemblerTable = new Assembler[256];

	private FFAssembler ffAssembler = new FFAssembler();
	private TerminalAssembler terminalAssembler = new TerminalAssembler();
	private DayAssembler dayAssembler = new DayAssembler();

    /** intervalLength in seconds */
	public ProfileReverseParser(
                    Date currentMeterDate, int nrOfChannels,
                    int intervalLength, TimeZone timeZone ) {

		// this.currentMeterDate = currentMeterDate; // KV 22072005 unused code
		Calendar c = ProtocolUtils.getCalendar( timeZone );
		c.setTime(currentMeterDate);
		dayNr = c.get(Calendar.DAY_OF_MONTH);
		monthNr = c.get(Calendar.MONTH);
		//this.timeZone = timeZone;// KV 22072005 unused code
		
		this.nrOfChannels = nrOfChannels;
		this.intervalLength = intervalLength;

		setAssemblerTable(0xE4, 0xE4, dayAssembler);
		setAssemblerTable(0x00, 0xE3, terminalAssembler);
		setAssemblerTable(0xE5, 0xFE, terminalAssembler);
		setAssemblerTable(0xFF, 0xFF, ffAssembler);
	}

	public void setInput(byte[] b) {
		byteAssembly = new ByteAssembly();
		byteAssembly.setInput(b);
	}

	public byte[] match() throws IOException {

		try {
		byteAssembly.index = byteAssembly.getSize() - 1;
		int character = byteAssembly.get();

		while (byteAssembly.index > 0 && !beginFound) {
			byteAssembly.index--;
			character = byteAssembly.get() & 0xFF;
			byteAssembly.push(new Byte((byte) character));
			assemblerTable[character].workOn(byteAssembly);
			if( DBG ) System.out.println( byteAssembly );
		}

		int length = ffAssembler.position16FF - lastGoodIndex;
		byte[] result = new byte[length];
		System.arraycopy(byteAssembly.input, lastGoodIndex, result, 0, length);
		return result;
		} catch( Exception ex ){
			ex.printStackTrace();
			if( DBG ) System.out.println( byteAssembly );
			throw new IOException( ex.getMessage() );
		}
	}

	private void setAssemblerTable(int from, int to, Assembler assembler) {
		for (int i = from; i <= to; i++) {
			if (i >= 0 && i < assemblerTable.length) {
				assemblerTable[i] = assembler;
			}
		}
	}

	public void blockScan() {
		int min = (int) Profile.dayByteSizeMin(nrOfChannels, intervalLength) / 2;
		int max = (int) Profile.dayByteSizeMax(nrOfChannels, intervalLength) / 2;
		
		if(DBG) System.out.println(byteAssembly.toString(byteAssembly.index - min));
		if(DBG) System.out.println(byteAssembly.toString(byteAssembly.index - max));

		int index = byteAssembly.index - min;
		if ( index > 0 && (byteAssembly.input[index] & 0xFF) == 0xE4) {
			byteAssembly.index -= min;
			if(DBG)  System.out.println(byteAssembly);
			lastGoodIndex = byteAssembly.index;
			blockScan();
			return;
		}
		
		index = byteAssembly.index - max;
		if (index > 0 && (byteAssembly.input[index] & 0xFF) == 0xE4) {
			byteAssembly.index -= max;
			if(DBG) System.out.println(byteAssembly);
			lastGoodIndex = byteAssembly.index;
			blockScan();
			return;
		}

		beginFound = true;
		byteAssembly.index = lastGoodIndex;
		int firstDay = (int) hex2dec(byteAssembly.input[byteAssembly.index + 1]);
		int firstMonth = (int) hex2dec(byteAssembly.input[byteAssembly.index + 2]);
		if(DBG) System.out.println( "FirstDay = " + firstDay + "/" + firstMonth);
		if(DBG) System.out.println(byteAssembly);
	}

	public static long hex2dec(byte value) {
		return Long.parseLong(Long.toHexString(value & 0xFF));
	}

	public interface Assembler {
		public abstract void workOn(ByteAssembly a) throws IOException;
	}

	class FFAssembler implements Assembler {
		int ffCount = 0;
		int position16FF = 0;
		//boolean past16Fs = false;// KV 22072005 unused code

		public void workOn(ByteAssembly ta) {
			if (ffCount == 15 ){
				//past16Fs = true;// KV 22072005 unused code
				position16FF = ta.index;				
			} else {
				ffCount += 1;
			}
			((Byte) ta.pop()).byteValue();
		}
	}

	class DayAssembler implements Assembler {

		//boolean foundCurrentDay = false;// KV 22072005 unused code

		public void workOn(ByteAssembly ta) throws IOException {

			((Byte) ta.pop()).byteValue();

			int day = (int) hex2dec(byteAssembly.input[byteAssembly.index + 1]);
			int month = (int) hex2dec(byteAssembly.input[byteAssembly.index + 2]);

			if (day == dayNr && month == monthNr + 1) {
				//foundCurrentDay = true; // KV 22072005 unused code
				System.out.println("DAY = " + day + " - " + month);
				// firstDay = day; // KV 22072005 unused code
				//firstMonth = month;// KV 22072005 unused code
				lastGoodIndex = byteAssembly.index;
				blockScan();
			}
		}
	}

	class TerminalAssembler implements Assembler {

		public void workOn(ByteAssembly ta) {
			ta.pop();
			ffAssembler.ffCount = 0;
		}
	}

}