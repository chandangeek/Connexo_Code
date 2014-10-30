package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnumeratedEndDeviceGroupImpl extends AbstractEndDeviceGroup implements EnumeratedEndDeviceGroup {

    private List<EntryImpl> entries;

    private final List<EndDeviceMembershipImpl> memberships = new ArrayList<>();

    private final DataModel dataModel;

    @Inject
    EnumeratedEndDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private List<EntryImpl> getEntries() {
        if (entries == null) {
            List<Entry> entryList = dataModel.mapper(Entry.class).find("endDeviceGroup", this);
            entries = new ArrayList<>(entryList.size());
            for (Entry entry : entryList) {
                entries.add((EntryImpl) entry);
            }
            buildMemberships();
        }
        return entries;
    }

    private void buildMemberships() {
        Map<EndDevice, EndDeviceMembershipImpl> map = new HashMap<>();
        for (EntryImpl entry : entries) {
            if (!map.containsKey(entry.getEndDevice())) {
                EndDeviceMembershipImpl newMembership = new EndDeviceMembershipImpl(entry.getEndDevice(), ImmutableRangeSet.of());
                map.put(entry.getEndDevice(), newMembership);
                memberships.add(newMembership);
            }
            EndDeviceMembershipImpl membership = map.get(entry.getEndDevice());
            membership.addRange(entry.getRange());
        }

    }

    private List<EndDeviceMembershipImpl> getMemberships() {
        if (entries == null) {
            getEntries();
        }
        return memberships;
    }

    static class EntryImpl implements Entry {

        private transient EnumeratedEndDeviceGroup endDeviceGroup;
        private long groupId;
        private transient EndDevice endDevice;
        private Interval interval;
        private long endDeviceId;

        private final DataModel dataModel;
        private final MeteringService meteringService;

        @Inject
        EntryImpl(DataModel dataModel, MeteringService meteringService) {
            this.dataModel = dataModel;
            this.meteringService = meteringService;
        }

        EntryImpl init(EnumeratedEndDeviceGroup endDeviceGroup, EndDevice endDevice, Range<Instant> range) {
            this.endDeviceGroup = endDeviceGroup;
            this.groupId = endDeviceGroup.getId();
            this.endDevice = endDevice;
            this.endDeviceId = endDevice.getId();
            this.interval = Interval.of(Effectivity.requireValid(range));
            return this;
        }

        static EntryImpl from(DataModel dataModel, EnumeratedEndDeviceGroup endDeviceGroup, EndDevice endDevice, Range<Instant> range) {
            return dataModel.getInstance(EntryImpl.class).init(endDeviceGroup, endDevice, range);
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public EndDevice getEndDevice() {
            if (endDevice == null) {
                endDevice = meteringService.findEndDevice(endDeviceId).get();
            }
            return endDevice;
        }

        public EnumeratedEndDeviceGroup getEndDeviceGroup() {
            if (endDeviceGroup == null) {
                endDeviceGroup = dataModel.mapper(EnumeratedEndDeviceGroup.class).getOptional(groupId).get();
        }
            return endDeviceGroup;
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

            return groupId == entry.groupId && endDeviceId == entry.endDeviceId && Objects.equals(interval.getStart(), entry.interval.getStart());

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, endDeviceId, interval.getStart());
        }
    }

    @Override
    public Entry add(EndDevice endDevice, Range<Instant> range) {
        EndDeviceMembershipImpl membership = forEndDevice(endDevice);
        if (membership == null) {
            membership = new EndDeviceMembershipImpl(endDevice, ImmutableRangeSet.of());
            getMemberships().add(membership);
        }
        membership.addRange(range);
        EntryImpl entry = EntryImpl.from(dataModel, this, endDevice, membership.resultingRange(range));
        getEntries().add(entry);
        return entry;
    }

    @Override
    public void remove(Entry entry) {
        EndDeviceMembershipImpl membership = forEndDevice(entry.getEndDevice());
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
            List<Entry> existingEntries = entryFactory().find("endDeviceGroup", this);
            DiffList<Entry> entryDiff = ArrayDiffList.fromOriginal(existingEntries);
            entryDiff.clear();
            for (EndDeviceMembership membership : memberships) {
                for (Range<Instant> range : membership.getRanges().asRanges()) {
                    entryDiff.add(EntryImpl.from(dataModel, this, membership.getEndDevice(), range));
                }
            }
            entryFactory().remove(FluentIterable.from(entryDiff.getRemovals()).toList());
            entryFactory().update(FluentIterable.from(entryDiff.getRemaining()).toList());
            entryFactory().persist(FluentIterable.from(entryDiff.getAdditions()).toList());
        }

    }

    private DataMapper<EnumeratedEndDeviceGroup> factory() {
        return dataModel.mapper(EnumeratedEndDeviceGroup.class);
    }

    private DataMapper<Entry> entryFactory() {
        return dataModel.mapper(Entry.class);
    }

    @Override
    public List<EndDevice> getMembers(final Instant instant) {
        return FluentIterable.from(getMemberships())
                .filter(Active.at(instant))
                .transform(To.END_DEVICE)
                .toList();
    }

    @Override
    public List<EndDeviceMembership> getMembers(Range<Instant> range) {
        return FluentIterable.from(getMemberships())
                .filter(Active.during(range))
                .transform(new Function<EndDeviceMembershipImpl, EndDeviceMembership>() {
                    @Override
                    public EndDeviceMembership apply(EndDeviceMembershipImpl input) {
                        return input.withRanges(input.getRanges().subRangeSet(range));
                    }
                })
                .toList();
    }

    @Override
    public boolean isMember(final EndDevice endDevice, Instant instant) {
        return !FluentIterable.from(getMemberships())
                .filter(With.endDevice(endDevice))
                .filter(Active.at(instant))
                .isEmpty();
    }

    @Override
    public void endMembership(EndDevice endDevice, Instant instant) {
    	getMemberships().stream()
        	.filter( member -> member.getEndDevice().equals(endDevice))
            .filter( member -> member.getRanges().contains(instant))
            .findFirst()
            .ifPresent(member -> member.removeRange(Range.atLeast(instant)));
    }

    @Override
    public Subquery getAmrIdSubQuery() {
        MeteringService service = dataModel.getInstance(MeteringService.class);
        Query<EndDevice> endDeviceQuery = service.getEndDeviceQuery();

        QueryExecutor<EntryImpl> query = dataModel.query(EntryImpl.class);
        Subquery subQueryEndDeviceIdInGroup = query.asSubquery(Where.where("endDeviceGroup").isEqualTo(this), "endDeviceId");
        return endDeviceQuery.asSubquery(ListOperator.IN.contains(subQueryEndDeviceIdInGroup, "id"), "amrId");
    }

    private EndDeviceMembershipImpl forEndDevice(EndDevice endDevice) {
        return FluentIterable.from(getMemberships())
                .filter(With.endDevice(endDevice))
                .first().orNull();
    }

    private static abstract class Active implements Predicate<EndDeviceMembershipImpl> {

        public static Active at(Instant instant) {
            return new ActiveAt(instant);
        }

        public static Active during(Range<Instant> range) {
            return new ActiveDuring(range);
        }
    }

    private static class ActiveDuring extends Active {
        private final Range<Instant> range;

        private ActiveDuring(Range<Instant> range) {
            this.range = range;
        }

        @Override
        public boolean apply(EndDeviceMembershipImpl membership) {
            return membership != null && !membership.getRanges().subRangeSet(range).isEmpty();
        }
    }


    private static class ActiveAt extends Active {
        private final Instant instant;

        private ActiveAt(Instant instant) {
            this.instant = instant;
        }

        @Override
        public boolean apply(EndDeviceMembershipImpl membership) {
            return membership != null && membership.getRanges().contains(instant);
        }
    }

    private enum To implements Function<EndDeviceMembership, EndDevice> {

        END_DEVICE;

        @Override
        public EndDevice apply(EndDeviceMembership membership) {
            return membership.getEndDevice();
        }
    }

    private static class With implements Predicate<EndDeviceMembership> {

        private final EndDevice endDevice;

        private With(EndDevice endDevice) {
            this.endDevice = endDevice;
        }

        public static With endDevice(EndDevice endDevice) {
            return new With(endDevice);
        }

        @Override
        public boolean apply(EndDeviceMembership membership) {
            return membership.getEndDevice().equals(endDevice);
        }
    }
}
