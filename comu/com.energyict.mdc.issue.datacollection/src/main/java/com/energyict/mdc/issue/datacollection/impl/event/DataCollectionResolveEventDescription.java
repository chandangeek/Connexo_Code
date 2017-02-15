/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.issue.datacollection.event.ConnectionLostResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.event.DeviceCommunicationFailureResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.UnableToConnectResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownDeviceResolvedEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;

import org.osgi.service.event.EventConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

public enum DataCollectionResolveEventDescription implements EventDescription {
    CONNECTION_LOST_AUTO_RESOLVE(
            "com/energyict/mdc/connectiontask/COMPLETION",
            ConnectionLostResolvedEvent.class) {
        public boolean validateEvent(Map<?, ?> map) {
            if (super.validateEvent(map)) {
                return isEmptyString(map, ModuleConstants.SKIPPED_TASK_IDS)
                        && !isEmptyString(map, ModuleConstants.CONNECTION_TASK_ID);
            }
            return false;
        }

        @Override
        public String getUniqueKey() {
            return DataCollectionEventDescription.CONNECTION_LOST.getUniqueKey();
        }
    },

    DEVICE_COMMUNICATION_FAILURE_AUTO_RESOLVE(
            "com/energyict/mdc/connectiontask/COMPLETION",
            DeviceCommunicationFailureResolvedEvent.class) {
        public boolean validateEvent(Map<?, ?> map) {
            if (super.validateEvent(map)) {
                return !isEmptyString(map, ModuleConstants.SUCCESS_TASK_IDS);
            }
            return false;
        }

        @Override
        public List<Map<?, ?>> splitEvents(Map<?, ?> map) {
            return splitEventsByKey(map, ModuleConstants.SUCCESS_TASK_IDS);
        }

        @Override
        public String getUniqueKey() {
            return DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE.getUniqueKey();
        }
    },

    UNABLE_TO_CONNECT_AUTO_RESOLVE(
            "com/energyict/mdc/connectiontask/COMPLETION",
            UnableToConnectResolvedEvent.class) {
        @Override
        public String getUniqueKey() {
            return DataCollectionEventDescription.UNABLE_TO_CONNECT.getUniqueKey();
        }
    },

    UNKNOWN_INBOUND_DEVICE_EVENT_AUTO_RESOLVE(
            "com/energyict/mdc/device/data/device/CREATED",
            UnknownDeviceResolvedEvent.class) {
        @Override
        public String getUniqueKey() {
            return DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE.getUniqueKey();
        }
    },

    UNKNOWN_OUTBOUND_DEVICE_EVENT_AUTO_RESOLVE(
            "com/energyict/mdc/device/data/device/CREATED",
            UnknownDeviceResolvedEvent.class) {
        @Override
        public String getUniqueKey() {
            return DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE.getUniqueKey();
        }
    };

    private String topic;
    private Class<? extends DataCollectionEvent> eventClass;

    private DataCollectionResolveEventDescription(String topic, Class<? extends DataCollectionEvent> eventClass) {
        this.topic = topic;
        this.eventClass = eventClass;
    }

    @Override
    public boolean canBeAggregated() {
        return false;
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
