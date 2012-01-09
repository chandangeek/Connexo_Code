package com.elster.protocolimpl.lis200.objects;

import com.elster.protocolimpl.lis200.LIS200Utils;
import com.elster.protocolimpl.lis200.Lis200Value;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings({"unused"})
public class MaxDemandObject extends SimpleObject {

	private String readData = "";

	/**
	 * constructor for max demand object
	 * 
	 * @param link
	 *            - base ProtocolLink class
	 * @param instance
	 *            - of status object address
	 * @param startAddress
	 *            - rest of status object address
	 */
	public MaxDemandObject(ProtocolLink link, int instance, String startAddress) {
		super(link, instance, startAddress);
	}

	/**
	 * private function to fill a string with the data
	 * 
	 * @throws IOException - in case of an error
	 */
	private void doReadValue() throws IOException {
		readData = readRawValue();
	}

	/**
	 * getMaxValue reads the max demand value and gives back the read value. The
	 * according time stamp has to be read with getMaxDate.
	 * 
	 * @return value - string with read max demand value
     * @throws IOException - in case of an error
	 */
	public String getMaxValue() throws IOException {
		doReadValue();
		String rawData = LIS200Utils.getTextBetweenBracketsFromIndex(readData,
				0);
		String[] data = rawData.split("[*]");

		return data[0];
	}

	/**
	 * getMaxValueUnit reads the max demand value and gives back the read unit
	 * of the value. The according time stamp has to be read with getMaxDate.
	 * 
	 * @return value - string with unit of value
     * @throws IOException - in case of an error
	 */
	public String getMaxValueUnit() throws IOException {
		if (readData.length() == 0)
			doReadValue();
		String rawData = LIS200Utils.getTextBetweenBracketsFromIndex(readData,
				0);
		String[] data = rawData.split("[*]");

		return data[1];
	}

	/**
	 * Getter for value as Lis200Value
	 * 
	 * @return lis200Value
     * @throws IOException - in case of an error
	 */
	public Lis200Value getLis200Value() throws IOException {
		if (readData.length() == 0)
			doReadValue();
		String rawData = LIS200Utils.getTextBetweenBracketsFromIndex(readData,
				0);
		return new Lis200Value(rawData);
	}

	/**
	 * private function to get max date as calendar...
	 * 
	 * @return date of max value
     * @throws IOException - in case of an error
	 */
	public Calendar getMaxCalendar() throws IOException {
		if (readData.length() == 0)
			doReadValue();

		String rawData = LIS200Utils.getTextBetweenBracketsFromIndex(readData,
				1);

		return ClockObject.parseCalendar(rawData, false, link.getTimeZone());
	}

	/**
	 * getMaxDate gets the time stamp for the read max demand value
	 * 
	 * @return date
     * @throws IOException - in case of an error
	 */
	public Date getMaxDate() throws IOException {
		return getMaxCalendar().getTime();
	}
}
