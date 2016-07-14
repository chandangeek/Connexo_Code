package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.Interval;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class EnumeratedUsagePointGroupImpl extends AbstractUsagePointGroup implements EnumeratedUsagePointGroup {

    private List<EntryImpl> entries;

    private final List<UsagePointMembershipImpl> memberships = new ArrayList<>();

    private final DataModel dataModel;

    @Inject
    EnumeratedUsagePointGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(this.doGetEntries());
    }

    private List<EntryImpl> doGetEntries() {
        if (entries == null) {
            this.entries = new ArrayList<>(dataModel.mapper(EntryImpl.class).find("usagePointGroup", this));
            buildMemberships();
        }
        return entries;
    }

    private void buildMemberships() {
        Map<UsagePoint, UsagePointMembershipImpl> map = new HashMap<>();
        for (EntryImpl entry : entries) {
            if (!map.containsKey(entry.getUsagePoint())) {
                UsagePointMembershipImpl newMembership = new UsagePointMembershipImpl(entry.getUsagePoint(), ImmutableRangeSet.of());
                map.put(entry.getUsagePoint(), newMembership);
                memberships.add(newMembership);
            }
            UsagePointMembershipImpl membership = map.get(entry.getUsagePoint());
            membership.addRange(entry.getInterval().toClosedOpenRange());
        }
    }

    private List<UsagePointMembershipImpl> getMemberships() {
        if (entries == null) {
            doGetEntries();
        }
        return memberships;
    }

    static class EntryImpl implements Entry {

        private transient EnumeratedUsagePointGroup usagePointGroup;
        private long groupId;
        private transient UsagePoint usagePoint;
        private Interval interval;
        private long usagePointId;
        @SuppressWarnings("unused") // Managed by ORM
        private Instant createTime;
        @SuppressWarnings("unused") // Managed by ORM
        private String userName;

        private final DataModel dataModel;
        private final MeteringService meteringService;

        @Inject
        EntryImpl(DataModel dataModel, MeteringService meteringService) {
            this.dataModel = dataModel;
            this.meteringService = meteringService;
        }

        EntryImpl init(EnumeratedUsagePointGroup usagePointGroup, UsagePoint usagePoint, Range<Instant> range) {
            this.usagePointGroup = usagePointGroup;
            this.groupId = usagePointGroup.getId();
            this.usagePoint = usagePoint;
            this.usagePointId = usagePoint.getId();
            this.interval = Interval.of(Effectivity.requireValid(range));
            return this;
        }

        static EntryImpl from(DataModel dataModel, EnumeratedUsagePointGroup usagePointGroup, UsagePoint usagePoint, Range<Instant> range) {
            return dataModel.getInstance(EntryImpl.class).init(usagePointGroup, usagePoint, range);
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public UsagePoint getUsagePoint() {
            if (usagePoint == null) {
                usagePoint = meteringService.findUsagePoint(usagePointId).get();
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
    public Entry add(UsagePoint usagePoint, Range<Instant> range) {
        UsagePointMembershipImpl membership = forUsagePoint(usagePoint);
        if (membership == null) {
            membership = new UsagePointMembershipImpl(usagePoint, ImmutableRangeSet.of());
            getMemberships().add(membership);
        }
        membership.addRange(range);
        EntryImpl entry = EntryImpl.from(dataModel, this, usagePoint, membership.resultingRange(range));
        doGetEntries().add(entry);
        return entry;
    }

    @Override
    public void remove(Entry entry) {
        UsagePointMembershipImpl membership = forUsagePoint(entry.getUsagePoint());
        Range<Instant> range = membership.getRanges().rangeContaining(entry.getRange().lowerEndpoint());
        if (range != null && range.equals(entry.getRange())) {
            membership.removeRange(range);
            if (membership.getRanges().isEmpty()) {
                getMemberships().remove(membership);
            }
        }
    }

    @Override
    public void save() {
        if (getId() == 0) {
            factory().persist(this);
            this.doGetEntries().stream().forEach(entry -> entry.groupId = this.getId());
            entryFactory().persist(this.doGetEntries());
        } else {
            factory().update(this);
            List<EntryImpl> existingEntries = entryFactory().find("usagePointGroup", this);
            DiffList<EntryImpl> entryDiff = ArrayDiffList.fromOriginal(existingEntries);
            entryDiff.clear();
            for (UsagePointMembership membership : memberships) {
                for (Range<Instant> range : membership.getRanges().asRanges()) {
                    entryDiff.add(EntryImpl.from(dataModel, this, membership.getUsagePoint(), range));
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

    private DataMapper<EntryImpl> entryFactory() {
        return dataModel.mapper(EntryImpl.class);
    }

    @Override
    public List<UsagePoint> getMembers(final Instant instant) {
        return FluentIterable.from(getMemberships())
                .filter(Active.at(instant))
                .transform(To.USAGE_POINT)
                .toList();
    }

    @Override
    public List<UsagePointMembership> getMembers(Range<Instant> range) {
        return FluentIterable.from(getMemberships())
                .filter(Active.during(range))
                .transform(new Function<UsagePointMembershipImpl, UsagePointMembership>() {
                    @Override
                    public UsagePointMembership apply(UsagePointMembershipImpl input) {
                        return input.withRanges(input.getRanges().subRangeSet(range));
                    }
                })
                .toList();
    }

    @Override
    public boolean isMember(final UsagePoint usagePoint, Instant instant) {
        return !FluentIterable.from(getMemberships())
                .filter(With.usagePoint(usagePoint))
                .filter(Active.at(instant))
                .isEmpty();
    }

    @Override
    public void endMembership(UsagePoint usagePoint, Instant instant) {
    	getMemberships().stream()
			.filter( member -> member.getUsagePoint().equals(usagePoint))
			.filter( member -> member.getRanges().contains(instant))
			.findFirst()
			.ifPresent( member -> member.removeRange(Range.atLeast(instant)));
    }

    private UsagePointMembershipImpl forUsagePoint(UsagePoint usagePoint) {
        return FluentIterable.from(getMemberships())
                .filter(With.usagePoint(usagePoint))
                .first().orNull();
    }

    private abstract static class Active implements Predicate<UsagePointMembershipImpl> {

        public static Active at(Instant instant) {
            return new ActiveAt(instant);
        }

        public static Active during(Range<Instant> range) {
            return new ActiveDuring(range);
        }
    }

    private static final class ActiveDuring extends Active {
        private final Range<Instant> range;

        private ActiveDuring(Range<Instant> range) {
            this.range = range;
        }

        @Override
        public boolean apply(UsagePointMembershipImpl membership) {
            return membership != null && !membership.getRanges().subRangeSet(range).isEmpty();
        }
    }


    private static final class ActiveAt extends Active {
        private final Instant instant;

        private ActiveAt(Instant instant) {
            this.instant = instant;
        }

        @Override
        public boolean apply(UsagePointMembershipImpl membership) {
            return membership != null && membership.getRanges().contains(instant);
        }
    }

    private enum To implements Function<UsagePointMembership, UsagePoint> {

        USAGE_POINT;

        @Override
        public UsagePoint apply(UsagePointMembership membership) {
            return membership.getUsagePoint();
        }
    }

    private static final class With implements Predicate<UsagePointMembership> {

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