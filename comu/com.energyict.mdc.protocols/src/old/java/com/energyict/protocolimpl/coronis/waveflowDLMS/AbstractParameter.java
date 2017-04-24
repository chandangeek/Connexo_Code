/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

abstract public class AbstractParameter extends AbstractRadioCommand {

	static final int PARAM_UPDATE_OK=0x00;
	static final int PARAM_UPDATE_ERROR=0xFF;

	enum ParameterId {

		ApplicationStatus(0x01,1,"Application Status (R/W)"),
		CurrentRTC(0x04,7,"Current time and date (R/W)"),
		AlarmConfiguration(0x58,1,"alarm configuration (R/W)"),
		Version(0x56,2,"Version (R)");

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


	AbstractParameter(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	abstract ParameterId getParameterId();

	void write() throws IOException {
		int retry=0;
		while(true) {
			ByteArrayOutputStream baos = null;
			try {
				baos = new ByteArrayOutputStream();
				DataOutputStream daos = new DataOutputStream(baos);
				daos.writeByte(RadioCommandId.WriteParameter.getCommandId());
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

				parseWriteResponse(getProtocolLink().getWaveFlowConnect().sendData(baos.toByteArray()));
				return;
			}
			catch(ConnectionException e) {
				if (retry++ >= getProtocolLink().getInfoTypeProtocolRetriesProperty()) {
					throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+getProtocolLink().getInfoTypeProtocolRetriesProperty()+"] reties!");
				}
				else {
					getProtocolLink().getLogger().warning(e.getMessage()+", retry ["+retry+"]");
				}
			}
			catch(WaveFlowDLMSException e) {
				if (retry++ >= getProtocolLink().getInfoTypeProtocolRetriesProperty()) {
					throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+getProtocolLink().getInfoTypeProtocolRetriesProperty()+"] reties!");
				}
				else {
					getProtocolLink().getLogger().warning(e.getMessage()+", retry ["+retry+"]");
				}
			}
			finally {
				if (baos != null) {
					try {
						baos.close();
					}
					catch(IOException e) {
						getProtocolLink().getLogger().severe(ProtocolUtils.stack2string(e));
					}
				}
			}
		}
	}

	private final void parseWriteResponse(final byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | RadioCommandId.WriteParameter.getCommandId())) {
				throw new WaveFlowDLMSException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {

				operatingMode = dais.readShort();

				if (getParameterId()!=null) {
					int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
					if (nrOfParameters != 1) {
						throw new WaveFlowDLMSException("Writing only 1 parameter at a time allowed, returned ["+nrOfParameters+"] parameters!");
					}

					ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
					if (pid != getParameterId()) {
						throw new WaveFlowDLMSException("Invalid parameter returned expected ["+getParameterId()+"], returned ["+pid+"]");
					}
				}

				int result = WaveflowProtocolUtils.toInt(dais.readByte());
				if (result != PARAM_UPDATE_OK) {
					throw new WaveFlowDLMSException("Update parameter ["+getParameterId()+"] failed. Result code ["+WaveflowProtocolUtils.toHexString(result)+"]");
				}
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getProtocolLink().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}

	void read() throws IOException {
		int retry=0;
		while(true) {
			ByteArrayOutputStream baos = null;
			try {
				baos = new ByteArrayOutputStream();
				DataOutputStream daos = new DataOutputStream(baos);
				daos.writeByte(RadioCommandId.ReadParameter.getCommandId());
				if (getParameterId()==null) {
					daos.writeByte(0); // write 0 parameter, only update the operating mode
				}
				else {
					daos.writeByte(1); // write 1 parameter
					daos.writeByte(getParameterId().id);
					daos.writeByte(getParameterId().length);
				}

				parseReadResponse(getProtocolLink().getWaveFlowConnect().sendData(baos.toByteArray()));
				return;
			}
			catch(ConnectionException e) {
				if (retry++ >= getProtocolLink().getInfoTypeProtocolRetriesProperty()) {
					throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+getProtocolLink().getInfoTypeProtocolRetriesProperty()+"] reties!");
				}
				else {
					getProtocolLink().getLogger().warning(e.getMessage()+", retry ["+retry+"]");
				}
			}
			catch(WaveFlowDLMSException e) {
				if (retry++ >= getProtocolLink().getInfoTypeProtocolRetriesProperty()) {
					throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+getProtocolLink().getInfoTypeProtocolRetriesProperty()+"] reties!");
				}
				else {
					getProtocolLink().getLogger().warning(e.getMessage()+", retry ["+retry+"]");
				}
			}
			finally {
				if (baos != null) {
					try {
						baos.close();
					}
					catch(IOException e) {
						getProtocolLink().getLogger().severe(ProtocolUtils.stack2string(e));
					}
				}
			}

		}

	}

	private final void parseReadResponse(final byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | RadioCommandId.ReadParameter.getCommandId())) {
				throw new WaveFlowDLMSException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {

				operatingMode = dais.readShort();

				if (getParameterId()!=null) {
					int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
					if (nrOfParameters != 1) {
						throw new WaveFlowDLMSException("Reading only 1 parameter at a time allowed, returned ["+nrOfParameters+"] parameters!");
					}

					ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
					if (pid != getParameterId()) {
						throw new WaveFlowDLMSException("Invalid parameter returned expected ["+getParameterId()+"], returned ["+pid+"]");
					}

					int length = WaveflowProtocolUtils.toInt(dais.readByte());
					if (length != getParameterId().length) {
						throw new WaveFlowDLMSException("Invalid length returned expected ["+getParameterId().length+"], returned ["+length+"]");
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
					getProtocolLink().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}

	/**
	 * Because we have implement a abstract parameter read/write class, we don't use this method...
	 */
	RadioCommandId getRadioCommandId() {
		return null;
	}

} // abstract public class AbstractParameter
