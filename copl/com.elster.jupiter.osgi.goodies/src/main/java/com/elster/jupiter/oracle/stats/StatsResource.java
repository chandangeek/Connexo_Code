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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.event.Event;

import com.google.common.collect.ImmutableMap;

@Path("/")
public class StatsResource {
	@Inject
	private StatService statService;
	@Inject
	private DataSource dataSource;
	
	@GET
	@Path("/statistics/")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public List<Map<String,Object>> getStatistics() {
		Event event = statService.getStats();
		Map<String,Object> sorted = new TreeMap<>();
		for (String key : event.getPropertyNames()) {
			sorted.put(key,event.getProperty(key));
		}
		List<Map<String,Object>> result = new ArrayList<>();
		for(Map.Entry<String, Object> entry : sorted.entrySet()) {
			result.add(ImmutableMap.of("name",entry.getKey(),"value",entry.getValue()));
		}
		return result;
	}
	
	@GET
	@Path("/waitevents/")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public List<Map<String,Object>> getWaitEvents() {
		Event event = statService.getWaitEvents();
		Map<String,Object> sorted = new TreeMap<>();
		for (String key : event.getPropertyNames()) {
			sorted.put(key,event.getProperty(key));
		}
		List<Map<String,Object>> result = new ArrayList<>();
		for(Map.Entry<String, Object> entry : sorted.entrySet()) {
			String[] values =  entry.getValue().toString().substring(1,entry.getValue().toString().length()-1).split("(,)");
			if (values.length == 3 && Long.parseLong(values[2]) < 100000L) {
				result.add(ImmutableMap.<String,Object>of(
						"name",entry.getKey(),
						"value",Long.parseLong(values[0]),
						"timeWaited",Long.parseLong(values[1]),
						"avgWaitTime",Long.parseLong(values[2])));
			}
		}
		return result;
	}
	
	@GET
	@Path("/sqls/")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public Collection<SqlExecution> getSqls() {
		return statService.getSqlExecutions();
	}
	
	private String getSqlSql() {
		return
			"select sql_id , dbms_lob.substr(sql_fulltext,4000,1), sum(executions) , sum(elapsed_time) / sum(executions) from gv$sql " +
			"where parsing_schema_name = user and last_active_time > (sysdate - 1) " +
			"group by sql_id , dbms_lob.substr(sql_fulltext,4000,1) " +
			"having sum(executions) > 1000 ";
	}
}
