package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMembership;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnumeratedUsagePointGroupImpl extends AbstractUsagePointGroup implements EnumeratedUsagePointGroup {

    private List<EntryImpl> entries;

    private final List<UsagePointMembershipImpl> memberships = new ArrayList<>();

    private final DataModel dataModel;

    @Inject
    EnumeratedUsagePointGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private List<EntryImpl> getEntries() {
        if (entries == null) {
            List<Entry> entryList = dataModel.mapper(Entry.class).find("usagePointGroup", this);
            entries = new ArrayList<>(entryList.size());
            for (Entry entry : entryList) {
                entries.add((EntryImpl) entry);
            }
            buildMemberships();
        }
        return entries;
    }

    private void buildMemberships() {
        Map<UsagePoint, UsagePointMembershipImpl> map = new HashMap<>();
        for (EntryImpl entry : entries) {
            if (!map.containsKey(entry.getUsagePoint())) {
                UsagePointMembershipImpl newMembership = new UsagePointMembershipImpl(entry.getUsagePoint(), IntermittentInterval.NEVER);
                map.put(entry.getUsagePoint(), newMembership);
                memberships.add(newMembership);
            }
            UsagePointMembershipImpl membership = map.get(entry.getUsagePoint());
            membership.addInterval(entry.getInterval());
        }

    }

    private List<UsagePointMembershipImpl> getMemberships() {
        if (entries == null) {
            getEntries();
        }
        return memberships;
    }

    static class EntryImpl implements Entry {

        private transient EnumeratedUsagePointGroup usagePointGroup;
        private long groupId;
        private transient UsagePoint usagePoint;
        private Interval interval;
        private long usagePointId;

        private final DataModel dataModel;

        @Inject
        EntryImpl(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        EntryImpl init(EnumeratedUsagePointGroup usagePointGroup, UsagePoint usagePoint, Interval interval) {
            this.usagePointGroup = usagePointGroup;
            this.groupId = usagePointGroup.getId();
            this.usagePoint = usagePoint;
            this.usagePointId = usagePoint.getId();
            this.interval = interval;
            return this;
        }

        static EntryImpl from(DataModel dataModel, EnumeratedUsagePointGroup usagePointGroup, UsagePoint usagePoint, Interval interval) {
            return dataModel.getInstance(EntryImpl.class).init(usagePointGroup, usagePoint, interval);
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public UsagePoint getUsagePoint() {
            if (usagePoint == null) {
                usagePoint = dataModel.mapper(UsagePoint.class).getOptional(usagePointId).get();
            }
            return usagePoint;
        }

        public EnumeratedUsagePointGroup getUsagePointGroup() {
            if (usagePointGroup == null) {
                usagePointGroup = dataModel.mapper(EnumeratedUsagePointGroup.class).getOptional(groupId).get();
        }
            return usagePointGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            EntryImpl entry = (EntryImpl) o;

            return groupId == entry.groupId && usagePointId == entry.usagePointId && Objects.equals(interval.getStart(), entry.interval.getStart());

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, usagePointId, interval.getStart());
        }
    }

    @Override
    public Entry add(UsagePoint usagePoint, Interval interval) {
        UsagePointMembershipImpl membership = forUsagePoint(usagePoint);
        if (membership == null) {
            membership = new UsagePointMembershipImpl(usagePoint, IntermittentInterval.NEVER);
            getMemberships().add(membership);
        }
        membership.addInterval(interval);
        EntryImpl entry = EntryImpl.from(dataModel, this, usagePoint, membership.resultingInterval(interval));
        getEntries().add(entry);
        return entry;
    }

    @Override
    public void remove(Entry entry) {
        UsagePointMembershipImpl membership = forUsagePoint(entry.getUsagePoint());
        Interval interval = membership.getIntervals().intervalAt(entry.getInterval().getStart());
        if (interval != null && interval.equals(entry.getInterval())) {
            membership.removeInterval(interval);
            if (membership.getIntervals().isEmpty()) {
                getMemberships().remove(membership);
            }
        }
    }

    @Override
    public void save() {
        if (id == 0) {
            factory().persist(this);
            for (EntryImpl entry : getEntries()) {
                entry.groupId = id;
            }
            ArrayList<Entry> result = new ArrayList<>();
            for (EntryImpl entry : getEntries()) {
                result.add(entry);
            }
            entryFactory().persist(result);
        } else {
            factory().update(this);
            List<Entry> existingEntries = entryFactory().find("usagePointGroup", this);
            DiffList<Entry> entryDiff = ArrayDiffList.fromOriginal(existingEntries);
            entryDiff.clear();
            for (UsagePointMembership membership : memberships) {
                for (Interval interval : membership.getIntervals().getIntervals()) {
                    entryDiff.add(EntryImpl.from(dataModel, this, membership.getUsagePoint(), interval));
                }
            }
            entryFactory().remove(FluentIterable.from(entryDiff.getRemovals()).toList());
            entryFactory().update(FluentIterable.from(entryDiff.getRemaining()).toList());
            entryFactory().persist(FluentIterable.from(entryDiff.getAdditions()).toList());
        }

    }

    private DataMapper<EnumeratedUsagePointGroup> factory() {
        return dataModel.mapper(EnumeratedUsagePointGroup.class);
    }

    private DataMapper<Entry> entryFactory() {
        return dataModel.mapper(Entry.class);
    }

    @Override
    public List<UsagePoint> getMembers(final Date date) {
        return FluentIterable.from(getMemberships())
                .filter(Active.at(date))
                .transform(To.USAGE_POINT)
                .toList();
    }

    @Override
    public List<UsagePointMembership> getMembers(Interval interval) {
        final IntermittentInterval intervalScope = IntermittentInterval.from(interval);
        return FluentIterable.from(getMemberships())
                .filter(Active.during(interval))
                .transform(new Function<UsagePointMembershipImpl, UsagePointMembership>() {
                    @Override
                    public UsagePointMembership apply(UsagePointMembershipImpl input) {
                        return input.withIntervals(input.getIntervals().intersection(intervalScope));
                    }
                })
                .toList();
    }

    @Override
    public boolean isMember(final UsagePoint usagePoint, Date date) {
        return !FluentIterable.from(getMemberships())
                .filter(With.usagePoint(usagePoint))
                .filter(Active.at(date))
                .isEmpty();
    }

    @Override
    public void endMembership(UsagePoint usagePoint, Date date) {
        Optional<UsagePointMembershipImpl> first = FluentIterable.from(getMemberships())
                .filter(With.usagePoint(usagePoint))
                .filter(Active.at(date)).first();
        if (first.isPresent()) {
            first.get().removeInterval(Interval.startAt(date));
        }
    }

    private UsagePointMembershipImpl forUsagePoint(UsagePoint usagePoint) {
        return FluentIterable.from(getMemberships())
                .filter(With.usagePoint(usagePoint))
                .first().orNull();
    }

    private static abstract class Active implements Predicate<UsagePointMembershipImpl> {

        public static Active at(Date date) {
            return new ActiveAt(date);
        }

        public static Active during(Interval interval) {
            return new ActiveDuring(interval);
        }
    }

    private static class ActiveDuring extends Active {
        private final Interval interval;

        private ActiveDuring(Interval interval) {
            this.interval = interval;
        }

        @Override
        public boolean apply(UsagePointMembershipImpl membership) {
            return membership != null && membership.getIntervals().overlaps(IntermittentInterval.from(interval));
        }
    }


    private static class ActiveAt extends Active {
        private final Date date;

        private ActiveAt(Date date) {
            this.date = date;
        }

        @Override
        public boolean apply(UsagePointMembershipImpl membership) {
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
