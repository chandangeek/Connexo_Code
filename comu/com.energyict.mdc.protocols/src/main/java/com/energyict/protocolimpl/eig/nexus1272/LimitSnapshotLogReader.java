package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class LimitSnapshotLogReader extends AbstractLogReader {


	public LimitSnapshotLogReader(OutputStream os ,NexusProtocolConnection npc) {
		outputStream = os;
		connection = npc;
		windowIndexAddress = new byte[] {(byte) 0x95, 0x03};;
		windowModeAddress = new byte[] {(byte) 0x95, 0x43};
		windowEndAddress = 38528;
	}

	@Override
	protected Command getHeaderCommand() {
		return NexusCommandFactory.getFactory().getLimitSnapshotLogHeaderCommand();
	}

	@Override
	protected Command getWindowCommand() {
		return NexusCommandFactory.getFactory().getLimitSnapshotLogWindowCommand();
	}

	protected byte[] readLogHeader() throws IOException {
		Command command = NexusCommandFactory.getFactory().getLimitSnapshotLogHeaderCommand();
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	@Override
	public void parseLog(byte[] LimitSnapshotLogData, ProfileData profileData, Date from, int intervalSeconds) throws IOException {
		throw new UnsupportedException();

	}
}
