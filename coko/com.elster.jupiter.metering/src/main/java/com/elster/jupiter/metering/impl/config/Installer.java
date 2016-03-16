package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;

public class Installer {

	private final EventService eventService;

	Installer(EventService eventService) {
		this.eventService = eventService;
	}

	public void install() {
    }

}