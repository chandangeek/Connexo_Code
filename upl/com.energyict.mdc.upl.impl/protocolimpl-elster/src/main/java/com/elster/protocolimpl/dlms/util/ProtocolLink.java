/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.elster.protocolimpl.dlms.util;

import com.elster.protocolimpl.dlms.HasSimpleObjectManager;
import com.elster.protocolimpl.dlms.connection.DlmsConnection;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;


public interface ProtocolLink extends HasSimpleObjectManager {

	DlmsConnection getDlmsConnection();

	TimeZone getTimeZone();

	boolean isIEC1107Compatible();

	int getNumberOfChannels() throws UnsupportedException, IOException;

	String getPassword();

	byte[] getDataReadout();

	int getProfileInterval() throws UnsupportedException, IOException;

	Logger getLogger();

	int getNrOfRetries();

	boolean isRequestHeader();
}
