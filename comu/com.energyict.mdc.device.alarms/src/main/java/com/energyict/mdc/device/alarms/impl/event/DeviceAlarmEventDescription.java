/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;
import com.energyict.mdc.device.alarms.event.EndDeviceEventCreatedEvent;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;

import org.osgi.service.event.EventConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum DeviceAlarmEventDescription implements EventDescription {
    END_DEVICE_EVENT_CREATED(
            "com/elster/jupiter/metering/enddeviceevent/CREATED",
            EndDeviceEventCreatedEvent.class,
            TranslationKeys.END_DEVICE_EVENT_CREATED) {
        public boolean validateEvent(Map<?, ?> map) {
            return super.validateEvent(map) && !isEmptyString(map, ModuleConstants.SKIPPED_TASK_IDS);
        }
    };

    private String topic;
    private TranslationKeys title;
    private Class<? extends DeviceAlarmEvent> eventClass;

    DeviceAlarmEventDescription(String topic, Class<? extends DeviceAlarmEvent> eventClass, TranslationKeys title) {
        this.topic = topic;
        this.eventClass = eventClass;
        this.title = title;
    }

    public TranslationKeys getTitle() {
        return title;
    }

    @Override
    public Class<? extends DeviceAlarmEvent> getEventClass() {
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
