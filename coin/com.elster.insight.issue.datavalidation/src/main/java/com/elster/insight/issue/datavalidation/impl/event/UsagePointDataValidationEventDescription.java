/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;

import org.osgi.service.event.EventConstants;

import java.util.Map;

public enum UsagePointDataValidationEventDescription {

    CANNOT_ESTIMATE_USAGEPOINT_DATA("com/elster/jupiter/estimation/estimationblock/FAILURE", CannotEstimateUsagePointDataEvent.class),
    USAGEPOINT_READINGQUALITY_DELETED("com/elster/jupiter/metering/readingquality/DELETED", UsagePointSuspectDeletedEvent.class) {
        @Override
        public boolean matches(Map<?, ?> map) {
            // TODO: refactor to choose proper quality code system when issue management becomes available for MDM
            return super.matches(map)
                    && ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT).getCode()
                    .equals(map.get("readingQualityTypeCode"));
        }
    }
    ;
    
    private String topic;
    private Class<? extends UsagePointDataValidationEvent> eventClass;
    
    private UsagePointDataValidationEventDescription(String topic, Class<? extends UsagePointDataValidationEvent> eventClass) {
        this.topic = topic;
        this.eventClass = eventClass;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public Class<? extends UsagePointDataValidationEvent> getEventClass() {
        return eventClass;
    }
    
    public boolean matches(Map<?, ?> map) {
        String topic = (String) map.get(EventConstants.EVENT_TOPIC);
        return this.topic.equalsIgnoreCase(topic);
    }
}
