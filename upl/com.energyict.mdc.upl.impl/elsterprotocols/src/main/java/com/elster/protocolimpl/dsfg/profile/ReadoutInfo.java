package com.elster.protocolimpl.dsfg.profile;

import java.util.Date;

/**
 * helper class to hold information for the read out time period respective the
 * corresponding archive line numbers
 * 
 * @author heuckeg
 * 
 */
public class ReadoutInfo {

	private Date readoutFrom;
	private Date readoutUntil;
	private long onoFrom;
	private long onoUntil;

	/**
	 * Constructor with given from and until date
	 *  
	 * @param from date
	 * @param until date
	 */
	public ReadoutInfo(Date from, Date until) {
		readoutFrom = from;
		readoutUntil = until;
		onoFrom = -1;
		onoUntil = -1;
	}

	/**
	 * neutral constructor
	 */
	public ReadoutInfo() {
		this(new Date(0), new Date(0));
	}

	/**
	 * Constructor with given archive line numbers
	 * 
	 * @param from
	 * @param until
	 */
	public ReadoutInfo(long from, long until) {
		this();
		onoFrom = from;
		onoUntil = until;
	}

	/** 
	 * Is range set via archive line numbers
	 * 
	 * @return true is onoFrom and onoUntil >= 0
	 */
	public boolean isOnoRangeSet() {
		return (onoFrom >= 0) && (onoUntil >= 0);
	}
	
	/**
	 * Getter for fromDate
	 * 
	 * @return fromDate
	 */
	public Date getFromDate() {
		return readoutFrom;
	}

	/**
	 * Getter for untilDate
	 * 
	 * @return untilDate
	 */
	public Date getUntilDate() {
		return readoutUntil;
	}

	/**
	 * Setter for starting archive line
	 * 
	 * @param from
	 */
	public void setOnoFrom(long from) {
		onoFrom = from;
	}
	
	/**
	 * Getter for starting archive line
	 * 
	 * @return onoFrom
	 */
	public long getOnoFrom() {
		return onoFrom;
	}

	/**
	 * Setter for ending archive line
	 * 
	 * @param from
	 */
	public void setOnoUntil(long until) {
		onoUntil = until;
	}
	
	/**
	 * Getter for ending archive line
	 * 
	 * @return onoUntil
	 */
	public long getOnoUntil() {
		return onoUntil;
	}
}

