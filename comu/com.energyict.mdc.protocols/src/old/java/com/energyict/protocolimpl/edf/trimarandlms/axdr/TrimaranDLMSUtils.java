/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DLMSUtils.java
 *
 * Created on 17 januari 2003, 15:55
 */

package com.energyict.protocolimpl.edf.trimarandlms.axdr;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class TrimaranDLMSUtils implements TrimaranDLMSCOSEMGlobals {

	/** Hide constructor to prevent creation of new instance of TrimaranDLMSUtils */
	private TrimaranDLMSUtils() {}

	public static byte attrSN2LN(short snAttr) {
		return attrSN2LN(snAttr & 0xFFFF);
	}
	public static byte attrSN2LN(int snAttr) {
		return (byte)(snAttr/8+1);
	}
	public static int attrLN2SN(int lnAttr) {
		return (lnAttr-1)*8;
	}

	public static int getAXDRLengthOffset(byte[] byteBuffer,int iOffset) {
		// Trimaran+ uses another way of encoding the length of an A-XDR (BER??) encoded ASN.1 notation
		return ((byteBuffer[iOffset] & (byte)0x80) != 0) ? 2 : 1 ;
	}

	public static long getAXDRLength(byte[] byteBuffer,int iOffset) {
		// Trimaran+ uses another way of encoding the length of an A-XDR (BER??) encoded ASN.1 notation
		if ((byteBuffer[iOffset] & (byte)0x80) != 0) {
			return ((long)(byteBuffer[iOffset]&0x7F)<<7) | ((byteBuffer[iOffset+1]&0x7F));
		} else {
			return ((long)byteBuffer[iOffset] & 0xFF);
		}
	}

	public static long parseValue2long(byte[] byteBuffer) throws IOException {
		return parseValue2long(byteBuffer,0);
	}

	public static long parseValue2long(byte[] byteBuffer,int iOffset) throws IOException {
		switch (byteBuffer[iOffset])
		{
		case TYPEDESC_NULL:
			return 0;

		case TYPEDESC_FLOATING_POINT:
		case TYPEDESC_OCTET_STRING:
		case TYPEDESC_VISIBLE_STRING:
		case TYPEDESC_TIME:
		case TYPEDESC_BCD:
		case TYPEDESC_BITSTRING:
		case TYPEDESC_STRUCTURE:
		case TYPEDESC_ARRAY:
		case  TYPEDESC_COMPACT_ARRAY:
			throw new IOException("parseValue2int() error");

		case  TYPEDESC_ENUM:
		case TYPEDESC_BOOLEAN:
			return (long)byteBuffer[iOffset+1]&0xff;

		case TYPEDESC_DOUBLE_LONG:
		case TYPEDESC_DOUBLE_LONG_UNSIGNED:
			return ProtocolUtils.getInt(byteBuffer,iOffset+1);

		case  TYPEDESC_UNSIGNED:
		case  TYPEDESC_INTEGER:
			return (long)byteBuffer[iOffset+1]&0xff;

		case  TYPEDESC_LONG_UNSIGNED:
		case  TYPEDESC_LONG:
			return ProtocolUtils.getShort(byteBuffer,iOffset+1);

		case  TYPEDESC_LONG64:
			return ProtocolUtils.getLong(byteBuffer,iOffset+1);

		default:
			throw new IOException("parseValue2long() error, unknown type "+byteBuffer[iOffset]);
		} // switch (byteBuffer[iOffset])

	} // public long parseValue2long(byte[] byteBuffer,int iOffset) throws IOException

	public static String parseValue2String(byte[] byteBuffer,int iOffset) throws IOException
	{
		switch (byteBuffer[iOffset])
		{
		case TYPEDESC_NULL:
			return String.valueOf(0);

		case TYPEDESC_FLOATING_POINT:
		case TYPEDESC_TIME:
		case TYPEDESC_BCD:
		case TYPEDESC_BITSTRING:
		case TYPEDESC_STRUCTURE:
		case TYPEDESC_ARRAY:
		case  TYPEDESC_COMPACT_ARRAY:
			throw new IOException("parseValue2int() error");


		case TYPEDESC_OCTET_STRING:
		case TYPEDESC_VISIBLE_STRING:
			byte[] bstr = new byte[byteBuffer[iOffset+1]];
			for(int i=0;i<bstr.length;i++) {
				bstr[i] = byteBuffer[iOffset+2+i];
			}
			return new String(bstr);

		case  TYPEDESC_ENUM:
		case TYPEDESC_BOOLEAN:
			return String.valueOf((long)byteBuffer[iOffset+1]&0xff);

		case TYPEDESC_DOUBLE_LONG:
		case TYPEDESC_DOUBLE_LONG_UNSIGNED:
			return String.valueOf((long)ProtocolUtils.getInt(byteBuffer,iOffset+1));

		case  TYPEDESC_UNSIGNED:
		case  TYPEDESC_INTEGER:
			return String.valueOf((long)byteBuffer[iOffset+1]&0xff);

		case  TYPEDESC_LONG_UNSIGNED:
		case  TYPEDESC_LONG:
			return String.valueOf((long)ProtocolUtils.getShort(byteBuffer,iOffset+1));

		case  TYPEDESC_LONG64:
			return String.valueOf(ProtocolUtils.getLong(byteBuffer,iOffset+1));

		default:
			throw new IOException("parseValue2int() error, unknown type.");
		} // switch (byteBuffer[iOffset])

	} // public String parseValue2String(byte[] byteBuffer,int iOffset) throws IOException

	protected static String getInfoLN(byte[] ln)
	{
		int aField, bField, cField, dField, eField, fField;
		String str = "";

		aField=ln[0]&0xFF;
		bField=ln[1]&0xFF;
		cField=ln[2]&0xFF;
		dField=ln[3]&0xFF;
		eField=ln[4]&0xFF;
		fField=ln[5]&0xFF;

		// A
		//if (A == 0) str += " Abstract");
		//if (A == 1) str += " Electricity");

		// C
		if (aField==1) // electricity related objects
		{
			if ((bField>=1) && (bField<=64)) { str += " channel"+Integer.toString(bField&0xFF); }
			if ((bField>=65) && (bField<=127)) { str += " ???, reserved"; }
			if ((bField>=128) && (bField<=254)) { str += " ???, manufacturer specific"; }
			if (bField>=255) {
				str += " ???, reserved";
			}

			if ((cField == 0) && (dField==0) && (eField<=9) && (fField==255)) { str += " Elektricity ID obj "+(eField+1)+" (data or register)"; }
			if ((cField == 0) && (dField==0) && (eField==255) && (fField==255)) { str += " Elektricity ID's obj(profile)"; }
			if ((cField == 0) && (dField==1) && (eField==0) && (fField==255)) { str += " Billing period counter obj (data or register)"; }
			if ((cField == 0) && (dField==1) && (eField==1) && (fField==255)) { str += " Number of available billing period data obj (data or register)"; }

			if ((cField == 0) && (dField==2) && (eField==0) && (fField==255)) { str += " Configuration program version NR obj (data or register)"; }
			if ((cField == 0) && (dField==2) && (eField==2) && (fField==255)) { str += " Time switch program NR obj (data or register)"; }
			if ((cField == 0) && (dField==2) && (eField==3) && (fField==255)) { str += " RCR program NR obj (data or register)"; }

			if ((cField == 0) && (dField==9) && (eField==10) && (fField==255)) { str += " Clock synchronization method (data or register)"; }
			if ((cField == 0) && (dField==11) && (eField==1) && (fField==255)) { str += " Measurement algorithjm for active power (data or register)"; }
			if ((cField == 0) && (dField==11) && (eField==2) && (fField==255)) { str += " Measurement algorithjm for active energy (data or register)"; }
			if ((cField == 0) && (dField==11) && (eField==3) && (fField==255)) { str += " Measurement algorithjm for reactive power (data or register)"; }
			if ((cField == 0) && (dField==11) && (eField==4) && (fField==255)) { str += " Measurement algorithjm for reactive energy (data or register)"; }
			if ((cField == 0) && (dField==11) && (eField==5) && (fField==255)) { str += " Measurement algorithjm for apparent power (data or register)"; }

			if ((cField == 0) && (dField==11) && (eField==6) && (fField==255)) { str += " Measurement algorithjm for apparent energy (data or register)"; }
			if ((cField == 0) && (dField==11) && (eField==7) && (fField==255)) { str += " Measurement algorithjm for power factor calculation (data or register)"; }

			if (cField == 1) { str += " SUM(Li) active power+"; }
			if (cField == 2) { str += " SUM(Li) active power-"; }
			if (cField == 3) { str += " SUM(Li) reactive power+"; }
			if (cField == 4) { str += " SUM(Li) reactive power-"; }
			if (cField == 5) { str += " SUM(Li) reactive power QI"; }
			if (cField == 6) { str += " SUM(Li) reactive power QII"; }
			if (cField == 7) { str += " SUM(Li) reactive power QIII"; }
			if (cField == 8) { str += " SUM(Li) reactive power QIV"; }
			if (cField == 9) { str += " SUM(Li) apparent power+"; }
			if (cField == 10) { str += " SUM(Li) apparent power-"; }
			if (cField == 11) { str += " Current, any phase"; }
			if (cField == 12) { str += " Voltage, any phase"; }
			if (cField == 13) { str += " Apparent power factor"; }
			if (cField == 14) { str += " Supply frequency"; }
			if (cField == 15) { str += " SUM(Li) Active power QI+QIV+QII+QIII"; }
			if (cField == 16) { str += " SUM(Li) Active power QI+QIV-QII-QIII"; }
			if (cField == 17) { str += " SUM(Li) Active power QI"; }
			if (cField == 18) { str += " SUM(Li) Active power QII"; }
			if (cField == 19) { str += " SUM(Li) Active power QIII"; }
			if (cField == 20) { str += " SUM(Li) Active power QIV"; }
			if (cField == 21) { str += " L1 active power+"; }
			if (cField == 22) { str += " L1 active power-"; }
			if (cField == 23) { str += " L1 reactive power+"; }
			if (cField == 24) { str += " L1 reactive power-"; }
			if (cField == 25) { str += " L1 reactive power QI"; }
			if (cField == 26) { str += " L1 reactive power QII"; }
			if (cField == 27) { str += " L1 reactive power QIII"; }
			if (cField == 28) { str += " L1 reactive power QIV"; }
			if (cField == 29) { str += " L1 apparent power+"; }
			if (cField == 30) { str += " L1 apparent power-"; }
			if (cField == 33) { str += " L1 power factor"; }
			if (cField == 34) { str += " L1 frequency"; }
			if (cField == 35) { str += " L1 active power QI+QIV+QII+QIII"; }
			if (cField == 36) { str += " L1 active power QI+QIV-QII-QIII"; }
			if (cField == 37) { str += " L1 active power QI"; }
			if (cField == 38) { str += " L1 active power QII"; }
			if (cField == 39) { str += " L1 active power QIII"; }
			if (cField == 40) { str += " L1 active power QIV"; }
			if (cField == 41) { str += " L2 active power+"; }
			if (cField == 42) { str += " L2 active power-"; }
			if (cField == 43) { str += " L2 reactive power+"; }
			if (cField == 44) { str += " L2 reactive power-"; }
			if (cField == 45) { str += " L2 reactive power QI"; }
			if (cField == 46) { str += " L2 reactive power QII"; }
			if (cField == 47) { str += " L2 reactive power QIII"; }
			if (cField == 48) { str += " L2 reactive power QIV"; }
			if (cField == 49) { str += " L2 apparent power+"; }
			if (cField == 50) { str += " L2 apparent power-"; }

			if (cField == 53) { str += " L2 power factor"; }
			if (cField == 54) { str += " L2 frequency"; }
			if (cField == 55) { str += " L2 active power QI+QIV+QII+QIII"; }
			if (cField == 56) { str += " L2 active power QI+QIV-QII-QIII"; }
			if (cField == 57) { str += " L2 active power QI"; }
			if (cField == 58) { str += " L2 active power QII"; }
			if (cField == 59) { str += " L2 active power QIII"; }
			if (cField == 60) { str += " L2 active power QIV"; }
			if (cField == 61) { str += " L3 active power+"; }
			if (cField == 62) { str += " L3 active power-"; }
			if (cField == 63) { str += " L3 reactive power+"; }
			if (cField == 64) { str += " L3 reactive power-"; }
			if (cField == 65) { str += " L3 reactive power QI"; }
			if (cField == 66) { str += " L3 reactive power QII"; }
			if (cField == 67) { str += " L3 reactive power QIII"; }
			if (cField == 68) { str += " L3 reactive power QIV"; }
			if (cField == 69) { str += " L3 apparent power+"; }
			if (cField == 70) { str += " L3 apparent power-"; }

			if ((cField == 31) && (dField!=7)) { str += " ???"; }
			if ((cField == 32) && (dField!=7)) { str += " ???"; }
			if ((cField == 51) && (dField!=7)) { str += " ???"; }
			if ((cField == 52) && (dField!=7)) { str += " ???"; }
			if ((cField == 71) && (dField!=7)) { str += " ???"; }
			if ((cField == 72) && (dField!=7)) { str += " ???"; }

			int match=0;
			if (((cField == 31) || (cField == 32)|| (cField == 51)|| (cField == 52)|| (cField == 71)|| (cField == 72)) &&	(dField==7) && (eField==0)) {
				str += " Total";
				match=1;
			}
			if (((cField == 31) || (cField == 32)|| (cField == 51)|| (cField == 52)|| (cField == 71)|| (cField == 72)) &&	(dField==7) && (eField>=1) && (eField<=127)) {
				str += " harmonic "+eField;
				match=1;
			}
			if (((cField == 31) || (cField == 32)|| (cField == 51)|| (cField == 52)|| (cField == 71)|| (cField == 72)) &&	(dField==7) && (eField>=128) && (eField<=254)) {
				str += " Manufacturer specific";
				match=1;
			}
			if (((cField == 31) || (cField == 32)|| (cField == 51)|| (cField == 52)|| (cField == 71)|| (cField == 72)) && (dField==7) && (eField==255)) {
				str += " Reserved";
				match=1;
			}

			if ((match == 0) && (cField != 0)) {
				if (eField==0) {	str += " Total"; }
				if ((eField>=1) && (eField<=63)) { str += " Rate "+eField;	}
				if ((eField>=128) && (eField<=254)) {	str += " Manufactures specific"; }
				if (eField==255) { str += " Reserved"; }
			}

			if (cField == 73) { str += " L3 power factor"; }
			if (cField == 74) { str += " L3 frequency"; }
			if (cField == 75) { str += " L3 active power QI+QIV+QII+QIII"; }
			if (cField == 76) { str += " L3 active power QI+QIV-QII-QIII"; }
			if (cField == 77) { str += " L3 active power QI"; }
			if (cField == 78) { str += " L3 active power QII"; }
			if (cField == 79) { str += " L3 active power QIII"; }
			if (cField == 80) { str += " L3 active power QIV"; }

			if ((cField == 81) && (dField==7)) { str += " Angle measurement "+eField; }


			if (cField == 82) { str += " Unitless quantity"; }
			if ((cField >= 83) && (cField <= 90)) { str += " ???"; }
			if (cField == 91) { str += " L0 current (N)"; }
			if (cField == 92) { str += " L0 voltage (N)"; }
			if ((cField >= 93) && (cField <= 95)) { str += " ???"; }
			if (cField == 96) { str += " El. rel. services, see 5.4.7"; }
			if (cField == 97) { str += " El. rel. error messages"; }
			if (cField == 98) { str += " El. list"; }

			//          if (C == 99)  str += " El. profiles, see 5.4.10"; }
			if ((cField==99) && (dField==1) && (eField>=0) && (eField<=127) && (fField==255)) { str += " Load profile object "+eField+" with recording period 1 (profile)"; }
			if ((cField==99) && (dField==2) && (eField>=0) && (eField<=127) && (fField==255)) { str += " Load profile object "+eField+" with recording period 2 (profile)"; }
			if ((cField==99) && (dField==3) && (eField==0) && (fField==255)) { str += " Load profile during test (profile)"; }
			if ((cField==99) && (dField==10) && (eField==1) && (fField==255)) { str += " Dips voltage profile (profile)"; }
			if ((cField==99) && (dField==10) && (eField==2) && (fField==255)) { str += " Swells voltage profile (profile)"; }
			if ((cField==99) && (dField==10) && (eField==3) && (fField==255)) { str += " Cuts voltage profile (profile)"; }
			if ((cField==99) && (dField==11) && (eField>=1) && (eField<=127) && (fField==255)) { str += " Voltage harmonic profile "+eField+" (profile)"; }
			if ((cField==99) && (dField==12) && (eField>=1) && (eField<=127) && (fField==255)) { str += " Current harmonic profile "+eField+" (profile)"; }
			if ((cField==99) && (dField==13) && (eField==0) && (fField==255)) { str += " Voltage unbalance profile (profile)"; }
			if ((cField==99) && (dField==98) && (fField==255)) { str += " Event log +"+eField+" (profile)"; }
			if ((cField==99) && (dField==99) && (fField==255)) { str += " Certification data log +"+eField+" (profile)"; }

			if ((cField >= 100) && (cField <= 127)) { str += " ???, reserved"; }
			if ((cField >= 128) && (cField <= 254)) { str += " ???, manufacturer specific"; }
			if (cField == 255) { str += " ???, reserved"; }

			if ((cField!=0)&&(cField!=96)&&(cField!=97)&&(cField!=98)&&(cField!=99))
			{
				if (dField==0) { str += " Billing period average (since last reset)"; }
				if (dField==1) { str += " Cumulative minimum 1"; }
				if (dField==2) { str += " Cumulative maximum 1"; }
				if (dField==3) { str += " Minimum 1"; }
				if (dField==4) { str += " Current average 1"; }
				if (dField==5) { str += " Last average 1"; }
				if (dField==6) { str += " Maximum 1"; }
				if (dField==7) { str += " Instantaneous value"; }
				if (dField==8) { str += " Time integral 1"; }
				if (dField==9) { str += " Time integral 2"; }
				if (dField==10) { str += " Time integral 3"; }
				if (dField==11) { str += " Cumulative minimum 2"; }
				if (dField==12) { str += " Cumulative maximum 2"; }
				if (dField==13) { str += " Minimum 2"; }
				if (dField==14) { str += " Current average 2"; }
				if (dField==15) { str += " Last average 2"; }
				if (dField==16) { str += " Maximum 2"; }
				if ((dField>=17) && (dField<=20)) { str += " ???"; }
				if (dField==21) { str += " Cumulative minimum 3"; }
				if (dField==22) { str += " Cumulative maximum 3"; }
				if (dField==23) { str += " Minimum 3"; }
				if (dField==24) { str += " Current average 3"; }
				if (dField==25) { str += " Last average 3"; }
				if (dField==26) { str += " Maximum 3"; }
				if (dField==27) { str += " Current average 5"; }
				if (dField==28) { str += " Current average 6"; }
				if (dField==29) { str += " Time integral 5"; }
				if (dField==30) { str += " Time integral 6"; }
				if (dField==31) { str += " Under limit threshold"; }
				if (dField==32) { str += " Under limit occurence counter"; }
				if (dField==33) { str += " Under limit duration"; }
				if (dField==34) { str += " Under limit magnitude"; }
				if (dField==35) { str += " Over limit threshold"; }
				if (dField==36) { str += " Over limit occurence counter"; }
				if (dField==37) { str += " Over limit duration"; }
				if (dField==38) { str += " Over limit magnitude"; }
				if (dField==39) { str += " Missing threshold"; }
				if (dField==40) { str += " Missing occurence counter"; }
				if (dField==41) { str += " Missing duration"; }
				if (dField==42) { str += " Missing magnitude"; }
				if ((dField>=43) && (dField<=54)) { str += " ???"; }
				if (dField==55) { str += " Test average"; }
				if ((dField>=56) && (dField<=57)) { str += " ???"; }
				if (dField==58) { str += " Time integral 4"; }
				if ((dField>=128) && (dField<=254)) { str += " ???, manufacturer specific codes"; }
				if (dField==255) { str += " Reserved"; }
			}

		} // if (A==1)
		else if (aField==0) // abstract objects
		{
			if ((bField>=1) && (bField<=64)) { str += " object/channel"+Integer.toString(bField&0xFF); }
			else if ((bField>=65) && (bField<=127)) { str += " ???, reserved"; }
			else if ((bField>=128) && (bField<=254)) { str += " ???, manufacturer specific"; }
			else if (bField>=255) { str += " ???, reserved"; }

			if (cField == 0) { str += " gen. purpose"; }
			else if ((cField == 1) && (dField==0) && (fField==255)) { str += " Clock object "+eField; }
			else if ((cField == 2) && (dField==0) && (eField==0) && (fField==255)) { str += " PSTN modem configuration"; }
			else if ((cField == 2) && (dField==1) && (eField==0) && (fField==255)) { str += " PSTN auto dial"; }
			else if ((cField >= 3) && (cField<=9)) { str += " ???"; }
			else if ((cField == 2) && (dField==2) && (eField==0) && (fField==255)) { str += " PSTN auto answer"; }

			else if ((cField == 10) && (dField==0) && (eField==0) && (fField==255)) { str += " Global meter reset"; }
			else if ((cField == 10) && (dField==0) && (eField==1) && (fField==255)) { str += " MDI reset / end of billing period"; }
			else if ((cField == 10) && (dField==0) && (eField==100) && (fField==255)) { str += " Tarrification script table"; }
			else if ((cField == 10) && (dField==0) && (eField==101) && (fField==255)) { str += " Activate test mode"; }
			else if ((cField == 10) && (dField==0) && (eField==102) && (fField==255)) { str += " Activate normal mode"; }
			else if ((cField == 10) && (dField==0) && (eField==103) && (fField==255)) { str += " Set output signals"; }
			else if ((cField == 10) && (dField==0) && (eField==125) && (fField==255)) { str += " Broadcast script table"; }

			else if ((cField == 11) && (dField==0) && (eField==0) && (fField==255)) { str += " Special days table"; }
			else if ((cField == 12) && (dField==0) && (fField==255)) { str += " Schedule object "+eField; }
			else if ((cField == 13) && (dField==0) && (eField==0) && (fField==255)) { str += " Activity calendar"; }
			else if ((cField == 14) && (dField==0) && (eField==0) && (fField==255)) { str += " Register activation"; }
			else if ((cField == 15) && (dField==0) && (eField==0) && (fField==255)) { str += " End of billing period (IC single action schedule)"; }
			else if ((cField == 20) && (dField==0) && (eField==0) && (fField==255)) { str += " IEC optical port setup obj"; }
			else if ((cField == 20) && (dField==0) && (eField==1) && (fField==255)) { str += " IEC electrical port setup obj"; }

			else if ((cField == 21) && (dField==0) && (eField==0) && (fField==255)) { str += " general local port readout (IC profile)"; }
			else if ((cField == 21) && (dField==0) && (eField==1) && (fField==255)) { str += " general display readout (IC profile)"; }
			else if ((cField == 21) && (dField==0) && (eField==2) && (fField==255)) { str += " alternate display readout (IC profile)"; }
			else if ((cField == 21) && (dField==0) && (eField==3) && (fField==255)) { str += " service display readout (IC profile)"; }
			else if ((cField == 21) && (dField==0) && (eField==4) && (fField==255)) { str += " list of configurable meter data (IC profile)"; }
			else if ((cField == 21) && (dField==0) && (eField>=5) && (fField==255)) { str += " additional readout profile "+(eField-4)+" (IC profile)"; }

			else if ((cField == 22) && (dField==0) && (eField==0) && (fField==255)) { str += " IEC HDLC setup obj"; }
			else if ((cField == 23) && (dField==0) && (eField==0) && (fField==255)) { str += " IEC twisted pair setup"; }
			else if ((cField >= 24) && (cField<=39)) { str += " ???"; }
			else if ((cField == 40) && (dField==0) && (eField==0) && (fField==255)) { str += " Current Association"; }
			else if ((cField == 40) && (dField==0) && (eField>=1) && (fField==255)) { str += " Association instance "+eField; }

			else if ((cField == 41) && (dField==0) && (eField==0) && (fField==255)) { str += " SAP assignment obj"; }
			else if ((cField == 42) && (dField==0) && (eField==0) && (fField==255)) { str += " COSEM logical device name (data or register)"; }
			else if ((cField >= 43) && (cField<=64)) { str += " ???"; }

			else if ((cField == 65) && (fField==255)) { str += " Utility tables D="+dField+" E="+eField; }

			else if ((cField >= 66) && (cField<=95)) { str += " ???"; }

			else if ((cField == 96) && (dField==1) && (eField<=9) && (fField==255)) { str += " Device ID "+(eField+1)+" obj (data or register)"; }
			else if ((cField == 96) && (dField==1) && (eField==255) && (fField==255)) { str += " Device ID's object (profile)"; }

			else if ((cField == 96) && (dField==2) && (eField==0) && (fField==255)) { str += " Number of configuration program changes obj (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==1) && (fField==255)) { str += " Date of last configuration program changes obj (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==2) && (fField==255)) { str += " Date of last time switch program change object (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==3) && (fField==255)) { str += " Date of last ripple control receiver program change obj (data)"; }

			else if ((cField == 96) && (dField==2) && (eField==4) && (fField==255)) { str += " Status of security switches (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==5) && (fField==255)) { str += " Date of last calibration (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==6) && (fField==255)) { str += " Date of next configuration program change (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==7) && (fField==255)) { str += " Time of activation of the passive calendar (data)"; }

			else if ((cField == 96) && (dField==2) && (eField==10) && (fField==255)) { str += " Number of protected configuration program changes obj (data)"; }
			else if ((cField == 96) && (dField==2) && (eField==11) && (fField==255)) { str += " Date of last protected configuration program changes obj (data)"; }

			else if ((cField == 96) && (dField==3) && (eField==1) && (fField==255)) { str += " State of input control signals (data or register or extended register)"; }
			else if ((cField == 96) && (dField==3) && (eField==2) && (fField==255)) { str += " State of output control signals (data or register or extended register)"; }

			else if ((cField == 96) && (dField==4) && (eField==0) && (fField==255)) { str += " State of the internal control signals (data or register or extended register)"; }

			else if ((cField == 96) && (dField==5) && (eField==0) && (fField==255)) { str += " internal operating status (data or register or extended register)"; }

			else if ((cField == 96) && (dField==6) && (eField==0) && (fField==255)) { str += " Battery use time counter (register or extended register)"; }
			else if ((cField == 96) && (dField==6) && (eField==1) && (fField==255)) { str += " Battery charge display (register or extended register)"; }
			else if ((cField == 96) && (dField==6) && (eField==2) && (fField==255)) { str += " Date of next change (register or extended register)"; }
			else if ((cField == 96) && (dField==6) && (eField==3) && (fField==255)) { str += " Battery voltage (register or extended register)"; }

			else if ((cField == 96) && (dField==7) && (eField==0) && (fField==255)) { str += " Total failure of all 3 phases longer than internal autonomy  (data or profile)"; }
			else if ((cField == 96) && (dField==7) && (eField==1) && (fField==255)) { str += " Phase L1 (data or profile)"; }
			else if ((cField == 96) && (dField==7) && (eField==2) && (fField==255)) { str += " Phase L2 (data or profile)"; }
			else if ((cField == 96) && (dField==7) && (eField==3) && (fField==255)) { str += " Phase L3 (data or profile)"; }

			else if ((cField == 96) && (dField==8) && (eField==0) && (fField==255)) { str += " Time of operation  (data or register)"; }
			else if ((cField == 96) && (dField==8) && (eField>=1) && (eField<=63) && (fField==255)) { str += " Time of registration rate "+eField+" (data or register)"; }
			else if ((cField == 96) && (dField==8) && (eField==255) && (fField==255)) { str += " Time of registration (profile)"; }

			else if ((cField == 96) && (dField==9) && (eField==0) && (fField==255)) { str += " Ambient temperature  (data or register or extended register)"; }

			else if ((cField == 96) && (dField>=50) && (dField<=96)) { str += " Manufacturer specific abstract objects (data or register or extended register or profile)"; }

			else if ((cField == 96) && (dField==99) && (eField==8) && (fField==255)) { str += " Name of the standard data set (data or register)"; }

			else if ((cField == 97) && (dField==97) && (eField<=9) && (fField==255)) { str += " Error "+(eField+1)+" obj (data)"; }
			else if ((cField == 97) && (dField==97) && (eField==255) && (fField==255)) { str += " Error profile obj (profile)"; }

			else if ((cField == 98) && (dField==1)) { str += " Date of billing periods E="+eField+" F="+fField+" (profile)"; }

			else if (cField == 127) { str += " Inactive object"; }

			else if ((cField >= 128) && (cField<=255)) { str += " ???, manufacturer specific"; }

		} else {
			str += " ???, value A = "+aField+" unknown";
		}

		return str;

	}

}
