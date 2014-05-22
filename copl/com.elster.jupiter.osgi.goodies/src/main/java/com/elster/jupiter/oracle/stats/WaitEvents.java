package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class WaitEvents extends SnapShot<WaitEvent> {

	final WaitEvent newRecord(ResultSet rs) throws SQLException {
		return new WaitEvent(rs,1);
	}
	
	final WaitEvent newRecord(String name) {
		return new WaitEvent(name);
	}
}
