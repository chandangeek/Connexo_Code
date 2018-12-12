/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    IMPORT_SCHEDULE_CREATED("importschedule/CREATED"),
    IMPORT_SCHEDULE_UPDATED("importschedule/UPDATED"),
    IMPORT_SCHEDULE_DELETED("importschedule/DELETED");

    private static final String NAMESPACE = "com/elster/jupiter/fileimport/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(FileImportService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldNotPublish()
                .create();
    }
}