package com.energyict.mdc.issue.datavalidation.impl.event;

import java.util.Map;

import org.osgi.service.event.EventConstants;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;


public enum DataValidationEventDescription {

    CANNOT_ESTIMATE_DATA("com/elster/jupiter/estimation/estimationblock/FAILURE", CannotEstimateDataEvent.class),
    READINGQUALITY_DELETED("com/elster/jupiter/metering/readingquality/DELETED", SuspectDeletedEvent.class) {
        @Override
        public boolean matches(Map<?, ?> map) {
            if (super.matches(map)) {
                String readingQualityCode = (String) map.get("readingQualityTypeCode");
                return ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT).getCode().equals(readingQualityCode);
            }
            return false;
        }
    }
    
    ;
    
    private String topic;
    private Class<? extends DataValidationEvent> eventClass;
    
    private DataValidationEventDescription(String topic, Class<? extends DataValidationEvent> eventClass) {
        this.topic = topic;
        this.eventClass = eventClass;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public Class<? extends DataValidationEvent> getEventClass() {
        return eventClass;
    }
    
    public boolean matches(Map<?, ?> map) {
        String topic = (String) map.get(EventConstants.EVENT_TOPIC);
        return this.topic.equalsIgnoreCase(topic);
    }
}
