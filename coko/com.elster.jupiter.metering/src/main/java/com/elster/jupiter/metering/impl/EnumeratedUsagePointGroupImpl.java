package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnumeratedUsagePointGroupImpl implements EnumeratedUsagePointGroup {

    private String name;
    private String mrid;
    private String description;
    private String aliasName;
    private String type;

    private List<UsagePointMembership> memberships = new ArrayList<>();

    private static class UsagePointMembership {
        private final UsagePoint usagePoint;
        private IntermittentInterval intervals;

        private UsagePointMembership(UsagePoint usagePoint, IntermittentInterval intervals) {
            this.usagePoint = usagePoint;
            this.intervals = intervals;
        }

        private IntermittentInterval getIntervals() {
            return intervals;
        }

        private UsagePoint getUsagePoint() {
            return usagePoint;
        }

        public void addInterval(Interval interval) {
            intervals = intervals.addInterval(interval);
        }

        public void removeInterval(Interval interval) {
            intervals = intervals.remove(interval);
        }

        private Interval resultingInterval(Interval interval) {
            return getIntervals().intervalAt(interval.getStart());
        }
    }

    private static class EntryImpl implements Entry {

        private final UsagePoint usagePoint;
        private final Interval interval;

        public EntryImpl(UsagePoint usagePoint, Interval interval) {
            this.usagePoint = usagePoint;
            this.interval = interval;
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public UsagePoint getUsagePoint() {
            return usagePoint;
        }
    }

    @Override
    public Entry add(UsagePoint usagePoint, Interval interval) {
        UsagePointMembership membership = forUsagePoint(usagePoint);
        if (membership == null) {
            membership = new UsagePointMembership(usagePoint, IntermittentInterval.NEVER);
            memberships.add(membership);
        }
        membership.addInterval(interval);
        return new EntryImpl(usagePoint, membership.resultingInterval(interval));
    }

    @Override
    public void remove(Entry entry) {
        UsagePointMembership membership = forUsagePoint(entry.getUsagePoint());
        Interval interval = membership.getIntervals().intervalAt(entry.getInterval().getStart());
        if (interval != null && interval.equals(entry.getInterval())) {
            membership.removeInterval(interval);
            if (membership.getIntervals().isEmpty()) {
                memberships.remove(membership);
            }
        }

    }

    @Override
    public long getId() {
        //TODO automatically generated method body, provide implementation.
        return 0;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<UsagePoint> getMembers(final Date date) {
        return FluentIterable.from(memberships)
                .filter(Active.at(date))
                .transform(To.USAGE_POINT)
                .toList();
    }

    @Override
    public boolean isMember(final UsagePoint usagePoint, Date date) {
        return !FluentIterable.from(memberships)
                .filter(With.usagePoint(usagePoint))
                .filter(Active.at(date))
                .isEmpty();
    }

    private UsagePointMembership forUsagePoint(UsagePoint usagePoint) {
        return FluentIterable.from(memberships)
                .filter(With.usagePoint(usagePoint))
                .first().orNull();
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mrid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMRID(String mrid) {
        this.mrid = mrid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setType(String type) {
        this.type = type;
    }

    private static class Active implements Predicate<UsagePointMembership> {

        private final Date date;

        private Active(Date date) {
            this.date = date;
        }

        public static Active at(Date date) {
            return new Active(date);
        }

        @Override
        public boolean apply(UsagePointMembership membership) {
            return membership != null && membership.getIntervals().contains(date);
        }
    }

    private enum To implements Function<UsagePointMembership, UsagePoint> {

        USAGE_POINT;

        @Override
        public UsagePoint apply(UsagePointMembership membership) {
            return membership.getUsagePoint();
        }
    }

    private static class With implements Predicate<UsagePointMembership> {

        private final UsagePoint usagePoint;

        private With(UsagePoint usagePoint) {
            this.usagePoint = usagePoint;
        }

        public static With usagePoint(UsagePoint usagePoint) {
            return new With(usagePoint);
        }

        @Override
        public boolean apply(UsagePointMembership membership) {
            return membership.getUsagePoint().equals(usagePoint);
        }
    }
}
