package com.energyict.protocolimpl.eig.nexus1272.parse;

import com.energyict.protocol.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Date;

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
		
		int century = ProtocolUtils.getVal(bais);
		int year = ProtocolUtils.getVal(bais);
		int month = ProtocolUtils.getVal(bais);
		int day = ProtocolUtils.getVal(bais);
		int hour = ProtocolUtils.getVal(bais);
		int minute = ProtocolUtils.getVal(bais);
		int second = ProtocolUtils.getVal(bais);
		int tenMilli = ProtocolUtils.getVal(bais);

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
		byte[] b = new byte[4];
		bais.read(b, 0, 4);
		int i = ProtocolUtils.getInt(b);
		BigDecimal bd = new BigDecimal(i).divide(new BigDecimal(65536), MathContext.DECIMAL128);
		return bd;
	}
	
	public BigDecimal parseF8() throws IOException {
		int val = (int) ProtocolUtils.getLong(bais, 2);
		if (val < 1000) 
			return new BigDecimal(val).divide(new BigDecimal(1000), MathContext.DECIMAL128);
		else if (val < 2000)
			return new BigDecimal(2).subtract(new BigDecimal(val).divide(new BigDecimal(1000), MathContext.DECIMAL128));
		else if (val < 3000)
			return new BigDecimal(val).divide(new BigDecimal(1000), MathContext.DECIMAL128).add(new BigDecimal(-2));
		else if (val < 4000)
			return new BigDecimal(4).subtract(new BigDecimal(val).divide(new BigDecimal(1000), MathContext.DECIMAL128));
		else 
			throw new IOException("Illegal value for average power factor, must be less than 4000, got " + val);
	}
	
	public BigDecimal parseF9() throws IOException {
		return new BigDecimal(ProtocolUtils.getShort(bais)).divide(new BigDecimal(100), MathContext.DECIMAL128);
	}
	
	public BigDecimal parseF10() throws IOException {
		return new BigDecimal(ProtocolUtils.getShort(bais)).divide(new BigDecimal(100), MathContext.DECIMAL128);
	}
	
	public long parseF18() throws IOException {
		return ProtocolUtils.getLong(bais, 4);
	}
	
	public long parseF20() throws IOException {
		return ProtocolUtils.getLong(bais, 8);
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
	
	public BigDecimal parseF64() throws IOException {
		return new BigDecimal(ProtocolUtils.getLong(bais, 4));
	}
	
	public int parseFourByteInt() throws IOException {
		return (int) ProtocolUtils.getLong(bais, 4);
	}
	
	public String parseSN() throws IOException {

		String ret = "";
		for (int i=0; i<4; i++) {
			ret += ProtocolUtils.buildStringHex(bais.read(), 2);
		}
		return ret.trim();
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
