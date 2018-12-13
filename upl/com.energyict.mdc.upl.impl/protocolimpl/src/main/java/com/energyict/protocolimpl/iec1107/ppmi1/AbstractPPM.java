package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec1107.ChannelMap;

import java.io.IOException;
import java.util.Date;

/**
 * This class is used to hide all the unused methods of MeterProtocol.
 *
 * @author jme
 *
 */
abstract class AbstractPPM extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol {

	@Override
	public void setRegister(String name, String value) throws UnsupportedException {
		throw new UnsupportedException();
	}

	@Override
	public void initializeDevice() throws UnsupportedException {
		throw new UnsupportedException();
	}

	@Override
	public void release() {
	}

	public ChannelMap getChannelMap() {
		return null;
	}

	@Override
	public String getFirmwareVersion() throws IOException {
		return "unknown";
	}

	@Override
	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		return getProfileData(new Date(), new Date(), includeEvents);
	}

	@Override
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	@Override
	public String getRegister(String name) throws UnsupportedException {
		throw new UnsupportedException();
	}

	@Override
	public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
		enableHHUSignOn(commChannel, false);
	}

}