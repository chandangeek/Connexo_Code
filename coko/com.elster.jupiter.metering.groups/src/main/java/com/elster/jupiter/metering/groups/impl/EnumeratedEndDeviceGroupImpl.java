package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class EnumeratedEndDeviceGroupImpl extends AbstractEndDeviceGroup implements EnumeratedEndDeviceGroup {

    private final QueryService queryService;
    
    private List<EntryImpl> entries;

    private final List<EndDeviceMembershipImpl> memberships = new ArrayList<>();

    @Inject
    EnumeratedEndDeviceGroupImpl(DataModel dataModel, EventService eventService, QueryService queryService) {
        super(eventService, dataModel);
        this.queryService = queryService;
    }

    public List<EntryImpl> getEntries() {
        return doGetEntries();
    }

    private List<EntryImpl> doGetEntries() {
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

        private Reference<EnumeratedEndDeviceGroup> endDeviceGroup = ValueReference.absent();
        private Reference<EndDevice> endDevice = ValueReference.absent();
        private Interval interval;

        private final DataModel dataModel;
        private final MeteringService meteringService;

        @Inject
        EntryImpl(DataModel dataModel, MeteringService meteringService) {
            this.dataModel = dataModel;
            this.meteringService = meteringService;
        }

        EntryImpl init(EnumeratedEndDeviceGroup endDeviceGroup, EndDevice endDevice, Range<Instant> range) {
            setEndDeviceGroup(endDeviceGroup);
            setEndDevice(endDevice);
            this.interval = Interval.of(Effectivity.requireValid(range));
            return this;
        }

        static EntryImpl from(DataModel dataModel, EnumeratedEndDeviceGroup endDeviceGroup, EndDevice endDevice, Range<Instant> range) {
            return dataModel.getInstance(EntryImpl.class).init(endDeviceGroup, endDevice, range);
        }
        
        public void setEndDevice(EndDevice endDevice) {
            this.endDevice.set(endDevice);
        }
        
        public void setEndDeviceGroup(EnumeratedEndDeviceGroup endDeviceGroup) {
            this.endDeviceGroup.set(endDeviceGroup);
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public EndDevice getEndDevice() {
            return endDevice.get();
        }

        public EnumeratedEndDeviceGroup getEndDeviceGroup() {
            return endDeviceGroup.get();
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

            return endDeviceGroup.get().getId() == entry.endDeviceGroup.get().getId()
                    && endDevice.get().getId() == entry.endDevice.get().getId()
                    && Objects.equals(interval.getStart(), entry.interval.getStart());
        }

        @Override
        public int hashCode() {
            return Objects.hash(endDeviceGroup.get().getId(), endDevice.get().getId(), interval.getStart());
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
        doGetEntries().add(entry);
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
        save();
    }

    @Override
    public void save() {
        if (id == 0) {
            factory().persist(this);
            for (EntryImpl entry : doGetEntries()) {
                entry.setEndDeviceGroup(this);
            }
            ArrayList<Entry> result = new ArrayList<>();
            for (EntryImpl entry : doGetEntries()) {
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
        return getMemberships().stream()
                .sorted(new Comparator<EndDeviceMembership>() {
                    @Override
                    public int compare(EndDeviceMembership o1, EndDeviceMembership o2) {
                        return o1.getEndDevice().getName().compareToIgnoreCase(o2.getEndDevice().getName());
                    }
                })
                .filter(Active.at(instant))
                .map(To.END_DEVICE)
                .collect(Collectors.toList());
    }

    @Override
    public List<EndDevice> getMembers(Instant instant, int start, int limit) {
        Query<Entry> query = queryService.wrap(dataModel.query(Entry.class, EndDeviceGroup.class, EndDevice.class));
        query.setEager();
        Condition condition = where("endDeviceGroup").isEqualTo(this).and(where("interval").isEffective(instant));
        int from = start + 1;
        int to = from + limit;
        List<Entry> entryList = query.select(condition, from, to, Order.ascending("endDevice.mRID").toUpperCase());
        return entryList.stream().map(Entry::getEndDevice).collect(Collectors.toList());
    }
    
    @Override
    public List<EndDeviceMembership> getMembers(Range<Instant> range) {
        return getMemberships().stream()
                .filter(Active.during(range))
                .map(input -> input.withRanges(input.getRanges().subRangeSet(range)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMember(final EndDevice endDevice, Instant instant) {
        return getMemberships().stream()
                .filter(Active.at(instant))
                .anyMatch(With.endDevice(endDevice));
    }

    @Override
    public void endMembership(EndDevice endDevice, Instant instant) {
        getMemberships().stream()
                .filter(With.endDevice(endDevice))
                .filter(Active.at(instant))
                .findFirst()
                .ifPresent(member -> member.removeRange(Range.atLeast(instant)));
    }

    @Override
    public Subquery getAmrIdSubQuery(AmrSystem... amrSystems) {
        MeteringService service = dataModel.getInstance(MeteringService.class);
        Query<EndDevice> endDeviceQuery = service.getEndDeviceQuery();

        QueryExecutor<EntryImpl> query = dataModel.query(EntryImpl.class);
        Subquery subQueryEndDeviceIdInGroup = query.asSubquery(where("endDeviceGroup").isEqualTo(this), "endDevice");
        Condition condition = ListOperator.IN.contains(subQueryEndDeviceIdInGroup, "id");
        if (amrSystems.length > 0) {
            condition = condition.and(ListOperator.IN.contains("amrSystem", Arrays.asList(amrSystems)));
        }

        return endDeviceQuery.asSubquery(condition, "amrId");
    }

    private EndDeviceMembershipImpl forEndDevice(EndDevice endDevice) {
        return getMemberships().stream()
                .filter(With.endDevice(endDevice))
                .findFirst()
                .orElse(null);
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
        public boolean test(EndDeviceMembershipImpl membership) {
            return membership != null && !membership.getRanges().subRangeSet(range).isEmpty();
        }
    }


    private static class ActiveAt extends Active {
        private final Instant instant;

        private ActiveAt(Instant instant) {
            this.instant = instant;
        }

        @Override
        public boolean test(EndDeviceMembershipImpl membership) {
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
        public boolean test(EndDeviceMembership membership) {
            return membership.getEndDevice().equals(endDevice);
        }
    }
}
