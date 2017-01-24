package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

abstract public class AbstractParameter extends AbstractRadioCommand {

	static final int PARAM_UPDATE_OK=0x00;
	static final int PARAM_UPDATE_ERROR=0xFF;

	enum ParameterId {

		// referenced doc "Waveflow 100mw encoder applicative specifications (CS-FW-SFW-WF_100mW_Encoder-E08)"

		ApplicationStatusEncoder(0x01,1,"Application Status Encoder"), // page 12
		ApplicationStatusMbus(0x01,2,"Application Status Mbus"), // page 12
		LeakageDetectionStatus(0x02,1,"Leakage Detection Status"), // page 12
		CurrentRTC(0x04,7,"Current time and date"), // page 13
		SamplingPeriod(0x07,1,"Reading the sampling period"), // page 14
		SamplingActivationType(0x08,1,"Sampling activation (00=immediate, 01 on next hour)"), // partioally missing in the document
		MeasurementPeriod(0x10,1,"Measurement period (in multiples of sampling periods)"), // page 16
		DayOfWeek(0x12,1,"Day of the week or month to data log"), // page 15
		HourOfMeasurement(0x13,1,"Hour in day of week or month to datalog"),
		NrOfLoggedRecords(0x14,2,"Number of records in the datalogging table"), // page 15
		EncoderModelPortA(0x1D,2,"Encoder model on port A"), // page 18
		EncoderModelPortB(0x1E,2,"Encoder model on port B"), // page 18
		EncoderUnitPortA(0x1F,2,"Encoder unit on port A"), // page 19
		EncoderUnitPortB(0x20,2,"Encoder unit on port B"), // page 19
		BatteryLifeDurationCounter(0x50,3,"Battery life duration counter"),
		BatteryLifeDateEnd(0x51,7,"Battery life end date"),
		BackflowDetectionDatePortA(0x3E,7,"Backflow detection date port A"),
		BackflowDetectionDatePortB(0x3F,7,"Backflow detection date port B"),
		BackflowDetectionFlagsPortA(0x43,2,"Backflow detection flags port A"),
		BackflowDetectionFlagsPortB(0x47,2,"Backflow detection flags port B"),
		CommunicationErrorDetectionDatePortA(0x4C,7,"communication error detection date port A"),
		CommunicationErrorDetectionDatePortB(0x4D,7,"communication error detection date port B"),
		CommunicationErrorReadingDatePortA(0x4E,7,"reading error detection date port A"),
		CommunicationErrorReadingDatePortB(0x4F,7,"reading error detection date port B"),
		AlarmConfiguration(0x58,1,"Alarm configuration");

		private int id;
		private int length;
		private String description;

		ParameterId(final int id, final int length, final String description) {
			this.id=id;
			this.length=length;
			this.description=description;
		}

		public String toString() {
			return WaveflowProtocolUtils.toHexString(id)+", "+description;
		}

		static ParameterId fromId(final int id) {
			for (ParameterId pid : values()) {
				if (pid.id == id) {
					return pid;
				}
			}
			return null;
		}

	} // enum ParameterId

	/**
	 * The operating mode of the waveflow device is implicit to the write and read command
	 */
	private int operatingMode;

	/**
	 * Operating mode write mask
	 */
	private int mask=0xffff;

	final void setMask(int mask) {
		this.mask = mask;
	}

	final int getOperatingMode() {
		return operatingMode;
	}

	final void setOperatingMode(int operatingMode) {
		this.operatingMode = operatingMode;
	}


	AbstractParameter(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	abstract ParameterId getParameterId();

	void write() throws IOException {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(EncoderRadioCommandId.WriteParameter.getCommandId());
			if (getParameterId()==null) {
				daos.writeShort(operatingMode); // update the operating mode
				daos.writeShort(mask); // mask to update the operating mode
				daos.writeByte(0); // write 0 parameter, only update the operating mode
			}
			else {
				daos.writeShort(0); // don't update the operating mode, value don't care
				daos.writeShort(0); // don't update the operating mode, mask = 0
				daos.writeByte(1); // write 1 parameter
				daos.writeByte(getParameterId().id);
				daos.writeByte(getParameterId().length);
				daos.write(prepare());
			}

			parseWriteResponse(getWaveFlow100mW().getWaveFlowConnect().sendData(baos.toByteArray()));

		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}

	private void parseWriteResponse(final byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | EncoderRadioCommandId.WriteParameter.getCommandId())) {
				throw new WaveFlow100mwEncoderException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {

				operatingMode = dais.readShort();

				if (getParameterId()!=null) {
					int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
					if (nrOfParameters != 1) {
						throw new WaveFlow100mwEncoderException("Writing only 1 parameter at a time allowed, returned ["+nrOfParameters+"] parameters!");
					}

					ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
					if (pid.id != getParameterId().id) {
						throw new WaveFlow100mwEncoderException("Invalid parameter returned expected ["+getParameterId()+"], returned ["+pid+"]");
					}
				}

				int result = WaveflowProtocolUtils.toInt(dais.readByte());
				if (result != PARAM_UPDATE_OK) {
					throw new WaveFlow100mwEncoderException("Update parameter ["+getParameterId()+"] failed. Result code ["+WaveflowProtocolUtils.toHexString(result)+"]");
				}
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}

	void read() throws IOException {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(EncoderRadioCommandId.ReadParameter.getCommandId());
			if (getParameterId()==null) {
				daos.writeByte(0); // write 0 parameter, only update the operating mode
			}
			else {
				daos.writeByte(1); // write 1 parameter
				daos.writeByte(getParameterId().id);
				daos.writeByte(getParameterId().length);
			}

			parseReadResponse(getWaveFlow100mW().getWaveFlowConnect().sendData(baos.toByteArray()));

		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}

	}

	private final void parseReadResponse(final byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | EncoderRadioCommandId.ReadParameter.getCommandId())) {
				throw new WaveFlow100mwEncoderException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {

				operatingMode = dais.readShort();

				if (getParameterId()!=null) {
					int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
					if (nrOfParameters != 1) {
						throw new WaveFlow100mwEncoderException("Reading only 1 parameter at a time allowed, returned ["+nrOfParameters+"] parameters!");
					}

					ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
					if (pid != getParameterId()) {
						throw new WaveFlow100mwEncoderException("Invalid parameter returned expected ["+getParameterId()+"], returned ["+pid+"]");
					}

					int length = WaveflowProtocolUtils.toInt(dais.readByte());
					if (length != getParameterId().length) {
						throw new WaveFlow100mwEncoderException("Invalid length returned expected ["+getParameterId().length+"], returned ["+length+"]");
					}

					byte[] resultData = new byte[dais.available()];
					dais.read(resultData);
					parse(resultData);
				}
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}

	/**
	 * Because we have implement a abstract parameter read/write class, we don't use this method...
	 */
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return null;
	}

} // abstract public class AbstractParameter
