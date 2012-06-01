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

import com.energyict.protocol.UnsupportedException;
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

    /**
     * Custom property to indicate if a non-matching checksum in an com.energyict.protocolimpl.ansi.c12.ReadResponse should be ignored.
     * If false, the non-matching checksum will generate an IOException.
     * If true, the non-matching checksum will be silently ignored.
     * @return
     */
    boolean ignoreChecksumFaults();
}
