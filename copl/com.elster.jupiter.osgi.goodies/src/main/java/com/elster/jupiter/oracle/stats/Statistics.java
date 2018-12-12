/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Statistics extends SnapShot<Stat> {

	final Stat newRecord(ResultSet rs) throws SQLException {
		return new Stat(rs,1);
	}
	
	final Stat newRecord(String name) {
		return new Stat(name);
	}
	
}
