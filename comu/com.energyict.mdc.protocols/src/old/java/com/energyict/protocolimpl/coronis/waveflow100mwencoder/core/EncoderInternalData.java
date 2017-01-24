package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class EncoderInternalData extends InternalData {

	static public final int ENCODER_INTERNAL_DATA_LENGTH = 58;

	/**
	 * 10 characters identifying the meter.
	 */
	private String userId;
	/**
	 * 8 digits giving the current meter reading
	 */
	private long currentIndex;

	/**
	 * 2 digits (1 byte) holding the value of up to 8 status flags
	 * bit0: cell log
	 * bit1: eeprom fault
	 * bit2..7 reserved
	 */
	private int status;

	/**
	 * 2 digits (1 byte) identifying the data packet version
	 */
	private int version;

	/**
	 * 8 digits identifying the totalizer serial
	 */
	private String totalizerSerial;

	/**
	 * 8 digits identifying the transducer serial
	 */
	private String transducerSerial;

	/**
	 * 2 digits (1 byte) giving the last 2 digits of the meterreading
	 */
	private int lastPart;

	/**
	 * 2 digits (1 byte) giving the unit
	 * 1 cubic meters, 2 litres, 3 cubic feet, 5 imperial gallons, 6 us gallons
	 */
	private EncoderUnitType encoderUnitType;

	/**
	 * 2 digits (1 byte) number of significant numbers of reading
	 */
	private int decimalPosition;

	/**
	 * 2 digits (1 byte) decimal counter of dry electrode events
	 */
	private int dryCount;

	/**
	 * 2 digits (1 byte) decimal counter of leak events
	 */
	private int leakCount;

	/**
	 * 2 digits (1 byte) decimal counter of tamper events
	 */
	private int tamperCount;

	/**
	 * 2 digits (1 byte) decimal counter of no flow events
	 */
	private int noflowCount;

	/**
	 * raw string of the internal data
	 */
	private String encoderInternalData;

	final public String getEncoderInternalData() {
		return encoderInternalData;
	}

    @Override
    public String getSerialNumber() {
		return userId;
	}

	final public long getCurrentIndex() {
		return currentIndex;
	}

	final public int getStatus() {
		return status;
	}

	final public int getVersion() {
		return version;
	}

	final public String getTotalizerSerial() {
		return totalizerSerial;
	}

	final public String getTransducerSerial() {
		return transducerSerial;
	}

	final public int getLastPart() {
		return lastPart;
	}

	final public EncoderUnitType getEncoderUnitType() {
		return encoderUnitType;
	}

	final public int getDecimalPosition() {
		return decimalPosition;
	}

	final public int getDryCount() {
		return dryCount;
	}

	final public int getLeakCount() {
		return leakCount;
	}

	final public int getTamperCount() {
		return tamperCount;
	}

	final public int getNoflowCount() {
		return noflowCount;
	}

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        if ((status & 0x01) == 0x01) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_CELL_LOW_A : EventStatusAndDescription.EVENTCODE_CELL_LOW_B, "Cell low" + getPortInfo()));
        }
        if ((status & 0x02) == 0x02) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_EEPROM_FAULT_A : EventStatusAndDescription.EVENTCODE_EEPROM_FAULT_B, "EEPROM fault" + getPortInfo()));
        }
        if (dryCount > 0) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_DRY_A : EventStatusAndDescription.EVENTCODE_DRY_B, dryCount + " dry electrode event(s)" + getPortInfo()));
        }
        if (leakCount > 0) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_LEAK_A : EventStatusAndDescription.EVENTCODE_LEAK_B, leakCount + " leak event(s)" + getPortInfo()));
        }
        if (tamperCount > 0) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_TAMPER_A : EventStatusAndDescription.EVENTCODE_TAMPER_B, tamperCount + " tamper event(s)" + getPortInfo()));
        }
        if (noflowCount > 0) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_NOFLOW_A : EventStatusAndDescription.EVENTCODE_NOFLOW_B, noflowCount + " no-flow event(s)" + getPortInfo()));
        }
        return meterEvents;
    }

	public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EncoderInternalData:\n");
        strBuff.append("   userId=" + getSerialNumber() + "\n");
        strBuff.append("   totalizerSerial="+getTotalizerSerial()+"\n");
        strBuff.append("   transducerSerial="+getTransducerSerial()+"\n");
        strBuff.append("   version="+getVersion()+"\n");
        strBuff.append("   status="+WaveflowProtocolUtils.toHexString(getStatus())+"\n");
        strBuff.append("   currentIndex="+getCurrentIndex()+"\n");
        strBuff.append("   decimalPosition="+getDecimalPosition()+"\n");
        strBuff.append("   unit="+getEncoderUnitType()+"\n");
        strBuff.append("   dryCount="+getDryCount()+"\n");
        strBuff.append("   lastPart="+getLastPart()+"\n");
        strBuff.append("   leakCount="+getLeakCount()+"\n");
        strBuff.append("   noflowCount="+getNoflowCount()+"\n");
        strBuff.append("   tamperCount="+getTamperCount()+"\n");
        return strBuff.toString();
    }

    EncoderInternalData(final byte[] data, final Logger logger, int portId) throws IOException {
        this.portId = portId;

		if (data.length != ENCODER_INTERNAL_DATA_LENGTH) {
			throw new WaveFlow100mwEncoderException("Invalid encoder internal data length. Expected length ["+ENCODER_INTERNAL_DATA_LENGTH+"], received length ["+data.length+"]");
		}

		encoderInternalData = new String(data);

		DataInputStream dais = null;
		try {
			byte[] temp;
			char c;
			int i;

			dais = new DataInputStream(new ByteArrayInputStream(data));

			c = (char)dais.readByte();
			if (c != '*') {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data. Expected [*], read ["+c+"]");
			}

			temp = new byte[10];
			dais.read(temp);
			userId = new String(temp);

			temp = new byte[8];
			dais.read(temp);
			currentIndex = Long.parseLong(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			status = Integer.parseInt(new String(temp));

			i = WaveflowProtocolUtils.toInt(dais.readByte());
			if (i!= 0x0d) {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data. Expected [0x0D], read ["+WaveflowProtocolUtils.toHexString(i)+"]");
			}

			c = (char)dais.readByte();
			if (c != '&') {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data. Expected [&], read ["+c+"]");
			}

			temp = new byte[2];
			dais.read(temp);
			version = Integer.parseInt(new String(temp));

			temp = new byte[8];
			dais.read(temp);
			totalizerSerial = new String(temp);

			temp = new byte[8];
			dais.read(temp);
			transducerSerial = new String(temp);

			temp = new byte[2];
			dais.read(temp);
			lastPart = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			encoderUnitType = EncoderUnitType.fromId(Integer.parseInt(new String(temp)));

			temp = new byte[2];
			dais.read(temp);
			decimalPosition = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			dryCount = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			leakCount = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			tamperCount = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			noflowCount = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			int receivedChecksum = Integer.parseInt(new String(temp),16);

			// calc checksum
			int calculatedChecksum=0;
			for (int j=1; j<=10 ; j++) {
				calculatedChecksum+=WaveflowProtocolUtils.toInt(data[j]);
			}
			for (int j=11; j<=20 ; j+=2) {
				int v = Integer.parseInt(new String(new byte[]{data[j],data[j+1]}),16);
				calculatedChecksum+=v;
			}
			for (int j=23; j<=54 ; j+=2) {
				int v = Integer.parseInt(new String(new byte[]{data[j],data[j+1]}),16);
				calculatedChecksum+=v;
			}

			if (receivedChecksum != (calculatedChecksum&0xff)) {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data checksum. Calculated ["+WaveflowProtocolUtils.toHexString(calculatedChecksum)+"], read ["+WaveflowProtocolUtils.toHexString(receivedChecksum)+"]");
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}
}
