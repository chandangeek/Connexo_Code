package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderModelInfo.EncoderModelType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public abstract class GenericHeader<T> {

	public abstract int getApplicationStatus();

	private static final int GENERIC_STRUCTURE_SIZE = 23;
    private static final double MAX = 0x20;


    /**
      * 23 bytes generic header.
      * first byte is unused
      */

	/**
	 * The "Operating Mode" is used to activate/deactivate each Waveflow 100mW Encoder feature. This
	 * parameter is accessible through the command write parameters.
	 * "Operating Mode" parameter is systematically returned in generic header present in almost each response
	 * frame of the Waveflow 500mW Encoder.
	 * bit15..9 unused
	 * bit8 backflow detection activated/deactivated
	 * bit7 Encoder misread detection activated/deactivated
	 * bit6 Extreme leak detection activated/deactivated
	 * bit5 Residual leak detection activated/deactivated
	 * bit4 Encoder	communication fault detection activated/deactivated
	 * bit3..2 Datalogging 00 : deactivated 01 : time steps mngt 10 : once a week mngt 11 : once a month mngt
	 * bit1..0 Ports management 00 : one Port (A) 01 :2 Ports (A & B)
	 */
	int operatingMode; // 2 bytes

	Date currentRTC; // 7 bytes

	/**
	 * The QoS value gives an image of the last beacon radio reception signal strength
	 */
	int qos; // 1 byte

	/**
	 * The "Short Life Counter" value gives correspond to the 2 most significant bytes of the real "Life Counter"
	 * (3 bytes). This real "Life Counter" gives an estimated quantity of energy that remains in Waveflow 100mW
	 * Encoder battery. User software has to take into account the default value of this counter to compute an
	 * estimated remaining lifetime.
	 */
	int shortLiftCounter; // 2 bytes


	/**
	 * The remaining battery life in 0..100 % knowing that the initial battery life count is 100 % and the getBatteryLifeCounter() is the remaining
	 * @return the remaining battery life in percentage
	 */
	public final int remainingBatteryLife() {
		return 100-(((BatteryLifeDurationCounter.INITIAL_BATTERY_LIFE_COUNT*100)-((getShortLiftCounter()<<8)*100))/BatteryLifeDurationCounter.INITIAL_BATTERY_LIFE_COUNT);
	}

	/**
	 * The encodermodel id and manufacturer for the port A and B
	 */
	EncoderModelInfo[] encoderModelInfos = new EncoderModelInfo[2];

	/**
	 * The encoder unit and digits info for the port A and B
	 */
	EncoderUnitInfo[] encoderUnitInfos = new EncoderUnitInfo[2];

    abstract void parse(DataInputStream dais) throws IOException;

	GenericHeader(DataInputStream dais, Logger logger, TimeZone timeZone) throws IOException {

		dais.readByte(); // skip unused byte (value 0)
		operatingMode = WaveflowProtocolUtils.toInt(dais.readShort());

		parse(dais);

		byte[] temp = new byte[7];
		dais.read(temp);
		currentRTC = TimeDateRTCParser.parse(temp, timeZone).getTime();

		qos = WaveflowProtocolUtils.toInt(dais.readByte());

		shortLiftCounter = WaveflowProtocolUtils.toInt(dais.readShort());

		/*
		Encoder unit on Port A
		byte7   Number of digits before the decimal point
		byte6   Unit
		Encoder unit on Port B
		byte5   Number of digits before the decimal point
		byte4   Unit
		Encoder model on Port A
		byte3   Encoder Manufacturer
		byte2   Adapter code
		Encoder model on Port B
		byte1   Encoder Manufacturer
		byte0   Adapter code
		*/

		for (int port = 0;port<2;port++) {
			int nrOfDigitsBeforeDecimalPoint = WaveflowProtocolUtils.toInt(dais.readByte());
			int id = WaveflowProtocolUtils.toInt(dais.readByte());
			encoderUnitInfos[port] = new EncoderUnitInfo(EncoderUnitType.fromId(id),nrOfDigitsBeforeDecimalPoint);
		}

		for (int port = 0;port<2;port++) {
			int id = WaveflowProtocolUtils.toInt(dais.readByte());
			int manufacturerId = WaveflowProtocolUtils.toInt(dais.readByte());
			encoderModelInfos[port] = new EncoderModelInfo(EncoderModelType.fromId(id),manufacturerId);
		}
	}

	static int size() {
		return GENERIC_STRUCTURE_SIZE;
	}

	final int getOperatingMode() {
		return operatingMode;
	}

	final Date getCurrentRTC() {
		return currentRTC;
	}


	final int getQos() {
		return qos;
	}

    public double getRssiLevel() {
        double value = (((double) qos) / MAX) * 100;
        return Math.round(value * 100.0) / 100.0;
    }

	final int getShortLiftCounter() {
		return shortLiftCounter;
	}


	final EncoderModelInfo[] getEncoderModelInfos() {
		return encoderModelInfos;
	}


	public final EncoderUnitInfo[] getEncoderUnitInfos() {
		return encoderUnitInfos;
	}



}
