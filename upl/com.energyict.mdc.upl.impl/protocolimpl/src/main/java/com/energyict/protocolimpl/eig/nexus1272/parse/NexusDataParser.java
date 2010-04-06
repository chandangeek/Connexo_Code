package com.energyict.protocolimpl.eig.nexus1272.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

public class NexusDataParser {

	protected ByteArrayInputStream bais;
	
	public NexusDataParser (byte[] in) {
		bais = new ByteArrayInputStream(in);
	}
	
	public boolean isEmpty() {
		return bais.available() <= 0;
	}
	
	public String parseF2() {
		String ret = "";
		for (int i=0; i<4; i++) {
			ret += (char)bais.read();
		}
		return ret.trim();
	}
	
	public Date parseF3() throws IOException {
		//TODO CLEAN UP
		
		int century = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int year = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int month = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int day = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int hour = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int minute = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int second = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int tenMilli = ProtocolUtils.getVal(bais);//ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();

		//TODO Use TZ from RMR tab?
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, century*100+year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, tenMilli*10);

		return cal.getTime();
	}
	
	public BigDecimal parseF7() {
		return null;
	}
	
	public BigDecimal parseF8() {
		return null;
	}
	
	public BigDecimal parseF9() {
		return null;
	}
	
	public BigDecimal parseF10() {
		return null;
	}
	
	public long parseF18() throws IOException {
		return ProtocolUtils.getLong(bais, 4);
	}
	
	public long parseF20() {
		return 0;
	}
	
	public int parseF43() {
		return 0;
	}
	
	public int parseF51() throws IOException {
		return ProtocolUtils.getShort(bais);
	}
	
	public int parseF58() {
		return 0;
	}
	
	public BigDecimal parseF64() {
		return null;
	}
	
	
	int recSize;
	private void parseLimitLog(byte[] limitTriggerLogData, byte[] limitSnapshotLogData) throws IOException {
		int offset = 0;
		int length = 8;
		int recNum = 0;
		int offset1 = 0;
		//		int recSize = 16;

		int dataPointersStart = 45332;
		int dataPointersEnd = 45460;

		//		byte[] test = intToByteArray(dataPointersStart);
		//		byte addrHigh = test[0];
		//		byte addrLow = test[1];
		//		int len = dataPointersEnd - dataPointersStart -1;
		//		byte[] send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		//		outputStream.write(send);
		//		byte[] byteArray = connection.receiveResponse().toByteArray();
		//		byte[] ba2 = new byte[byteArray.length - 9]; 
		//		System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
		//
		//		List <LinePointMap> lpMap = processPointers(ba2);
		try {
			while (offset1 < limitSnapshotLogData.length) {
				Date recDate = parseF3(limitTriggerLogData, offset);
				Date recDate2 = parseF3(limitSnapshotLogData, offset1);
				String event = recDate + "\t" + recDate2;
				offset+= length;
				System.out.println(event);
				//			for (LinePointMap lp : lpMap) {
				//				int val = parseF64(ba, offset);
				//				offset+=4;
				//				System.out.println("\t"+ lp.line + "." + lp.point + " - " + lp.getDescription() + "\t" + val);
				//			}

				recNum++;
				offset1 = recNum * recSize;
				offset = recNum * 32;
			}
		}catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private Date parseF3(byte[] byteArray) throws IOException {
		return (parseF3(byteArray, 9));
	}

	private Date parseF3(byte[] byteArray, int offset) throws IOException {
		int century = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int year = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int month = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int day = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int hour = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int minute = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int second = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int tenMilli = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();

		//TODO Use TZ from RMR tab?
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, century*100+year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, tenMilli*10);

		return cal.getTime();
	}

	private String parseF2(byte[] byteArray) throws IOException {
		return parseF2(byteArray, 9, byteArray.length);
	}

	private String parseF2(byte[] byteArray, int offset, int len) throws IOException {
		String ret = "";
		for (int i=offset; i<len; i++) {
			ret += (char)byteArray[i];
		}
		return ret.trim();
	}

	public String parseSN() throws IOException {

		String ret = "";
		for (int i=0; i<4; i++) {
			ret += ProtocolUtils.buildStringHex(bais.read(), 2);
		}
		return ret.trim();
	}

	private long parseF18(InputStream dataInStream, int len) throws IOException {
		dataInStream.skip(9);
		long val = 	ProtocolUtils.getLong((ByteArrayInputStream)dataInStream, len-9);
		return val;

	}

	private long parseF18(byte[] bArray, int offset, int len) throws IOException {
		long val = ProtocolUtils.getLong(bArray, offset, len);
		return val;

	}

	private int parseF51(byte[] bArray, int offset, int len) throws IOException {
		int val = ProtocolUtils.getInt(bArray, offset, len);
		return val;

	}

	private int parseF64(byte[] bArray, int offset) throws IOException {
		return parseF64(bArray, offset, 4);
	}
	private int parseF64(byte[] bArray, int offset, int len) throws IOException {
		int val = ProtocolUtils.getInt(bArray, offset, len);
		return val;

	}

	
	private void parseHistorical2Log(byte[] ba) throws IOException {
		int offset = 0;
		int length = 8;
		int recNum = 0;
		//		int recSize = 16;

		int dataPointersStart = 45332;
		int dataPointersEnd = 45460;

		byte[] test = intToByteArray(dataPointersStart);
		byte addrHigh = test[0];
		byte addrLow = test[1];
		int len = dataPointersEnd - dataPointersStart -1;
		byte[] send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		//outputStream.write(send);
		byte[] byteArray = new byte []{};//connection.receiveResponse().toByteArray();
		byte[] ba2 = new byte[byteArray.length - 9]; 
		System.arraycopy(byteArray, 9, ba2, 0, ba2.length);

		List <LinePoint> lpMap = processPointers(ba2);

		while (offset < ba.length) {
			Date recDate = parseF3(ba, offset);
			String event = recDate + "";
			offset+= length;
			System.out.println(recDate);
			for (LinePoint lp : lpMap) {
				int val = parseF64(ba, offset);
				offset+=4;
				System.out.println("\t"+ lp.getLine() + "." + lp.getPoint() + " - " + lp.getDescription() + "\t" + val);
			}

			recNum++;
			offset = recNum * recSize;
		}
	}

	private List<LinePoint> processPointers(byte[] ba) throws IOException {
		int offset = 0;
		List <LinePoint> lpMap = new ArrayList <LinePoint> ();
		//		for (int i = 0; i<4; i++){
		while (offset <= ba.length-4) {	
			if (ba[offset] == -1 && ba[offset+1] == -1) {
				offset += 4;
				continue;
			}
			int line = ProtocolUtils.getInt(ba, offset, 2);
			offset+=2;
			int point = ProtocolUtils.getInt(ba, offset, 1);
			offset+=2;
			lpMap.add(new LinePoint(line, point));
		}
		return lpMap;
	}





	public static final byte POWER = 0x000;
	public static final byte PASSWORD = 0x001;
	public static final byte CHANGE_PROGRAMMABLE_SETTINGS = 0x002;
	public static final byte CHANGE_FIRMWARE= 0x003;
	public static final byte CHANGE_TIME = 0x004;
	public static final byte TEST_MODE = 0x005;
	public static final byte LOG_DOWNLOAD = 0x006;
	public static final byte FEATURE_RESET = 0x007;
	private void parseSystemLog(byte[] ba) throws IOException {

		int offset = 0;
		int length = 8;
		int recNum = 0;
		int recSize = 16;

		while (offset < ba.length) {
			Date recDate = parseF3(ba, offset);
			String event = recDate + "";
			offset+= length;
			byte code = ba[offset++];
			byte subcode1;
			byte subcode2;
			byte subcode3;
			switch (code) {
			case POWER:
				event += " => POWER";
				subcode1 = ba[offset++];
				switch (subcode1) {
				case 0x00:
					event += " : Power was lost";
					break;
				case 0x01:
					event += " : Normal operation was restored";
					break;
				default:
					event += " : Undefined";
					break;
				}
				break;

			case PASSWORD:
				event += " => PASSWORD";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x00:
					event += " : Password Protection was Enabled";
					break;
				case 0x01:
					event += " : Password Protection was Disabled";
					break;
				case 0x002:
					event += " : The Level 1 Password was changed";
					break;
				case 0x003:
					event += " : The Level 2 Password was changed";
					break;
				case 0x004:
					event += " : Level 1 access was granted";
					break;
				case 0x005:
					event += " : Level 2 access was granted";
					break;
				case 0x006:
					event += " : An invalid password was supplied";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			case CHANGE_PROGRAMMABLE_SETTINGS:
				event += " => CHANGE PROGRAMMABLE SETTINGS";
				break;
			case CHANGE_FIRMWARE:
				event += " => CHANGE FIRMWARE";
				subcode1 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : Comm Run Time";
					break;
				case 0x001:
					event += " : DSP Run Time";
					break;
				default:
					event += " : Undefined";
					break;
				}
				String version = parseF2(ba, offset, 4);
				event += " : Old version - " + version;
				break;
			case CHANGE_TIME:
				event += " => CHANGE TIME";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : Old Time - The time stamp is the old time of the meter";
					break;
				case 0x001:
					event += " : New Time - The time stamp is the new time of the meter";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			case TEST_MODE:
				event += " => TEST MODE";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x001:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC";
					break;
				case 0x002:
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = TLC";
					break;
				case 0x003:
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = TLC";
					break;
				case 0x004:
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = TLC";
					break;
				case 0x005:
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = TLC";
					break;
				case 0x006:
					event += " : Action = Block Average Test  Test Mode = TLC";
					break;
				case 0x007:
					event += " : Action = Rolling Average Test  Test Mode = TLC";
					break;
				case 0x008:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC";
					break;
				case 0x009:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT";
					break;
				case 0x00A:
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = TLC & CTPT";
					break;
				case 0x00B:
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = TLC & CTPT";
					break;
				case 0x00C:
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = TLC & CTPT";
					break;
				case 0x00D:
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = TLC & CTPT";
					break;
				case 0x00E:
					event += " : Action = Block Average Test  Test Mode = TLC & CTPT";
					break;
				case 0x00F:
					event += " : Action = Rolling Average Test  Test Mode = TLC & CTPT";
					break;
				case 0x010:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT";
					break;
				case 0x011:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated";
					break;
				case 0x012:
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = Uncompensated";
					break;
				case 0x013:
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = Uncompensated";
					break;
				case 0x014:
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = Uncompensated";
					break;
				case 0x015:
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = Uncompensated";
					break;
				case 0x016:
					event += " : Action = Block Average Test  Test Mode = Uncompensated";
					break;
				case 0x017:
					event += " : Action = Rolling Average Test  Test Mode = Uncompensated";
					break;
				case 0x018:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated";
					break;
				case 0x019:
					event += " : Action = Wh Test (Del & Rcv)   Test Mode = CTPT";
					break;
				case 0x01A:
					event += " : Action = VARh Test (Q1 & Q2)   Test Mode = CTPT";
					break;
				case 0x01B:
					event += " : Action = VARh Test (Q3 & Q4)   Test Mode = CTPT";
					break;
				case 0x01C:
					event += " : Action = VAh Test (Q1 & Q4)   Test Mode = CTPT";
					break;
				case 0x01D:
					event += " : Action = VAh Test (Q2 & Q3)   Test Mode = CTPT";
					break;
				case 0x01E:
					event += " : Action = Block Average Test   Test Mode = CTPT";
					break;
				case 0x01F:
					event += " : Action = Rolling Average Test   Test Mode = CTPT";
					break;
				case 0x020:
					event += " : Action = Wh Test (Del & Rcv)   Test Mode = CTPT";
					break;
				default:
					event += " : Undefined";
					break;
				}

			case LOG_DOWNLOAD:
				event += " => LOG DOWNLOAD";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				subcode3 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : Download Started, Log records while downloading";
					break;
				case 0x001:
					event += " : Download Started, Log Paused while downloading";
					break;
				case 0x002:
					event += " : Download Ended";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Historical Log 1";
					break;
				case 0x001:
					event += " : Historical Log 2";
					break;
				case 0x002:
					event += " : Sequence of Events State Log";
					break;
				case 0x003:
					event += " : Sequence of Events Snapshot Log";
					break;
				case 0x004:
					event += " : Digital Input State Log";
					break;
				case 0x005:
					event += " : Digital Input Snapshot Log";
					break;
				case 0x006:
					event += " : Digital Output State Log";
					break;
				case 0x007:
					event += " : Digital Output Snapshot Log";
					break;
				case 0x008:
					event += " : Flicker Log";
					break;
				case 0x009:
					event += " : Waveform Trigger Log";
					break;
				case 0x00A:
					event += " : System Event Log";
					break;
				case 0x00B:
					event += " : Waveform Sample Log";
					break;
				case 0x00C:
					event += " : PQ Log";
					break;
				case 0x00D:
					event += " : Reset Log";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode3) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			case FEATURE_RESET:
				event += " => FEATURE RESET";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : All Logs Reset";
					break;
				case 0x001:
					event += " : Maximum Reset";
					break;
				case 0x002:
					event += " : Minimum Reset";
					break;
				case 0x003:
					event += " : Energy Reset";
					break;
				case 0x004:
					event += " : Time of Use Current Month";
					break;
				case 0x005:
					event += " : Internal Input Accumulations and Aggregations";
					break;
				case 0x006:
					event += " : KYZ Output Accumulations";
					break;
				case 0x007:
					event += " : Cumulative Demand";
					break;
				case 0x008:
					event += " : Historical Log 1 Reset";
					break;
				case 0x009:
					event += " : Historical Log 2 Reset";
					break;
				case 0x00A:
					event += " : Sequence of Events Log Reset";
					break;
				case 0x00B:
					event += " : Digital Input Log Reset";
					break;
				case 0x00C:
					event += " : Digital Output Log Reset";
					break;
				case 0x00D:
					event += " : Flicker Log Reset";
					break;
				case 0x00E:
					event += " : Waveform Log Reset";
					break;
				case 0x00F:
					event += " : PQ Log Reset";
					break;
				case 0x010:
					event += " : System Event Log Reset";
					break;
				case 0x011:
					event += " : Total Average Power Factor Reset";
					break;
				case 0x012:
					event += " : Time of Use Active Registers";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			default:
				event += " => UNDEFINED";
				break;
			}

			System.out.println(event);
			recNum++;
			offset = recNum * recSize;
		}

	}

	private void testParse(byte[] byteArray) throws IOException {
		int offset = 0;
		int length = 4;
		long memsize = parseF18(byteArray, offset, length);
		offset += length;
		length = 2;
		int recSize = parseF51(byteArray, offset, length);
		offset += length;
		int firstIndex = parseF51(byteArray, offset, length);
		offset += length;
		int lastIndex = parseF51(byteArray, offset, length);
		offset += length;
		length = 8;
		Date firstTimeStamp = parseF3(byteArray, offset);
		offset += length;
		Date lastTimeStamp = parseF3(byteArray, offset);
		offset += length;
		length = 8;
		long validBitmap = parseF18(byteArray, offset, length);
		offset += length;
		length = 2;
		int maxRecords = parseF51(byteArray, offset, length);
		"".toCharArray();
	}
	
	public static byte[] intToByteArray(int value) {
		byte[] b = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
}
