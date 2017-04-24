package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec1107.ChannelMap;

import java.io.IOException;
import java.util.Date;

/**
 * This method is used to hide all the unused methods of MeterProtocol
 *
 * @author jme
 *
 */
public abstract class AbstractPPM extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol {

	public AbstractPPM(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	/* (non-Javadoc)
         * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String, java.lang.String)
         */
	public void setRegister(String name, String value) throws IOException {
		throw new UnsupportedException();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#initializeDevice()
	 */
	public void initializeDevice() throws IOException {
		throw new UnsupportedException();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#getCache()
	 */
	public Object getCache() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#fetchCache(int)
	 */
	public Object fetchCache(int rtuid) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#setCache(java.lang.Object)
	 */
	public void setCache(Object cacheObject) {
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#updateCache(int, java.lang.Object)
	 */
	public void updateCache(int rtuid, Object cacheObject) {
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#release()
	 */
	public void release() throws IOException {
	}

	/**
	 * @return
	 */
	public ChannelMap getChannelMap() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#getFirmwareVersion()
	 */
	public String getFirmwareVersion() throws IOException {
		return "unknown";
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
	 */
	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		return getProfileData(new Date(), new Date(), includeEvents);
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
	 */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String)
	 */
	public String getRegister(String name) throws IOException {
		throw new UnsupportedException();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)
	 */
	public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
		enableHHUSignOn(commChannel, false);
	}

}
