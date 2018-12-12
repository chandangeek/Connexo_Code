/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SqlExecution extends Record {
	private long initialExecutions;
	private long initialElapsed;
	private long executions;
	private long elapsed;
	private String sqlText;
	 
	SqlExecution(String name) {
		super(name);
	}
	
	SqlExecution(ResultSet rs, int startIndex) throws SQLException {
		super(rs,startIndex++);
		this.initialExecutions = rs.getLong(startIndex++);
		this.initialElapsed = rs.getLong(startIndex++);
		this.sqlText = rs.getString(startIndex++);
	}
	
	void update(ResultSet rs, int startIndex) throws SQLException {
		this.executions = rs.getLong(startIndex++);
		this.elapsed = rs.getLong(startIndex++);
		this.sqlText = rs.getString(startIndex++);
	}
	
	@XmlElement
	String getSqlId() {
		return getName();
	}
	
	@XmlElement
	long getExecutions() {
		return executions - initialExecutions;
	}
	
	@XmlElement
	long getElapsed() {
		return elapsed - initialElapsed;
	}
	
	@XmlElement
	long getElapsedPerExecution() {
		return getExecutions() == 0 ? 0 : getElapsed() / getExecutions();
	}
	
	@XmlElement
	String getSqlText() {
		return sqlText;
	}

	@Override
	boolean isRelevant() {
		return getExecutions() > 0;
	}
	
	@Override
	public String contents() {
		return "(" + getExecutions() + "," + getElapsed() + "," + getExecutions()/getElapsed() + "," + sqlText + ")";
	}
		
}
