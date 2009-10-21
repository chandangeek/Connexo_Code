package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.math.BigDecimal;
import java.util.Date;

public class Interval {

	private Date			date;
	private BigDecimal[]	value;
	private ProfileParser	profileParser;

	public Interval(ProfileParser profileParser) {
		this.profileParser = profileParser;
		this.value = new BigDecimal[getProfileParser().getLoadDef().getNrOfChannels()];
	}

	private ProfileParser getProfileParser() {
		return profileParser;
	}

	boolean isEmpty() {
		for (int i = 0; i < this.value.length; i++) {
			if (this.value[i] != null) {
				return false;
			}
		}
		return true;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal[] getValue() {
		return value;
	}

	public BigDecimal getValue(int index) {
		return value[index];
	}

	public void setValue(BigDecimal value, int index) {
		this.value[index] = value;
	}

	@Override
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
