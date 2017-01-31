/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.service.event.Event;

public abstract class SnapShot<T extends Record> {

	static final String SERVERSESSIONS =
			"select inst_id , sid from gv$session where machine = sys_context('userenv','host') and osuser = " +
			"(select osuser from v$session where sid = sys_context('userenv','sid'))";			
	
	private final Map<String,T> records = new HashMap<>();
	private Date date;
	
	final void init(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(getSql())) {
			try (ResultSet rs = statement.executeQuery()) {
				while(rs.next()) {
					T record = newRecord(rs);
					records.put(record.getName(),record);
				}
			};
		}
		this.date = new Date();
	}
	
	final void update(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(getSql())) {
			try (ResultSet rs = statement.executeQuery()) {
				while(rs.next()) {
					T record = records.get(rs.getString(1));
					if (record == null) {
						record = newRecord(rs.getString(1));
						records.put(record.getName(), record);
					} 
					record.update(rs,2);
				}
			};
		}
	}
	
	abstract String getSql();
	
	abstract T newRecord(ResultSet rs) throws SQLException;
	
	abstract T newRecord(String name);
	
	abstract String getTopic();
	
	Event toOsgiEvent() {
		Map<String,Object> props = new HashMap<>();
		props.put("startTime", date);
		for (String name : records.keySet()) {
			T record = records.get(name);
			if (record.isRelevant()) {
				props.put(name, record.contents());
			}
		}
		return new Event(getTopic(), props);
	}
	
	List<T> records() {
		List<T> result = new ArrayList<>();
		for (T record : records.values()) {
			if (record.isRelevant()) {
				result.add(record);
			}
		}
		return result;
	}
}
