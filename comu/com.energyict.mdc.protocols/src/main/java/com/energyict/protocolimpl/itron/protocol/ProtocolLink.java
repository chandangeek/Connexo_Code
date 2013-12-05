/*
 * ProtocolLink.java
 *
 * Created on 22 september 2006, 13:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol;

import com.energyict.protocolimpl.itron.protocol.schlumberger.CommandFactory;

import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public interface ProtocolLink {

	CommandFactory getCommandFactory();

	TimeZone getTimeZone();

	int getBlockSize();

}
