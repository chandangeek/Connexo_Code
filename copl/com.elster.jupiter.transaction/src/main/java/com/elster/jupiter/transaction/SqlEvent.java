package com.elster.jupiter.transaction;

import java.util.List;

import org.osgi.service.event.Event;

import com.elster.jupiter.util.time.StopWatch;
import com.google.common.collect.ImmutableMap;

public final class SqlEvent {

	private final String text;
	private final List<Object> parameters;
	private final int fetchCount;
	private final StopWatch stopWatch;
	
	public SqlEvent(StopWatch stopWatch, String text, List<Object> parameters , int fetchCount) {
		this.stopWatch = stopWatch;
		this.text = text;
		this.parameters = parameters;
		this.fetchCount = fetchCount;		
	}
	
	@Override
	public String toString() {
		String base = 
			"SQL statement " + getStatementId() + " executed in " + (stopWatch.getElapsed() / 1000L) + " \u00b5s";
		if (fetchCount >= 0) {
			base += ", fetched " + fetchCount + (fetchCount == 1 ? " tuple." : "tuples.");
		}
		return base;
	}

	public String getText() {
		return text;
	}

	public List<Object> getParameters() {
		return parameters;
	}

	public int getFetchCount() {
		return fetchCount >= 0 ? fetchCount : 0;
	}

	public StopWatch getStopWatch() {
		return stopWatch;
	}
	
	public int getStatementId() {
		return text.hashCode();
	}
	
	public Event toOsgiEvent() {
		ImmutableMap.Builder<String,Object> builder = new ImmutableMap.Builder<>();
		builder.put("text",text).put("elapsed",stopWatch.getElapsed()/1000L).put("statement_id",getStatementId());
		if (parameters != null && parameters.size() > 0) {
			builder.put("parameters",parameters.toString());
		}
		if (fetchCount >= 0) {
			builder.put("fetchCount",fetchCount);
		}
		return new Event("com/elster/jupiter/sql/STATEMENT", builder.build());					
	}

}
