/*
 * C12ProtocolLink.java
 *
 * Created on 16 oktober 2005, 17:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
public interface C12ProtocolLink {

	C12Layer2 getC12Layer2();

	TimeZone getTimeZone();

	int getNumberOfChannels() throws UnsupportedException, IOException;

	ProtocolChannelMap getProtocolChannelMap();

	int getProfileInterval() throws UnsupportedException, IOException;

	Logger getLogger();

	PSEMServiceFactory getPSEMServiceFactory();

	StandardTableFactory getStandardTableFactory();

	int getInfoTypeRoundtripCorrection();

	AbstractManufacturer getManufacturer();

	/**
	 * In case of GE KV and KV2 meters, returns
	 * getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
	 */
	int getMeterConfig() throws IOException; // meter specific

}
