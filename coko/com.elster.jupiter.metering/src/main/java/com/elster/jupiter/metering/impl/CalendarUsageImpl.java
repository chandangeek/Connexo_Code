/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;

@SupportsTimeOfUseEventsFromEffectiveMetrologyConfigurations(message = PrivateMessageSeeds.Constants.UNSATISFIED_TOU, groups = {Save.Create.class, Save.Update.class})
public class CalendarUsageImpl implements ServerCalendarUsage {

    enum Fields {
        ID("id"),
        USAGEPOINT("usagePoint"),
        CALENDAR("calendar"),
        INTERVAL("interval");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private Reference<ServerUsagePoint> usagePoint = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();
    private Interval interval;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    public CalendarUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private CalendarUsageImpl init(Instant from, ServerUsagePoint usagePoint, Calendar calendar) {
        interval = Interval.of(Range.atLeast(from));
        this.calendar.set(calendar);
        this.usagePoint.set(usagePoint);
        return this;
    }

    public static CalendarUsageImpl create(DataModel dataModel, Instant from, ServerUsagePoint usagePoint, Calendar calendar) {
        return dataModel.getInstance(CalendarUsageImpl.class).init(from, usagePoint, calendar);
    }

    @Override
    public Range<Instant> getRange() {
        return interval.toClosedOpenRange();
    }

    @Override
    public boolean overlaps(Range<Instant> otherRange) {
        return !ImmutableRangeSet.of(getRange()).subRangeSet(otherRange).isEmpty();
    }

    @Override
    public boolean startsOnOrAfter(Instant when) {
        Instant start = this.interval.getStart();
        return start.equals(when) || start.isAfter(when);
    }

    @Override
    public Calendar getCalendar() {
        return calendar.get();
    }

    UsagePoint getUsagePoint() {
        return usagePoint.get();
    }

    void save() {
        this.usagePoint.get().add(this);
    }

    @Override
    public boolean notEnded() {
        return this.interval.getEnd() == null;
    }

    @Override
    public void end(Instant endAt) {
        Range<Instant> currentRange = getRange();
        interval = Interval.of(Ranges.copy(currentRange).withOpenUpperBound(endAt));
        update();
    }

    void update() {
        Save.UPDATE.save(dataModel, this);
    }

}