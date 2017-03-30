/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.issue.datacollection.event.ConnectionLostEvent;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.event.DeviceCommunicationFailureEvent;
import com.energyict.mdc.issue.datacollection.event.UnableToConnectEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownInboundDeviceEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownSlaveDeviceEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import org.osgi.service.event.EventConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

public enum DataCollectionEventDescription implements EventDescription {
    CONNECTION_LOST(
            "com/energyict/mdc/connectiontask/COMPLETION",
            CommunicationErrorType.CONNECTION_FAILURE,
            ConnectionLostEvent.class,
            TranslationKeys.EVENT_TITLE_CONNECTION_LOST) {
        public boolean validateEvent(Map<?, ?> map) {
            if (super.validateEvent(map)) {
                return !isEmptyString(map, ModuleConstants.SKIPPED_TASK_IDS);
            }
            return false;
        }
    },

    DEVICE_COMMUNICATION_FAILURE(
            "com/energyict/mdc/connectiontask/COMPLETION",
            CommunicationErrorType.COMMUNICATION_FAILURE,
            DeviceCommunicationFailureEvent.class,
            TranslationKeys.EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE) {
        public boolean validateEvent(Map<?, ?> map) {
            if (super.validateEvent(map)) {
                return !isEmptyString(map, ModuleConstants.FAILED_TASK_IDS);
            }
            return false;
        }

        @Override
        public List<Map<?, ?>> splitEvents(Map<?, ?> map) {
            return splitEventsByKey(map, ModuleConstants.FAILED_TASK_IDS);
        }
    },

    UNABLE_TO_CONNECT(
            "com/energyict/mdc/connectiontask/FAILURE",
            CommunicationErrorType.CONNECTION_SETUP_FAILURE,
            UnableToConnectEvent.class,
            TranslationKeys.EVENT_TITLE_UNABLE_TO_CONNECT),

    UNKNOWN_INBOUND_DEVICE(
            "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE",
            null,
            UnknownInboundDeviceEvent.class,
            TranslationKeys.EVENT_TITLE_UNKNOWN_INBOUND_DEVICE) {
    },

    UNKNOWN_OUTBOUND_DEVICE(
            "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE",
            null,
            UnknownSlaveDeviceEvent.class,
            TranslationKeys.EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE);

    private String topic;
    private CommunicationErrorType errorType;
    private TranslationKeys title;
    private Class<? extends DataCollectionEvent> eventClass;

    private DataCollectionEventDescription(String topic, CommunicationErrorType errorType, Class<? extends DataCollectionEvent> eventClass, TranslationKeys title) {
        this.topic = topic;
        this.errorType = errorType;
        this.eventClass = eventClass;
        this.title = title;
    }

    public CommunicationErrorType getErrorType() {
        return errorType;
    }

    public TranslationKeys getTitle() {
        return title;
    }

    @Override
    public Class<? extends DataCollectionEvent> getEventClass() {
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
    public boolean canBeAggregated() {
        return getErrorType() != null;
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

    protected List<Map<?, ?>> splitEventsByKey(Map<?, ?> map, String key) {
        String[] failedTasks = String.class.cast(map.get(key)).split(",");
        List<Map<?, ?>> eventDataList = new ArrayList<>(failedTasks.length);
        for (String task : failedTasks) {
            if (!is(task).emptyOrOnlyWhiteSpace()) {
                Map<Object, Object> data = new HashMap<>(map);
                data.put(key, task);
                eventDataList.add(data);
            }
        }
        return eventDataList;
    }

    public String getTopic() {
        return topic;
    }
}
