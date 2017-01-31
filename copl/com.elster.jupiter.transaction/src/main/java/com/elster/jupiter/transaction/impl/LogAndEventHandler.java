/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.transaction.SqlEvent;
import com.elster.jupiter.transaction.TransactionEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

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
    public void handle(Object notification, Object ... notificationDetails) {
    	EventAdmin eventAdmin = eventAdminHolder.get();
    	if (notification instanceof SqlEvent) {
    		SqlEvent event = (SqlEvent) notification;
    		if(sqlLog) {
    			Logger.getLogger("com.elster.jupiter.transaction").info(event.toString());
    		}
    		if (sqlEvent && eventAdmin != null) {
    			eventAdmin.postEvent(event.toOsgiEvent());
    		}    		
    	}
    	if (notification instanceof TransactionEvent) {
    		TransactionEvent event = (TransactionEvent) notification;
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
