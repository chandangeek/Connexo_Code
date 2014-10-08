package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.issue.datacollection.event.*;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import org.osgi.service.event.EventConstants;

import java.util.*;

import static com.elster.jupiter.util.Checks.is;

public enum DataCollectionEventDescription {
    CONNECTION_LOST(
            "com/energyict/mdc/connectiontask/COMPLETION",
            CommunicationErrorType.CONNECTION_FAILURE,
            ConnectionLostEvent.class,
            MessageSeeds.EVENT_TITLE_CONNECTION_LOST){
        public boolean validateEvent(Map<?, ?> map){
            if (super.validateEvent(map)){
                String skippedTasks = String.class.cast(map.get(ModuleConstants.SKIPPED_TASK_IDS));
                return !Checks.is(skippedTasks).emptyOrOnlyWhiteSpace();
            }
            return false;
        }

        @Override
        public List<Map<?, ?>> splitEvents(Map<?, ?> map) {
            String[] skippedTasks = String.class.cast(map.get(ModuleConstants.SKIPPED_TASK_IDS)).split(",");
            List<Map<?, ?>> eventDataList = new ArrayList<>(skippedTasks.length);
            for (String task : skippedTasks) {
                if (!is(task).emptyOrOnlyWhiteSpace()) {
                    Map<Object, Object> data = new HashMap<>(map);
                    data.put(ModuleConstants.SKIPPED_TASK_IDS, task);
                    eventDataList.add(data);
                }
            }
            return eventDataList;
        }
    },

    DEVICE_COMMUNICATION_FAILURE(
            "com/energyict/mdc/connectiontask/COMPLETION",
            CommunicationErrorType.COMMUNICATION_FAILURE,
            DeviceCommunicationFailureEvent.class,
            MessageSeeds.EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE){
        public boolean validateEvent(Map<?, ?> map){
            if (super.validateEvent(map)){
                String failedTasks = String.class.cast(map.get(ModuleConstants.FAILED_TASK_IDS));
                return !Checks.is(failedTasks).emptyOrOnlyWhiteSpace();
            }
            return false;
        }

        @Override
        public List<Map<?, ?>> splitEvents(Map<?, ?> map) {
            String[] failedTasks = String.class.cast(map.get(ModuleConstants.FAILED_TASK_IDS)).split(",");
            List<Map<?, ?>> eventDataList = new ArrayList<>(failedTasks.length);
            for (String task : failedTasks) {
                if (!is(task).emptyOrOnlyWhiteSpace()) {
                    Map<Object, Object> data = new HashMap<>(map);
                    data.put(ModuleConstants.FAILED_TASK_IDS, task);
                    eventDataList.add(data);
                }
            }
            return eventDataList;
        }
    },

    UNABLE_TO_CONNECT(
            "com/energyict/mdc/connectiontask/FAILURE",
            CommunicationErrorType.CONNECTION_SETUP_FAILURE,
            UnableToConnectEvent.class,
            MessageSeeds.EVENT_TITLE_UNABLE_TO_CONNECT),

    UNKNOWN_INBOUND_DEVICE(
            "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE",
            null,
            UnknownInboundDeviceEvent.class,
            MessageSeeds.EVENT_TITLE_UNKNOWN_INBOUND_DEVICE),

    UNKNOWN_OUTBOUND_DEVICE(
            "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE",
            null,
            UnknownOutboundDeviceEvent.class,
            MessageSeeds.EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE);
/*
    DEVICE_EVENT(
            "com/elster/jupiter/metering/enddeviceevent/CREATED",
            null,
            MeterIssueEvent.class,
            MessageSeeds.EVENT_TITLE_DEVICE_EVENT);
*/
    private String topic;
    private CommunicationErrorType errorType;
    private MessageSeeds title;
    private Class<? extends DataCollectionEvent> eventClass;

    private DataCollectionEventDescription(String topic, CommunicationErrorType errorType, Class<? extends DataCollectionEvent> eventClass, MessageSeeds title) {
        this.topic = topic;
        this.errorType = errorType;
        this.eventClass = eventClass;
        this.title = title;
    }

    @Deprecated
    private DataCollectionEventDescription(String topic, CommunicationErrorType errorType, MessageSeeds title) {
        this.topic = topic;
        this.errorType = errorType;
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public CommunicationErrorType getErrorType() {
        return errorType;
    }

    public MessageSeeds getTitle() {
        return title;
    }

    public Class<? extends DataCollectionEvent> getEventClass(){
        return this.eventClass;
    }

    public boolean validateEvent(Map<?, ?> map){
        String topic = String.class.cast(map.get(EventConstants.EVENT_TOPIC));
        return this.topic.equalsIgnoreCase(topic);
    }

    public List<Map<?, ?>> splitEvents(Map<?, ?> map){
        return Collections.singletonList(map);
    }

    public static DataCollectionEventDescription getDescription(Map<?, ?> map) {
        for (DataCollectionEventDescription column : DataCollectionEventDescription.values()) {
            if (column.validateEvent(map)) {
                return column;
            }
        }
        return null;
    }
}
