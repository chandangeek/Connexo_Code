/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.time.Instant;

public class EventSetOnMetrologyConfigurationImpl implements EventSetOnMetrologyConfiguration {

    public enum Fields {
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        EVENTSET("eventSet");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    static EventSetOnMetrologyConfigurationImpl from(DataModel dataModel, MetrologyConfiguration metrologyConfiguration, EventSet eventSet) {
        return dataModel.getInstance(EventSetOnMetrologyConfigurationImpl.class).init(metrologyConfiguration, eventSet);
    }

    private EventSetOnMetrologyConfigurationImpl init(MetrologyConfiguration metrologyConfiguration, EventSet eventSet) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.eventSet.set(eventSet);
        return this;
    }

    private Reference<EventSet> eventSet = ValueReference.absent();
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return metrologyConfiguration.get();
    }

    @Override
    public EventSet getEventSet() {
        return eventSet.get();
    }
}
