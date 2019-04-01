/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.event;

import com.elster.jupiter.issue.task.event.TaskEvent;
import com.elster.jupiter.issue.task.event.TaskFailureEvent;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.issue.task.impl.i18n.TranslationKeys;

import org.osgi.service.event.EventConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum TaskEventDescription implements EventDescription {
    TASK_FAILED(
            "com/elster/jupiter/tasks/taskoccurrence/FAILED",
            TaskFailureEvent.class,
            TranslationKeys.TASK_FAILED_EVENT) {
        public boolean validateEvent(Map<?, ?> map) {
            return super.validateEvent(map);
        }
    };

    private String topic;
    private TranslationKeys title;
    private Class<? extends TaskEvent> eventClass;

    TaskEventDescription(String topic, Class<? extends TaskEvent> eventClass, TranslationKeys title) {
        this.topic = topic;
        this.eventClass = eventClass;
        this.title = title;
    }

    public TranslationKeys getTitle() {
        return title;
    }

    @Override
    public Class<? extends TaskEvent> getEventClass() {
        return this.eventClass;
    }

    @Override
    public boolean validateEvent(Map<?, ?> map) {
        String topic = String.class.cast(map.get(EventConstants.EVENT_TOPIC));
        return this.topic.equalsIgnoreCase(topic);
    }

    @Override
    public List<Map<?, ?>> splitEvents(Map<?, ?> map) {
        return Collections.singletonList(map);
    }

    @Override
    public String getUniqueKey() {
        return this.name();
    }

    protected boolean isEmptyString(Map<?, ?> map, String key) {
        Object requestedObj = map.get(key);
        if (requestedObj instanceof String) {
            String stringForCheck = String.class.cast(map.get(key));
            return Checks.is(stringForCheck).emptyOrOnlyWhiteSpace();
        }
        return requestedObj == null;
    }

    //will handle just one event for the time being

    public boolean matches(Map<?, ?> map) {
        String topic = (String) map.get(EventConstants.EVENT_TOPIC);
        return this.topic.equalsIgnoreCase(topic);
    }

    public String getTopic() {
        return topic;
    }
}
