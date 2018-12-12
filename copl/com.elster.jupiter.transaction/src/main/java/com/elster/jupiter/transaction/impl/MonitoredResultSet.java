/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MonitoredResultSet extends ResultSetWrapper {
	
	private int fetchCount;
	
	MonitoredResultSet(ResultSet resultSet, Statement statement) {
		super(resultSet, statement);
	}

	int getFetchCount() {
		return fetchCount;
	}
	
	@Override
	public boolean next() throws SQLException {
		boolean result = super.next();
		if (result) {
			fetchCount++;
		}
		return result;
	}

}
