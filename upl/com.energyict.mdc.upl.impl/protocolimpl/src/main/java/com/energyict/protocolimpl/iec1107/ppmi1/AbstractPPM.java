package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.ChannelMap;

/**
 * This method is used to hide all the unused methods of MeterProtocol
 * 
 * @author jme
 * 
 */
public abstract class AbstractPPM implements MeterProtocol, HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol {

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String, java.lang.String)
	 */
	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
		throw new UnsupportedException();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.MeterProtocol#initializeDevice()
	 */
	public void initializeDevice() throws IOException, UnsupportedException {
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
	public Object fetchCache(int rtuid) throws SQLException, BusinessException {
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
	public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
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
	public String getFirmwareVersion() throws IOException, UnsupportedException {
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
	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
		throw new UnsupportedException();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)
	 */
	public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
		enableHHUSignOn(commChannel, false);
	}

}
