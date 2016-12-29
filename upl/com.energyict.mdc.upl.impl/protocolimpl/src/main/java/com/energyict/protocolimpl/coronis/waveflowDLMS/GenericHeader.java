package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

public class GenericHeader {

	private int version;
	private int operatingMode;
	private int applicationStatus;
	private int alarmConfiguration;
	Date currentDateTime;
	private int qos;

	public String toString() {
		return "version="+WaveflowProtocolUtils.toHexString(version)+", "+
		       "operatingMode="+WaveflowProtocolUtils.toHexString(operatingMode)+", "+
		       "applicationStatus="+WaveflowProtocolUtils.toHexString(applicationStatus)+", "+
		       "alarmConfiguration="+WaveflowProtocolUtils.toHexString(alarmConfiguration)+", "+
		       "currentDateTime="+currentDateTime+", "+
		       "qos="+WaveflowProtocolUtils.toHexString(qos);
	}

	GenericHeader(byte[] data,AbstractDLMS abstractDLMS) throws IOException {

		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));
			version = WaveflowProtocolUtils.toInt(dais.readByte());
			operatingMode = WaveflowProtocolUtils.toInt(dais.readShort());
			applicationStatus = WaveflowProtocolUtils.toInt(dais.readByte());
			alarmConfiguration = WaveflowProtocolUtils.toInt(dais.readByte());
			byte[] temp = new byte[7];
			dais.read(temp);
			currentDateTime = TimeDateRTCParser.parse(temp, abstractDLMS.getTimeZone()).getTime();
			qos = WaveflowProtocolUtils.toInt(dais.readByte());
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}
	}

	static int size() {
		return 13;
	}

	final int getVersion() {
		return version;
	}

	final int getOperatingMode() {
		return operatingMode;
	}

	final int getApplicationStatus() {
		return applicationStatus;
	}

	final int getAlarmConfiguration() {
		return alarmConfiguration;
	}

	final Date getCurrentDateTime() {
		return currentDateTime;
	}

	final int getQos() {
		return qos;
	}

}
