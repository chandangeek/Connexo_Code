/*
 * ProtocolLink.java
 *
 * Created on 24 maart 2004, 17:40
 */

package com.energyict.protocolimpl.pact.core.common;

import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.protocol.UnsupportedException;

/**
 * 
 * @author Koen
 */
public interface ProtocolLink {

	TimeZone getTimeZone();

	TimeZone getRegisterTimeZone();

	PACTConnection getPactConnection();

	ChannelMap getChannelMap();

	int getProfileInterval() throws UnsupportedException, java.io.IOException;

	PACTToolkit getPACTToolkit();

	PACTMode getPACTMode();

	Logger getLogger();

	boolean isExtendedLogging();

	boolean isStatusFlagChannel();

	int getForcedRequestExtraDays();

	int getModulo();

	boolean isMeterTypeICM200();

	boolean isMeterTypeCSP();

}
