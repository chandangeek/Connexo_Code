package com.elster.jupiter.transaction;

import com.elster.jupiter.util.time.StopWatch;
import com.google.common.collect.ImmutableMap;

import org.osgi.service.event.Event;

import java.util.List;

public final class SqlEvent {

    private static final long NANOS_PER_MICRO = 1000L;
    private final String text;
    private final List<Object> parameters;
    private final int fetchCount;
    private final int batchCount;
    private final int rowCount;
    private final StopWatch stopWatch;

    public SqlEvent(StopWatch stopWatch, String text, List<Object> parameters, int fetchCount, int rowCount, int batchCount) {
        this.stopWatch = stopWatch;
        this.text = text;
        this.parameters = parameters;
        this.fetchCount = fetchCount;
        this.batchCount = batchCount;
        this.rowCount = rowCount;
    }

    public String print() {
    	StringBuilder builder = new StringBuilder();
    	if (fetchCount < 0) {
    		if (batchCount == 0) {
    			builder.append("Inserted/Updated " + rowCount + " row(s) for: " + text );
    		} else {
    			builder.append("Executed " + rowCount + " statements in " + batchCount + " batch(es) for: " + text );
    		}
    	} else {
    		builder.append("Fetched " + fetchCount + " tuple(s) from " + text);
    	} 
    	return builder.toString();
    }
    
    @Override
    public String toString() {
        String base =
                "SQL statement " + getStatementId() + " executed in " + (stopWatch.getElapsed() / NANOS_PER_MICRO) + " \u00b5s";
        if (fetchCount >= 0) {
            base += ", fetched " + fetchCount + (fetchCount == 1 ? " tuple" : " tuples");
        } else  {
        	if (batchCount == 0) {
        		base += " inserted/updated " + rowCount + (rowCount == 1  ? " row" : " rows");
        	} else {
        		base += " executed " + batchCount + " statements in " + batchCount + (batchCount == 1 ? " batch" : " batches"); 
        	}
        }
        return base + ".";
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

    public int getBatchCount() {
        return batchCount;
    }
    
    public int getRowCount() {
    	return rowCount;
    }
    
    public StopWatch getStopWatch() {
        return stopWatch;
    }

    public int getStatementId() {
        return text.hashCode();
    }

    public Event toOsgiEvent() {
        ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
        builder.put("text", text).put("elapsed", stopWatch.getElapsed() / NANOS_PER_MICRO).put("statement_id", getStatementId());
        if (parameters != null && !parameters.isEmpty()) {
            builder.put("parameters", parameters.toString());
        }
        if (fetchCount >= 0) {
            builder.put("fetchCount", fetchCount);
        } else {
        	builder.put("rowCount", rowCount);
    	}
        if (batchCount > 0) {
        	builder.put("batchCount", batchCount);
        }
        return new Event("com/elster/jupiter/sql/STATEMENT", builder.build());
    }

}
