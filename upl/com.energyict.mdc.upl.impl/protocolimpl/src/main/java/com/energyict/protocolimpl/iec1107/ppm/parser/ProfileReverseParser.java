package com.energyict.protocolimpl.iec1107.ppm.parser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppm.Profile;

/** @author fbo */

public class ProfileReverseParser {

	private boolean DBG = false;

	// Date currentMeterDate = null; // KV 22072005 unused code
	private int dayNr = 0;
	private int monthNr = 0;
	//TimeZone timeZone = null; // KV 22072005 unused code

	private boolean beginFound = false;
	private int lastGoodIndex = 0;
	// int firstDay = 0; // KV 22072005 unused code
	// int firstMonth = 0;// KV 22072005 unused code

	//private int dayMinByteSize;// KV 22072005 unused code
	//private int dayMaxByteSize;// KV 22072005 unused code

	private int nrOfChannels;
	private int intervalLength;

	private ByteAssembly byteAssembly = null;

	private Assembler[] assemblerTable = new Assembler[256];

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
		this.dayNr = c.get(Calendar.DAY_OF_MONTH);
		this.monthNr = c.get(Calendar.MONTH);
		//this.timeZone = timeZone;// KV 22072005 unused code

		this.nrOfChannels = nrOfChannels;
		this.intervalLength = intervalLength;

		setAssemblerTable(0xE4, 0xE4, this.dayAssembler);
		setAssemblerTable(0x00, 0xE3, this.terminalAssembler);
		setAssemblerTable(0xE5, 0xFE, this.terminalAssembler);
		setAssemblerTable(0xFF, 0xFF, this.ffAssembler);
	}

	public void setInput(byte[] b) {
		this.byteAssembly = new ByteAssembly();
		this.byteAssembly.setInput(b);
	}

	public byte[] match() throws IOException {

		try {
			this.byteAssembly.setIndex(this.byteAssembly.getSize() - 1);
			int character = this.byteAssembly.get();

			while ((this.byteAssembly.getIndex() > 0) && !this.beginFound) {
				this.byteAssembly.addToIndex(-1);
				character = this.byteAssembly.get() & 0xFF;
				this.byteAssembly.push(new Byte((byte) character));
				this.assemblerTable[character].workOn(this.byteAssembly);
				if( this.DBG ) {
					System.out.println( this.byteAssembly );
				}
			}

			int length = this.ffAssembler.position16FF - this.lastGoodIndex;
			byte[] result = new byte[length];
			System.arraycopy(this.byteAssembly.getInput(), this.lastGoodIndex, result, 0, length);
			return result;
		} catch( Exception ex ){
			ex.printStackTrace();
			if( this.DBG ) {
				System.out.println( this.byteAssembly );
			}
			throw new IOException( ex.getMessage() );
		}
	}

	private void setAssemblerTable(int from, int to, Assembler assembler) {
		for (int i = from; i <= to; i++) {
			if ((i >= 0) && (i < this.assemblerTable.length)) {
				this.assemblerTable[i] = assembler;
			}
		}
	}

	public void blockScan() {
		int min = (int) Profile.dayByteSizeMin(this.nrOfChannels, this.intervalLength) / 2;
		int max = (int) Profile.dayByteSizeMax(this.nrOfChannels, this.intervalLength) / 2;

		if(this.DBG) {
			System.out.println(this.byteAssembly.toString(this.byteAssembly.getIndex() - min));
		}
		if(this.DBG) {
			System.out.println(this.byteAssembly.toString(this.byteAssembly.getIndex() - max));
		}

		int index = this.byteAssembly.getIndex() - min;
		if ( (index > 0) && ((this.byteAssembly.getInput()[index] & 0xFF) == 0xE4)) {
			this.byteAssembly.addToIndex(-min);
			if(this.DBG) {
				System.out.println(this.byteAssembly);
			}
			this.lastGoodIndex = this.byteAssembly.getIndex();
			blockScan();
			return;
		}

		index = this.byteAssembly.getIndex() - max;
		if ((index > 0) && ((this.byteAssembly.getInput()[index] & 0xFF) == 0xE4)) {
			this.byteAssembly.addToIndex(-max);
			if(this.DBG) {
				System.out.println(this.byteAssembly);
			}
			this.lastGoodIndex = this.byteAssembly.getIndex();
			blockScan();
			return;
		}

		this.beginFound = true;
		this.byteAssembly.setIndex(this.lastGoodIndex);
		int firstDay = (int) hex2dec(this.byteAssembly.getInput()[this.byteAssembly.getIndex() + 1]);
		int firstMonth = (int) hex2dec(this.byteAssembly.getInput()[this.byteAssembly.getIndex() + 2]);
		if(this.DBG) {
			System.out.println( "FirstDay = " + firstDay + "/" + firstMonth);
		}
		if(this.DBG) {
			System.out.println(this.byteAssembly);
		}
	}

	public static long hex2dec(byte value) {
		return Long.parseLong(Long.toHexString(value & 0xFF));
	}

	public interface Assembler {
		abstract void workOn(ByteAssembly a) throws IOException;
	}

	class FFAssembler implements Assembler {
		int ffCount = 0;
		int position16FF = 0;
		//boolean past16Fs = false;// KV 22072005 unused code

		public void workOn(ByteAssembly ta) {
			if (this.ffCount == 15 ){
				//past16Fs = true;// KV 22072005 unused code
				this.position16FF = ta.getIndex();
			} else {
				this.ffCount += 1;
			}
			((Byte) ta.pop()).byteValue();
		}
	}

	class DayAssembler implements Assembler {

		//boolean foundCurrentDay = false;// KV 22072005 unused code

		public void workOn(ByteAssembly ta) throws IOException {

			((Byte) ta.pop()).byteValue();

			int day = (int) hex2dec(ProfileReverseParser.this.byteAssembly.getInput()[ProfileReverseParser.this.byteAssembly.getIndex() + 1]);
			int month = (int) hex2dec(ProfileReverseParser.this.byteAssembly.getInput()[ProfileReverseParser.this.byteAssembly.getIndex() + 2]);

			if ((day == ProfileReverseParser.this.dayNr) && (month == ProfileReverseParser.this.monthNr + 1)) {
				//foundCurrentDay = true; // KV 22072005 unused code
				System.out.println("DAY = " + day + " - " + month);
				// firstDay = day; // KV 22072005 unused code
				//firstMonth = month;// KV 22072005 unused code
				ProfileReverseParser.this.lastGoodIndex = ProfileReverseParser.this.byteAssembly.getIndex();
				blockScan();
			}
		}
	}

	class TerminalAssembler implements Assembler {

		public void workOn(ByteAssembly ta) {
			ta.pop();
			ProfileReverseParser.this.ffAssembler.ffCount = 0;
		}
	}

}