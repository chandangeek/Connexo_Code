/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.energyict.protocolimpl.iec1107;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ProtocolChannelMap;


/**
 * 
 * @author Koen
 */
public interface ProtocolLink {

	FlagIEC1107Connection getFlagIEC1107Connection();

	TimeZone getTimeZone();

	boolean isIEC1107Compatible();

	int getNumberOfChannels() throws UnsupportedException, IOException;

	String getPassword();

	byte[] getDataReadout();

	int getProfileInterval() throws UnsupportedException, IOException;

	/**
	 * @deprecated use getProtocolChannelMap()
	 */
	@Deprecated
	ChannelMap getChannelMap();

	ProtocolChannelMap getProtocolChannelMap();

	Logger getLogger();

	int getNrOfRetries();

	boolean isRequestHeader();

}
