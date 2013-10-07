package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.pubsub.*;
import com.elster.jupiter.transaction.*;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventAdmin;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component(name="com.elster.jupiter.transaction.logging", service=Subscriber.class)
public class LogAndEventHandler implements Subscriber  {
	private volatile AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();
	private volatile boolean sqlLog;
	private volatile boolean sqlEvent;
	private volatile boolean txLog;
	private volatile boolean txEvent;
	
	public LogAndEventHandler() {		
	}
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC) 
	public void setEventAdminService(EventAdmin eventAdminService) {
		this.eventAdminHolder.set(eventAdminService);
	}
	
	public void unsetEventAdminService(EventAdmin eventAdminService) {
		this.eventAdminHolder.compareAndSet(eventAdminService, null);
	}
	
	@Activate
    public void activate(Map<String, Object> props) {
    	if (props != null) {
    		sqlLog = Boolean.TRUE == props.get("sqllog");
    		sqlEvent = Boolean.TRUE == props.get("sqlevent");
    		txLog = Boolean.TRUE == props.get("txlog");
    		txEvent = Boolean.TRUE == props.get("txevent");
    	}
    }

    @Override
    public void handle(Object rawEvent, Object ... eventDetails) {
    	EventAdmin eventAdmin = eventAdminHolder.get();
    	if (rawEvent instanceof SqlEvent) {
    		SqlEvent event = (SqlEvent) rawEvent;
    		if(sqlLog) {
    			Logger.getLogger("com.elster.jupiter.transaction").info(event.toString());
    		}
    		if (sqlEvent && eventAdmin != null) {
    			eventAdmin.postEvent(event.toOsgiEvent());
    		}    		
    	}
    	if (rawEvent instanceof TransactionEvent) {
    		TransactionEvent event = (TransactionEvent) rawEvent;
    		if (txLog) {
    			Logger.getLogger("com.elster.jupiter.transaction").info(event.toString());
    		}
    		if (txEvent && eventAdmin != null) {
    			eventAdmin.postEvent(event.toOsgiEvent());
    		}    		
    	}
    }
  
    @Override
    public Class<?>[] getClasses() {
    	return new Class<?>[] { SqlEvent.class , TransactionEvent.class };
    }
}
