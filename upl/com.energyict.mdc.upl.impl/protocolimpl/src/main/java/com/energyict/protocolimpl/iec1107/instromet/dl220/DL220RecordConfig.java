/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

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
	public abstract int getTimeIndex();

	/**
	 * @return the valueIndex
	 */
	public abstract int getValueIndex();

	/**
	 * @return the statusIndex
	 */
	public abstract int getStatusIndex();

	/**
	 * @return the errorIndex
	 */
	public abstract int getEventIndex();

}