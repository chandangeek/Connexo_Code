package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;

/**
 * Copyrights EnergyICT
 *
 * @since 12-apr-2010 14:19:45
 * @author jme
 */
public interface EDevice {

	/**
	 * @return The logger
	 */
	Logger getLogger();

	/**
	 * @return the rtu
	 */
	Rtu getMeter();

	/**
	 * @return
	 */
	CosemObjectFactory getCosemObjectFactory();

	/**
	 * @param channel
	 */
	Calendar getFromCalendar(final Channel channel);

	/**
	 * @return
	 */
	Calendar getToCalendar();

	/**
	 * @return
	 */
	TimeZone getTimeZone();

	/**
	 * @return
	 * @throws IOException
	 */
	String getSerialNumber() throws IOException;

	/**
	 * @return
	 */
	DLMSMeterConfig getMeterConfig();

	/**
	 * @return
	 */
	int getPhysicalAddress();

}
