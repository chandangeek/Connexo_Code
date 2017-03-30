/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;

import org.osgi.service.event.EventConstants;

import java.util.Map;

public enum DataValidationEventDescription {

    CANNOT_ESTIMATE_DATA("com/elster/jupiter/estimation/estimationblock/FAILURE", CannotEstimateDataEvent.class),
    READINGQUALITY_DELETED("com/elster/jupiter/metering/readingquality/DELETED", SuspectDeletedEvent.class) {
        @Override
        public boolean matches(Map<?, ?> map) {
            // TODO: refactor to choose proper quality code system when issue management becomes available for MDM
            return super.matches(map)
                    && ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT).getCode()
                    .equals(map.get("readingQualityTypeCode"));
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
