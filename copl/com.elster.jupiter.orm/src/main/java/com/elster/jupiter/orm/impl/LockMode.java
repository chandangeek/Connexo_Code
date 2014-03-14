package com.elster.jupiter.orm.impl;

public enum LockMode {
	NONE(""),
	WAIT("FOR UPDATE "),
	NOWAIT("FOR UPDATE NOWAIT"),
	SKIP("FOR UPDATE SKIP LOCKED");
	
	private final String sql;
	
	private LockMode(String sql) {
		this.sql = sql;
	}
	
	String toSql() {
		return sql;
	}
}
