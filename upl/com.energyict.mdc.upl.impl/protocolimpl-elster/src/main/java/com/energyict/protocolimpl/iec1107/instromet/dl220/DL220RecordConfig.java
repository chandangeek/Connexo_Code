/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.io.IOException;

/**
 * Definition of a DL220Record configuration.<br>
 * Mainly it describes the indexes of the records
 * 
 * @author gna
 * @since 11-mrt-2010
 *
 */
public interface DL220RecordConfig {

	/**
	 * @return the timeIndex
	 */
	public abstract int getTimeIndex() throws IOException;

	/**
	 * @param index
	 * 			- the index of the value to return (if more then one channel is configured) 		
	 * @return the valueIndex
	 */
	public abstract int getValueIndex(int index) throws IOException;

	/**
	 * @return the statusIndex
	 */
	public abstract int getStatusIndex() throws IOException;

	/**
	 * @return the errorIndex
	 */
	public abstract int getEventIndex() throws IOException;

	/**
	 * @return the number of objects per record
	 * @throws IOException 
	 */
	public abstract int getNumberOfObjectsPerRecord() throws IOException;

}