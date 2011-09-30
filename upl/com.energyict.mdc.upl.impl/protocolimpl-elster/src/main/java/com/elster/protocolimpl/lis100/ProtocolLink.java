/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.connection.Lis100Connection;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;


/**
 * 
 * @author Koen
 */
@SuppressWarnings({"unused"})
public interface ProtocolLink {

	Lis100Connection getLis100Connection();

	TimeZone getTimeZone();

	boolean isIEC1107Compatible();

	int getNumberOfChannels() throws IOException;

	String getPassword();

	byte[] getDataReadout();

	int getProfileInterval() throws IOException;

	Logger getLogger();

	int getNrOfRetries();

	boolean isRequestHeader();
}
