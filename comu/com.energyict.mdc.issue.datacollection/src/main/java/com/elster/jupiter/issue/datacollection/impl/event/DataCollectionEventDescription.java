package com.elster.jupiter.issue.datacollection.impl.event;

import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.tasks.history.CommunicationErrorType;

public enum DataCollectionEventDescription {
    UNKNOWN_INBOUND_DEVICE(
            "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE",
            null,
            MessageSeeds.EVENT_TITLE_UNKNOWN_INBOUND_DEVICE),
    UNKNOWN_OUTBOUND_DEVICE(
            "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE",
            null,
            MessageSeeds.EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE),
    DEVICE_COMMUNICATION_FAILURE(
            "com/energyict/mdc/comtask/FAILURE",
            CommunicationErrorType.COMMUNICATION_FAILURE,
            MessageSeeds.EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE),
    DEVICE_CONNECTION_SETUP_FAILURE(
            "com/energyict/mdc/connectiontasksetup/FAILURE",
            CommunicationErrorType.CONNECTION_SETUP_FAILURE,
            MessageSeeds.EVENT_TITLE_DEVICE_CONNECTION_SETUP_FAILURE),
    DEVICE_CONNECTION_FAILURE(
            "com/energyict/mdc/connectiontask/FAILURE",
            CommunicationErrorType.CONNECTION_FAILURE,
            MessageSeeds.EVENT_TITLE_DEVICE_CONNECTION_FAILURE),
    DEVICE_EVENT(
            "com/energyict/mdc/deviceevent/CREATED",
            null,
            MessageSeeds.EVENT_TITLE_DEVICE_EVENT),
    ;

    private String topic;
    private CommunicationErrorType errorType;
    private MessageSeeds title;

    private DataCollectionEventDescription(String topic, CommunicationErrorType errorType, MessageSeeds title){
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

    public static DataCollectionEventDescription getDescriptionByTopic(String topicName) {
        for (DataCollectionEventDescription column : DataCollectionEventDescription.values()) {
            if (column.topic.equalsIgnoreCase(topicName)) {
                return column;
            }
        }
        return null;
    }
}
