/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Stat extends Record {
	private long initialValue;
	private long value;
	
	Stat(String name) {
		super(name);
	}
	
	Stat(ResultSet rs, int startIndex) throws SQLException {
		super(rs,startIndex++);
		this.initialValue = rs.getLong(startIndex++);
	}
	
	void update(ResultSet rs, int startIndex) throws SQLException {
		this.value = rs.getLong(startIndex++);
	}
	
	long getValue() {
		return value - initialValue;
	}

	@Override
	boolean isRelevant() {
		return getValue() > 0;
	}
	
	@Override
	public String contents() {
		return "" + getValue();
	}
	
}
