package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * class to read a lis200 status object
 * 
 * @author heuckeg
 * @since 6/9/2010
 */

public class StatusObject extends SimpleObject {

	/**
	 * constructor for status object
	 * 
	 * @param link
	 *            - base ProtocolLink class
	 * @param instance
	 *            - of status object address
	 * @param startAddress
	 *            - rest of status object address
	 */
	public StatusObject(ProtocolLink link, int instance, String startAddress) {
		super(link, instance, startAddress);
	}

	/**
	 * Getter for status as an integer (normally status is read a sequence of bit
	 * numbers)
	 * 
	 * @return status as an integer
	 * @throws java.io.IOException
	 */
	public int getStatusInt() throws IOException {
		//String status = getValue(); 
		String status = readRawValue();
		
		int i = status.indexOf("(");
		status = status.substring(i);
		
		return statusToInt(status);
	}

	/**
	 * static function to convert sequence of bits ("(1)(5)(16)"to an integer
	 * 
	 * @param status
	 *            - string with bit numbers
	 * @return integer 
	 */
	public static int statusToInt(String status) {

		int result = 0;

		status = status.replace("(", "");
		status = status.replace(")", ";");
		String[] codes = status.split(";");
		for (String code : codes) {
			int i = Integer.valueOf(code);
			result |= (1 << (i - 1));
		}

		return result;
	}
}
