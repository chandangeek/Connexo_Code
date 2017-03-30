/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author jme
 *
 */
public class Interval {

	private Date			date;
	private BigDecimal[]	value;
	private ProfileParser	profileParser;

	/**
	 * @param profileParser
	 */
	public Interval(ProfileParser profileParser) {
		this.profileParser = profileParser;
		this.value = new BigDecimal[getProfileParser().getLoadDef().getNrOfChannels()];
	}

	/**
	 * @return
	 */
	private ProfileParser getProfileParser() {
		return profileParser;
	}

	/**
	 * Check if the interval contains elements
	 * @return
	 */
	boolean isEmpty() {
		for (int i = 0; i < this.value.length; i++) {
			if (this.value[i] != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Getter for the date of the interval
	 * @return The date of the interval
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Setter for the date of the interval
	 * @param date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return
	 */
	public BigDecimal[] getValue() {
		return value;
	}

	/**
	 * @param index
	 * @return
	 */
	public BigDecimal getValue(int index) {
		return value[index];
	}

	/**
	 * @param value
	 * @param index
	 */
	public void setValue(BigDecimal value, int index) {
		this.value[index] = value;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		for (int i = 0; i < this.value.length; i++) {
			sb.append(this.value[i]);
			sb.append(" ");
		}
		sb.append(" ]");
		return sb.toString();
	}

}
