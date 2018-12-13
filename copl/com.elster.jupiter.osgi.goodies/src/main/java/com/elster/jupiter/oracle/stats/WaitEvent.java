/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WaitEvent extends Record {
	private long initialTotalWaits;
	private long initialTimeWaited;
	private long totalWaits;
	private long timeWaited;
	 
	WaitEvent(String name) {
		super(name);
	}
	
	WaitEvent(ResultSet rs, int startIndex) throws SQLException {
		super(rs,startIndex++);
		this.initialTotalWaits = rs.getLong(startIndex++);
		this.initialTimeWaited = rs.getLong(startIndex++);
	}
	
	void update(ResultSet rs, int startIndex) throws SQLException {
		this.totalWaits = rs.getLong(startIndex++);
		this.timeWaited = rs.getLong(startIndex++);
	}
	
	long getWaits() {
		return totalWaits - initialTotalWaits;
	}
	
	long getTimeWaited() {
		return timeWaited - initialTimeWaited;
	}

	@Override
	boolean isRelevant() {
		return getWaits() > 0;
	}
	
	@Override
	public String contents() {
		return "(" + getWaits() + "," + getTimeWaited() + "," + getTimeWaited()/getWaits() + ")";
	}
		
}
