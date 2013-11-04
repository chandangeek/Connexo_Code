package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnumeratedUsagePointGroupImpl implements EnumeratedUsagePointGroup {

    private long id;

    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String type;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    private List<EntryImpl> entries;

    private final List<UsagePointMembership> memberships = new ArrayList<>();

    private List<EntryImpl> getEntries() {
        if (entries == null) {
            List<Entry> entryList = Bus.getOrmClient().getEnumeratedUsagePointGroupEntryFactory().find("usagePointGroup", this);
            entries = new ArrayList<>(entryList.size());
            for (Entry entry : entryList) {
                entries.add((EntryImpl) entry);
            }
            buildMemberships();
        }
        return entries;
    }

    private void buildMemberships() {
        Map<UsagePoint, UsagePointMembership> map = new HashMap<>();
        for (EntryImpl entry : entries) {
            if (!map.containsKey(entry.getUsagePoint())) {
                UsagePointMembership newMembership = new UsagePointMembership(entry.getUsagePoint(), IntermittentInterval.NEVER);
                map.put(entry.getUsagePoint(), newMembership);
                memberships.add(newMembership);
            }
            UsagePointMembership membership = map.get(entry.getUsagePoint());
            membership.addInterval(entry.getInterval());
        }

    }

    private List<UsagePointMembership> getMemberships() {
        if (entries == null) {
            getEntries();
        }
        return memberships;
    }

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

    static class EntryImpl implements Entry {

        private transient EnumeratedUsagePointGroup usagePointGroup;
        private long groupId;
        private transient UsagePoint usagePoint;
        private Interval interval;
        private long usagePointId;

        private EntryImpl() {

        }

        public EntryImpl(EnumeratedUsagePointGroup usagePointGroup, UsagePoint usagePoint, Interval interval) {
            this.usagePointGroup = usagePointGroup;
            this.groupId = usagePointGroup.getId();
            this.usagePoint = usagePoint;
            this.usagePointId = usagePoint.getId();
            this.interval = interval;
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public UsagePoint getUsagePoint() {
            if (usagePoint == null) {
                usagePoint = Bus.getOrmClient().getUsagePointFactory().get(usagePointId).get();
            }
            return usagePoint;
        }

        public EnumeratedUsagePointGroup getUsagePointGroup() {
            if (usagePointGroup == null) {
                usagePointGroup = factory().get(groupId).get();
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
        UsagePointMembership membership = forUsagePoint(usagePoint);
        if (membership == null) {
            membership = new UsagePointMembership(usagePoint, IntermittentInterval.NEVER);
            getMemberships().add(membership);
        }
        membership.addInterval(interval);
        EntryImpl entry = new EntryImpl(this, usagePoint, membership.resultingInterval(interval));
        getEntries().add(entry);
        return entry;
    }

    @Override
    public void remove(Entry entry) {
        UsagePointMembership membership = forUsagePoint(entry.getUsagePoint());
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
                    entryDiff.add(new EntryImpl(this, membership.getUsagePoint(), interval));
                }
            }
            entryFactory().remove(FluentIterable.from(entryDiff.getRemovals()).toList());
            entryFactory().update(FluentIterable.from(entryDiff.getRemaining()).toList());
            entryFactory().persist(FluentIterable.from(entryDiff.getAdditions()).toList());
        }

    }

    private static DataMapper<EnumeratedUsagePointGroup> factory() {
        return Bus.getOrmClient().getEnumeratedUsagePointGroupFactory();
    }

    private DataMapper<Entry> entryFactory() {
        return Bus.getOrmClient().getEnumeratedUsagePointGroupEntryFactory();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<UsagePoint> getMembers(final Date date) {
        return FluentIterable.from(getMemberships())
                .filter(Active.at(date))
                .transform(To.USAGE_POINT)
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
        Optional<UsagePointMembership> first = FluentIterable.from(getMemberships())
                .filter(With.usagePoint(usagePoint))
                .filter(Active.at(date)).first();
        if (first.isPresent()) {
            first.get().removeInterval(Interval.startAt(date));
        }
    }

    private UsagePointMembership forUsagePoint(UsagePoint usagePoint) {
        return FluentIterable.from(getMemberships())
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
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setMRID(String mrid) {
        this.mRID = mrid;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
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
