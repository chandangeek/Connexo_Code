package com.energyict.protocolimpl.base;

import java.io.IOException;
import java.util.Date;

/**
 * Interface for classes, used for clock control of a device
 *
 * @author jme
 */
public interface ClockController {

	/**
	 * Set the clock in the device to the current machine time
	 * @throws IOException
	 */
	void setTime() throws IOException;

	/**
	 * Shift the clock in the device to the current machine time
	 * @throws IOException
	 */
	void shiftTime() throws IOException;


	/**
	 * Set the clock in the device to the time, given in the {@link Date} parameter
	 * @param dateTime is a {@link Date}
	 * @throws IOException
	 */
	void setTime(Date date) throws IOException;

	/**
	 * Shift the clock in the device to the time, given in the {@link Date} parameter
	 * @param dateTime is a {@link Date}
	 * @throws IOException
	 */
	void shiftTime(Date date) throws IOException;

	/**
	 * Read the current time from the device
	 * @return the current date and time as a {@link Date}
	 * @throws IOException
	 */
	Date getTime() throws IOException;


}
