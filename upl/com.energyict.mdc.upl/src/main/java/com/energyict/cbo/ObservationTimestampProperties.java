package com.energyict.cbo;

import com.energyict.mdc.upl.meterdata.CollectedTopology;

import java.util.Date;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-25 (13:57)
 */
public final class ObservationTimestampProperties {

    // Hide factory class constructor
    private ObservationTimestampProperties() {
    }

    public static CollectedTopology.ObservationTimestampProperty from(Date date, String propertyName) {
        return new DateObservationTimestampProperty(propertyName, date);
    }

    private static class DateObservationTimestampProperty implements CollectedTopology.ObservationTimestampProperty {
        private final String name;
        private final Date value;

        private DateObservationTimestampProperty(String name, Date value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Date getValue() {
            return value;
        }
    }
}