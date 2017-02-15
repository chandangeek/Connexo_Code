/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.elster.opus;
/*
 * copy past of Medo Parsers object
 */
public class Parsers {
	protected char[] parseShortToChar(short s){
		// parses to a char array
		char[] c=new char[2];
		c[1]=(char) (((s & 0xFF00)>>8)& 0xFF);
		c[0]=(char) (s & 0xFF);
		return c;
	}

	protected short parseCharToShort(char[] c){
		c[0]&=0x00FF;
		c[1]&=0x00FF;
		return (short) ((c[1]<<8) | c[0]);
	}
	protected char[] parseIntToChar(int i){
		char[] c=new char[4];
		c[3]=(char) (((i & 0xFF000000)>>24)& 0xFF);
		c[2]=(char) (((i & 0xFF0000)>>16)& 0xFF);
		c[1]=(char) (((i & 0xFF00)>>8)& 0xFF);
		c[0]=(char) (i & 0xFF);
		return c;
	}
	protected int parseCharToInt(char[] c){
		c[0]&=0x00FF;
		c[1]&=0x00FF;
		c[2]&=0x00FF;
		c[3]&=0x00FF;
		return (int) ((c[3]<<24) | (c[2]<<16) | (c[1]<<8) | c[0]);
	}
	protected long parseCharToLong(char[] c){
		// gets a 4 byte array
		return ((long) (parseCharToInt(c)) & 0x00000000FFFFFFFF);
	}
	protected char[] parseLongToChar(long l){
		l&=0x00000000FFFFFFFF;
		return parseIntToChar((int)l);
	}
	protected byte[] parseCArraytoBArray(char[] charArray) {
		byte[] b=new byte[charArray.length];
		for(int i=0; i<charArray.length; i++){
			b[i]=(byte) charArray[i];
		}
		return b;
	}
	protected char[] parseBArraytoCArray(byte[] byteArray) {
		char[] c = new char[byteArray.length];
		for(int i=0; i<byteArray.length; i++){
			c[i] = (char) byteArray[i];
		}
		return c;
	}
	protected String NumberToString(byte b){
		// byte is seen as unsigned 8 bit integer
		char c= (char) b ;
		return NumberToString(c);
	}
	protected String NumberToString(char c){
		c&=0x00FF; // mask
		return ""+ (int) c;
	}
	protected String NumberToString(short s){
		// makes negative numbers positive (might change later) based upon observations in Status-RamTop
		String string=""+(int)((char) s);
		return string;
	}
	protected String NumberToString(int i){
		String string=""+i;
		return string;
	}
	protected String NumberToString(long l){
		String string=""+l;
		return string;
	}

}
