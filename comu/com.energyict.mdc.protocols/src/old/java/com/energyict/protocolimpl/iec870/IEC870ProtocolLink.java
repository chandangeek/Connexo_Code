/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.energyict.protocolimpl.iec870;

import com.energyict.mdc.upl.UnsupportedException;

import java.io.IOException;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public interface IEC870ProtocolLink {

	IEC870Connection getIEC870Connection();

	TimeZone getTimeZone();

	int getNumberOfChannels() throws UnsupportedException, IOException;

	String getPassword();

	int getProfileInterval() throws UnsupportedException, IOException;

}
