package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;

import org.osgi.service.event.EventConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

public enum DeviceAlarmEventDescription implements EventDescription {
    END_DEVICE_EVENT_CREATED(
            "com/elster/jupiter/enddeviceevent/CREATED",
            DeviceAlarmEvent.class,
            TranslationKeys.END_DEVICE_EVENT_CREATED) {
        public boolean validateEvent(Map<?, ?> map) {
            if (super.validateEvent(map)) {
                return !isEmptyString(map, ModuleConstants.SKIPPED_TASK_IDS);
            }
            return false;
        }
    };


    private String topic;
    private TranslationKeys title;
    private Class<? extends DeviceAlarmEvent> eventClass;

    private DeviceAlarmEventDescription(String topic, Class<? extends DeviceAlarmEvent> eventClass, TranslationKeys title) {
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
    //TODO FixME
    public boolean canBeAggregated() {
        return false;
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
	
	//not needed - will handle just one event for the time being

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
