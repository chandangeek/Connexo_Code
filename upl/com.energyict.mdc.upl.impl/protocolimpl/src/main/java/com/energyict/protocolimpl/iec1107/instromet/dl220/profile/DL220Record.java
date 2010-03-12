/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import java.io.IOException;
import java.util.Date;

/**
 * Definition of a DL220Record
 * 
 * @author gna
 * @since 11-mrt-2010
 *
 */
public interface DL220Record {

	/**
	 * @return the Interval TIME
	 */
	public abstract Date getEndTime() throws IOException;

	/**
	 * @return the Interval VALUE
	 */
	public abstract String getValue() throws IOException;

	/**
	 * @return the Interval STATUS
	 */
	public abstract String getStatus() throws IOException;

	/**
	 * @return the Interval EVENT
	 */
	public abstract String getEvent() throws IOException;

}