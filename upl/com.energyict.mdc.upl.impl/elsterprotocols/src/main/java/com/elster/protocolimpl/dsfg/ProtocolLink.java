/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.elster.protocolimpl.dsfg;

import com.elster.protocolimpl.dsfg.connection.DsfgConnection;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;


/**
 * 
 * @author Koen
 */
public interface ProtocolLink {

	DsfgConnection getDsfgConnection();

	TimeZone getTimeZone();

	boolean isIEC1107Compatible();

	int getNumberOfChannels() throws UnsupportedException, IOException;

	String getPassword();

	byte[] getDataReadout();

	int getProfileInterval() throws UnsupportedException, IOException;

	Logger getLogger();

	int getNrOfRetries();

	boolean isRequestHeader();
	
	/* added getter for dsfg data */
	String getArchiveInstance();
	
	String getRegistrationInstance();

}
