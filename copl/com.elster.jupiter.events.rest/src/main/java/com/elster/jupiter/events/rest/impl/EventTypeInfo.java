package com.elster.jupiter.events.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.events.EventType;

@XmlRootElement
public class EventTypeInfo {
	
	public String topic;
	public String component;
	public String scope;
	public String category;
	public String name;
	public boolean publish = true;
	
	public EventTypeInfo(EventType eventType) {
		topic = eventType.getTopic();
		component = eventType.getComponent();
		scope = eventType.getScope();
		category = eventType.getCategory();
		name = eventType.getName();
		publish = eventType.shouldPublish();
    }

    public EventTypeInfo() {
    }
    
    public void updateEventType(EventType eventType) {
    	eventType.setPublish(publish);
    }
    
    
    

}
