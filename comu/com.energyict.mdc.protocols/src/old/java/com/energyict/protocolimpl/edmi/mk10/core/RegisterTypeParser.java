/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Registertypes.java
 *
 * Created on 21 maart 2006, 16:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

/**
 *
 * @author koen
 */
public class RegisterTypeParser {

	private static final int DEBUG = 0;
	TimeZone timeZone;

	/** Creates a new instance of Registertypes */
	public RegisterTypeParser(TimeZone timeZone) {
		this.timeZone=timeZone;
	}

	public AbstractRegisterType parse2External(char type, byte[] data) throws IOException {
		return parse(type,data, true);
	}

	public AbstractRegisterType parse2Internal(char type, byte[] data) throws IOException {
		return parse(type,data, false);
	}

	public AbstractRegisterType parseFromRaw(char type, byte[] data) throws IOException {
		return this.parseFromRaw(type, data, 0);
	}

	public AbstractRegisterType parseFromRaw(char type, byte[] data, int chan_scaler) throws IOException {
		switch(type) {
		case 'F': // Float
		byte [] bta = {0x00 ,0x00 ,0x00, 0x00};
		int highvalue = (data[1]) & 0x00FF;
		int lowvalue = (data[0]) & 0x00FF;
		double value = lowvalue + (highvalue * 256);
		BigDecimal bi = new BigDecimal(value);
		RegisterTypeFloat rt = new RegisterTypeFloat(bta);
		rt.setValue(bi.movePointLeft(chan_scaler).floatValue());

		if (DEBUG >= 1) {
			System.out.println(	" #### parseFromRaw() Type: " + type +
					" data: " + ProtocolUtils.getResponseData(data) +
					" chan_scaler: 0x" + ProtocolUtils.buildStringHex(chan_scaler, 4) +
					" lowvalue: " + lowvalue +
					" highvalue: " + highvalue +
					" highvalue * 256: " + (highvalue * 256) +
					" Value: " + value +
					" Result: " + rt.getValue()
			);
		}

		return rt;
		default:
			return null;
		}
	}

	private AbstractRegisterType parse(char type, byte[] data, boolean external) throws IOException {

		// The MK10 doesn't support as much register types as the MK6 does. Unused register types are commented out.
		switch(type) {

		case 'A': // String
			return new RegisterTypeString(data);
		case 'B': // Boolean
			return new RegisterTypeBoolean(data);
		case 'C': // Byte
			return new RegisterTypeByte(data);
			//            case 'D': // Double
			//                return new RegisterTypeDouble(data);
			//            case 'E': // EFA String
			//                if (external) return new RegisterTypeString(data);
			//                else return new RegisterType16BitUnsignedInt(data);
		case 'F': // Float
			return new RegisterTypeFloat(data);
			//            case 'G': // String/Long
			//                if (external) return new RegisterTypeString(data);
			//                else return new RegisterType32BitSignedInt(data);
			//            case 'H': // Hex Short
			//                return new RegisterType16BitUnsignedInt(data);
		case 'I': // Short
			return new RegisterType16BitSignedInt(data);
			//            case 'J': // Variable special
			//                return new RegisterTypeRawData(data);
		case 'L': // Long
			return new RegisterType32BitSignedInt(data);
		case 'N': // Invalid type
			return null;
			//            case 'O': // Float energy
			//                if (external) return new RegisterTypeFloat(data);
			//                return new RegisterType32BitUnsignedLong(data);
			//            case 'P': // Power factor
			//                if (external) return new RegisterTypeFloat(data);
			//                else  return new RegisterType16BitSignedInt(data);
		case 'Q': // Time seconds since midnight
			if (external) {
				return new RegisterTypeDate(timeZone,data,true,false);
			} else {
				return new RegisterType32BitUnsignedLong(data);
			}
		case 'R': // Date seconds since 1/1/96
			if (external) {
				return new RegisterTypeDate(timeZone,data,false,true);
			} else {
				return new RegisterType32BitUnsignedLong(data, timeZone);
			}
		case 'S': // Special type
			return new RegisterTypeRawData(data);
		case 'T': // Time/Date seconds since 1/1/96
			if (external) {
				return new RegisterTypeDate(timeZone,data,true,true);
			} else {
				return new RegisterType32BitUnsignedLong(data, timeZone);
			}
			//            case 'U': // Double Energy
			//                if (external) return new RegisterTypeDouble(data);
			//                else return new RegisterType64BitSignedLong(data);
			//            case 'V': // Long Long
			//                return new RegisterType64BitSignedLong(data);
			//            case 'W': // Waveform
			//                return new RegisterTypeRawData(data);
			//            case 'X': // Hex Long
			//                return new RegisterType32BitUnsignedLong(data);
			//            case 'Z': // Hex Long (register nr)
			//                return new RegisterType32BitUnsignedLong(data);
		default:
			return null;
		}


	}



}
