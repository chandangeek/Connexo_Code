package com.elster.jupiter.events.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.events.EventType;


@XmlRootElement
public class EventTypeInfos {
	
	public int total;
	public List<EventTypeInfo> eventTypes = new ArrayList<>();

	EventTypeInfos() {
	}

	EventTypeInfos(EventType eventType) {
	    add(eventType);
	}

	EventTypeInfos(Iterable<? extends EventType> types) {
	    addAll(types);
	}

	EventTypeInfo add(EventType eventType) {
		EventTypeInfo result = new EventTypeInfo(eventType);
		eventTypes.add(result);
	    total++;
	    return result;
	}

	void addAll(Iterable<? extends EventType> types) {
	    for (EventType each : types) {
	        add(each);
	    }
	}


}


