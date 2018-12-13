/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.util.streams.Functions;

import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadings;
import ch.iec.tc57._2011.getmeterreadings.Name;
import ch.iec.tc57._2011.getmeterreadings.NameType;
import ch.iec.tc57._2011.getmeterreadings.Reading;
import ch.iec.tc57._2011.getmeterreadings.ReadingType;
import ch.iec.tc57._2011.getmeterreadings.UsagePoint;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GetMeterReadingsRequestBuilder {
    private GetMeterReadings getMeterReadings;

    private GetMeterReadingsRequestBuilder() {
        getMeterReadings = new GetMeterReadings();
    }

    static GetMeterReadingsRequestBuilder createRequest() {
        return new GetMeterReadingsRequestBuilder();
    }

    GetMeterReadingsRequestBuilder withTimePeriods(String source, Range<Instant>... periods) {
        Arrays.stream(periods)
                .forEach(period -> withTimePeriod(source, period.lowerEndpoint(), period.upperEndpoint()));
        return this;
    }

    GetMeterReadingsRequestBuilder withTimePeriod(Instant start, Instant end) {
        return withTimePeriod(null, start, end);
    }

    GetMeterReadingsRequestBuilder withTimePeriod(String source, Instant start, Instant end) {
        Reading reading = new Reading();
        reading.setSource(source);
        DateTimeInterval interval = new DateTimeInterval();
        interval.setStart(start);
        interval.setEnd(end);
        reading.setTimePeriod(interval);
        getMeterReadings.getReading().add(reading);
        return this;
    }

    GetMeterReadingsRequestBuilder withReadingTypeMRIDs(String... mRIDs) {
        Arrays.stream(mRIDs)
                .forEach(mRID -> withReadingType(mRID, null));
        return this;
    }

    GetMeterReadingsRequestBuilder withReadingTypeFullAliasNames(String... names) {
        Arrays.stream(names)
                .forEach(name -> withReadingType(null, name));
        return this;
    }

    GetMeterReadingsRequestBuilder withReadingType(String mRID, String fullAliasName) {
        ReadingType readingType = new ReadingType();
        readingType.setMRID(mRID);
        name(fullAliasName).ifPresent(readingType.getNames()::add);
        getMeterReadings.getReadingType().add(readingType);
        return this;
    }

    GetMeterReadingsRequestBuilder withUsagePointMRIDs(String... mRIDs) {
        Arrays.stream(mRIDs)
                .forEach(mRID -> withUsagePoint(mRID, null));
        return this;
    }

    GetMeterReadingsRequestBuilder withUsagePointNames(String... names) {
        Arrays.stream(names)
                .forEach(name -> withUsagePoint(null, name));
        return this;
    }

    GetMeterReadingsRequestBuilder withUsagePoint(String mRID, String name, String... purposeNames) {
        UsagePoint usagePoint = new UsagePoint();
        usagePoint.setMRID(mRID);
        List<Name> names = usagePoint.getNames();
        name(name, UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType()).ifPresent(names::add);
        Arrays.stream(purposeNames)
                .map(purposeName -> name(purposeName, UsagePointNameTypeEnum.PURPOSE.getNameType()))
                .flatMap(Functions.asStream())
                .forEach(names::add);
        getMeterReadings.getUsagePoint().add(usagePoint);
        return this;
    }

    GetMeterReadingsRequestType get() {
        GetMeterReadingsRequestType request = new GetMeterReadingsRequestType();
        request.setGetMeterReadings(getMeterReadings);
        return request;
    }

    static Optional<Name> name(String value) {
        return name(value, null);
    }

    static Optional<Name> name(String value, String type) {
        if (value == null) {
            return Optional.empty();
        }
        Name name = new Name();
        name.setName(value);
        nameType(type).ifPresent(name::setNameType);
        return Optional.of(name);
    }

    private static Optional<NameType> nameType(String type) {
        if (type == null) {
            return Optional.empty();
        }
        NameType nameType = new NameType();
        nameType.setName(type);
        return Optional.of(nameType);
    }
}
