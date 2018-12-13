/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventTypeInfo {
	
	public String topic;
	public String component;
	public String scope;
	public String category;
	public String name;
	public long version;
	public boolean publish = true;
	
	public EventTypeInfo(EventType eventType) {
		topic = eventType.getTopic();
		component = eventType.getComponent();
		scope = eventType.getScope();
		category = eventType.getCategory();
		name = eventType.getName();
		publish = eventType.shouldPublish();
		version = eventType.getVersion();
    }

    public EventTypeInfo() {
    }
    
    public void updateEventType(EventType eventType) {
    	eventType.setPublish(publish);
    }
}
