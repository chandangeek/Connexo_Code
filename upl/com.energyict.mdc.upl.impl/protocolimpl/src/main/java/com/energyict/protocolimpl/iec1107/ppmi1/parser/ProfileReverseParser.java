package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.Profile;

/** @author fbo */

public class ProfileReverseParser {

	boolean DBG = true;

	int dayNr = 0;
	int monthNr = 0;

	private boolean beginFound = false;
	int lastGoodIndex = 0;

	private int nrOfChannels;
	private int intervalLength;

	ByteAssembly byteAssembly = null;

	Assembler[] assemblerTable = new Assembler[256];

	private FFAssembler ffAssembler = new FFAssembler();
	private TerminalAssembler terminalAssembler = new TerminalAssembler();
	private DayAssembler dayAssembler = new DayAssembler();

	/** intervalLength in seconds */
	public ProfileReverseParser(Date currentMeterDate, int nrOfChannels, int intervalLength, TimeZone timeZone) {
		Calendar c = ProtocolUtils.getCalendar( timeZone );
		c.setTime(currentMeterDate);
		this.dayNr = c.get(Calendar.DAY_OF_MONTH);
		this.monthNr = c.get(Calendar.MONTH);
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
			this.byteAssembly.index = this.byteAssembly.getSize() - 1;
			int character = this.byteAssembly.get();

			while ((this.byteAssembly.index > 0) && !this.beginFound) {
				this.byteAssembly.index--;
				character = this.byteAssembly.get() & 0xFF;
				this.byteAssembly.push(new Byte((byte) character));
				this.assemblerTable[character].workOn(this.byteAssembly);
				if( this.DBG ) {
					System.out.println( this.byteAssembly );
				}
			}

			int length = this.ffAssembler.position16FF - this.lastGoodIndex;
			byte[] result = new byte[length];
			System.arraycopy(this.byteAssembly.input, this.lastGoodIndex, result, 0, length);
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
			System.out.println(this.byteAssembly.toString(this.byteAssembly.index - min));
		}
		if(this.DBG) {
			System.out.println(this.byteAssembly.toString(this.byteAssembly.index - max));
		}

		int index = this.byteAssembly.index - min;
		if ( (index > 0) && ((this.byteAssembly.input[index] & 0xFF) == 0xE4)) {
			this.byteAssembly.index -= min;
			if(this.DBG) {
				System.out.println(this.byteAssembly);
			}
			this.lastGoodIndex = this.byteAssembly.index;
			blockScan();
			return;
		}

		index = this.byteAssembly.index - max;
		if ((index > 0) && ((this.byteAssembly.input[index] & 0xFF) == 0xE4)) {
			this.byteAssembly.index -= max;
			if(this.DBG) {
				System.out.println(this.byteAssembly);
			}
			this.lastGoodIndex = this.byteAssembly.index;
			blockScan();
			return;
		}

		this.beginFound = true;
		this.byteAssembly.index = this.lastGoodIndex;
		int firstDay = (int) hex2dec(this.byteAssembly.input[this.byteAssembly.index + 1]);
		int firstMonth = (int) hex2dec(this.byteAssembly.input[this.byteAssembly.index + 2]);
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

		public void workOn(ByteAssembly ta) {
			if (this.ffCount == 15 ){
				this.position16FF = ta.index;
			} else {
				this.ffCount += 1;
			}
			((Byte) ta.pop()).byteValue();
		}
	}

	class DayAssembler implements Assembler {
		public void workOn(ByteAssembly ta) throws IOException {
			((Byte) ta.pop()).byteValue();
			int day = (int) hex2dec(ProfileReverseParser.this.byteAssembly.input[ProfileReverseParser.this.byteAssembly.index + 1]);
			int month = (int) hex2dec(ProfileReverseParser.this.byteAssembly.input[ProfileReverseParser.this.byteAssembly.index + 2]);
			if ((day == ProfileReverseParser.this.dayNr) && (month == ProfileReverseParser.this.monthNr + 1)) {
				System.out.println("DAY = " + day + " - " + month);
				ProfileReverseParser.this.lastGoodIndex = ProfileReverseParser.this.byteAssembly.index;
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