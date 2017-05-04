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

abstract public class AbstractRadioCommand {

	enum RadioCommandId {

		RSSILevel(0x20),
		FirmwareVersion(0x28),
		WriteParameter(0x19),
		AlarmRoute(0x0A),
		ReadParameter(0x18);

		private int commandId;

		final int getCommandId() {
			return commandId;
		}

		RadioCommandId(final int commandId) {
			this.commandId=commandId;
		}
	} // enum RadioCommandId

	/**
	 * The reference to the ProtocolLink protocol implementation class
	 */
	private ProtocolLink protocolLink;

	final ProtocolLink getProtocolLink() {
		return protocolLink;
	}

	AbstractRadioCommand(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}

	abstract void parse(byte[] data) throws IOException;
	abstract byte[] prepare() throws IOException;
	abstract RadioCommandId getRadioCommandId();

	void invoke() throws IOException {
		int retry=0;
		while(true) {
			ByteArrayOutputStream baos = null;
			try {
				baos = new ByteArrayOutputStream();
				DataOutputStream daos = new DataOutputStream(baos);
				daos.writeByte(getRadioCommandId().getCommandId());
				daos.write(prepare()); // write 1 parameter
				parseRead(getProtocolLink().getWaveFlowConnect().sendData(baos.toByteArray()));
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

	private final void parseRead(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
				throw new WaveFlowDLMSException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {

				byte[] temp = new byte[dais.available()];
				dais.read(temp);
				parse(temp);
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
}
