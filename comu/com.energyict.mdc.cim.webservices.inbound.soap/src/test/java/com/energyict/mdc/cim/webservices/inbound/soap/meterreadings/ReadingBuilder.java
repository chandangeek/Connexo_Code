/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import ch.iec.tc57._2011.getmeterreadings.DataSource;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.getmeterreadings.NameType;
import ch.iec.tc57._2011.getmeterreadings.Reading;

import java.time.Instant;

public class ReadingBuilder {
    private Reading reading;

    private ReadingBuilder() {
        reading = new Reading();
    }

    static ReadingBuilder createRequest() {
        return new ReadingBuilder();
    }

    ReadingBuilder withTimePeriod(String source, Instant start, Instant end) {
        reading.setSource(source);
        DateTimeInterval interval = new DateTimeInterval();
        interval.setStart(start);
        interval.setEnd(end);
        reading.setTimePeriod(interval);
        return this;
    }

    ReadingBuilder withScheduleStrategy(String scheduleStrategy) {
        reading.setScheduleStrategy(scheduleStrategy);
        return this;
    }

    ReadingBuilder withConnectionMethod(String connectionMethod) {
        reading.setConnectionMethod(connectionMethod);
        return this;
    }

    ReadingBuilder withDataSource(DataSource dataSource) {
        reading.getDataSource().add(dataSource);
        return this;
    }

    ReadingBuilder withDataSource(String name, String nameTypeName) {
        DataSource dataSource = new DataSource();
        NameType nameType = new NameType();
        nameType.setName(nameTypeName);
        dataSource.setName(name);
        dataSource.setNameType(nameType);

        return withDataSource(dataSource);
    }

    Reading get() {
        return reading;
    }
}
