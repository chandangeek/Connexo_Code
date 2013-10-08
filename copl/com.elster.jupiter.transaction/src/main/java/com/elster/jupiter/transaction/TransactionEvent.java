package com.elster.jupiter.transaction;

import org.osgi.service.event.*;

import com.elster.jupiter.util.time.StopWatch;
import com.google.common.collect.ImmutableMap;

public final class TransactionEvent {

	private final Transaction<?> transaction;
	private final StopWatch stopWatch;
	private final int sqlCount;
	private final int fetchCount;
	private final boolean failed;
	
	public TransactionEvent(Transaction<?> transaction, boolean failed , StopWatch stopWatch , int sqlCount , int fetchCount) {
		this.transaction = transaction;
		this.failed = failed;
		this.stopWatch = stopWatch;
		this.sqlCount = sqlCount;
		this.fetchCount = fetchCount;		
	}
	
	@Override
	public String toString() {
		String base = 
			(failed ? "Rollbacked" : "Committed") + " Transaction " + transaction.getClass().getName() + " executed in " + (stopWatch.getElapsed() / 1000L) + " \u00b5s" +
			", executed " + sqlCount + " statements, fetched " + fetchCount + " tuples";
		
		return base;
	}

	public int getFetchCount() {
		return fetchCount;
	}

	public StopWatch getStopWatch() {
		return stopWatch;
	}
	
	public Event toOsgiEvent() {
		ImmutableMap.Builder<String,Object> builder = new ImmutableMap.Builder<>();
		builder
			.put("transaction",transaction.getClass().getName())			
			.put("elapsed",stopWatch.getElapsed()/1000L)
			.put("cpu",stopWatch.getCpu()/1000L)
			.put("statements",sqlCount)
			.put("fetchCount",fetchCount)		
			.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
		if (failed) {
			builder.put("failed",true);
		}
		return new Event("com/elster/jupiter/transaction/TRANSACTION", builder.build());					
	}
	
	public boolean hasFailed() {
		return failed;
	}

}
