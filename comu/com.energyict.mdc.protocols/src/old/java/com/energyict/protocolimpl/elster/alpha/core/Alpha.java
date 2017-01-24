/*
 * Alpha.java
 *
 * Created on 27 september 2005, 13:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author koen
 */
public interface Alpha {

	TimeZone getTimeZone();

	AlphaConnection getAlphaConnection();

	CommandFactory getCommandFactory();

	BillingDataRegisterFactory getBillingDataRegisterFactory() throws IOException;

	int getNumberOfChannels() throws UnsupportedException, IOException;

	ProtocolChannelMap getProtocolChannelMap();

	int getProfileInterval() throws UnsupportedException, IOException;

	Logger getLogger();

	int getTotalRegisterRate();

}
