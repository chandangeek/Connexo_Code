/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Record {
	
	private final String name;
	
	Record(String name) {
		this.name = name;
	}
	
	Record(ResultSet rs, int startIndex) throws SQLException {
		this(rs.getString(startIndex));
	}
	
	abstract void update(ResultSet rs, int startIndex) throws SQLException;
	
	abstract boolean isRelevant();
	
	abstract String contents();
	
	final String getName() {
		return name;
	}
	
	
}
