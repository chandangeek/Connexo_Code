/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class Events implements EventHandler{
		private final Map<String,Long> events = new ConcurrentHashMap<>();

		@Override
		synchronized public void handleEvent(Event event) {
			Long count = events.get(event.getTopic());
			if (count == null) {
				events.put(event.getTopic(),1L);
			} else {
				events.put(event.getTopic(),++count);
			}
		}
		
		public List<String> getTopics() {
			return new ArrayList<>(events.keySet());
		}
		
		public long getCount(String topic) {
			Long count = events.get(topic);
			return count == null ? 0 : count;
		}
	
}
