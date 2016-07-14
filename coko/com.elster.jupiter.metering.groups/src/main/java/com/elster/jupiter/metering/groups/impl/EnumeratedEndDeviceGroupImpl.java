package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.Save;
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
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.Interval;

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
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

class EnumeratedEndDeviceGroupImpl extends AbstractEndDeviceGroup implements EnumeratedEndDeviceGroup {

    private final QueryService queryService;
    private final ExecutionTimer endDeviceGroupMemberCountTimer;

    private List<EntryImpl> entries;

    private final List<EndDeviceMembershipImpl> memberships = new ArrayList<>();

    @Inject
    EnumeratedEndDeviceGroupImpl(DataModel dataModel, EventService eventService, QueryService queryService, ExecutionTimer endDeviceGroupMemberCountTimer) {
        super(eventService, dataModel);
        this.queryService = queryService;
        this.endDeviceGroupMemberCountTimer = endDeviceGroupMemberCountTimer;
    }

    public List<EntryImpl> getEntries() {
        return doGetEntries();
    }

    private List<EntryImpl> doGetEntries() {
        if (entries == null) {
            this.entries = new ArrayList<>(this.loadEntries()); // Take a copy of the List
            buildMemberships();
        }
        return entries;
    }

    private List<EntryImpl> loadEntries() {
        return getDataModel().mapper(EntryImpl.class).find("endDeviceGroup", this);
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

    @Override
    public Entry add(EndDevice endDevice, Range<Instant> range) {
        EndDeviceMembershipImpl membership = forEndDevice(endDevice);
        if (membership == null) {
            membership = new EndDeviceMembershipImpl(endDevice, ImmutableRangeSet.of());
            getMemberships().add(membership);
        }
        membership.addRange(range);
        EntryImpl entry = EntryImpl.from(getDataModel(), this, endDevice, membership.resultingRange(range));
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
        update();
    }

    void save() {
        Save.CREATE.save(getDataModel(), this);
        for (EntryImpl entry : doGetEntries()) {
            entry.setEndDeviceGroup(this);
        }
        List<Entry> result = new ArrayList<>();
        for (EntryImpl entry : doGetEntries()) {
            result.add(entry);
        }
        entryFactory().persist(result);
    }

    @Override
    public void update() {
        Save.UPDATE.save(getDataModel(), this);
        List<Entry> existingEntries = entryFactory().find("endDeviceGroup", this);
        DiffList<Entry> entryDiff = ArrayDiffList.fromOriginal(existingEntries);
        entryDiff.clear();
        for (EndDeviceMembership membership : memberships) {
            for (Range<Instant> range : membership.getRanges().asRanges()) {
                entryDiff.add(EntryImpl.from(getDataModel(), this, membership.getEndDevice(), range));
            }
        }
        entryFactory().remove(entryDiff.getRemovals().stream().collect(Collectors.toList()));
        entryFactory().update(entryDiff.getRemaining().stream().collect(Collectors.toList()));
        entryFactory().persist(entryDiff.getAdditions().stream().collect(Collectors.toList()));
    }

    @Override
    public void delete() {
        List<EntryImpl> entries;
        if (this.entries == null) {
            entries = this.loadEntries();
        } else {
            entries = this.entries;
        }
        entries.forEach(EntryImpl::delete);
        this.entries.clear();
        super.delete();
    }

    private DataMapper<Entry> entryFactory() {
        return getDataModel().mapper(Entry.class);
    }

    @Override
    public List<EndDevice> getMembers(final Instant instant) {
        return this.getMemberStream(instant).collect(Collectors.toList());
    }

    private Stream<EndDevice> getMemberStream(Instant instant) {
        return getMemberships().stream()
                .sorted(new Comparator<EndDeviceMembership>() {
                    @Override
                    public int compare(EndDeviceMembership o1, EndDeviceMembership o2) {
                        return o1.getEndDevice().getName().compareToIgnoreCase(o2.getEndDevice().getName());
                    }
                })
                .filter(Active.at(instant))
                .map(To.END_DEVICE);
    }

    @Override
    public long getMemberCount(Instant instant) {
        try {
            return this.endDeviceGroupMemberCountTimer.time(() -> this.getMemberStream(instant).count());
        } catch (Exception e) {
            // actual Caller implementation is not throwing any exception
            return 0;
        }
    }

    @Override
    public List<EndDevice> getMembers(Instant instant, int start, int limit) {
        Query<Entry> query = queryService.wrap(getDataModel().query(Entry.class, EndDeviceGroup.class, EndDevice.class));
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
        MeteringService service = getDataModel().getInstance(MeteringService.class);
        Query<EndDevice> endDeviceQuery = service.getEndDeviceQuery();

        QueryExecutor<EntryImpl> query = getDataModel().query(EntryImpl.class);
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

    static class EntryImpl implements Entry {

        private final DataModel dataModel;
        private Reference<EnumeratedEndDeviceGroup> endDeviceGroup = ValueReference.absent();
        private Reference<EndDevice> endDevice = ValueReference.absent();
        private Interval interval;
        @SuppressWarnings("unused") // Managed by ORM
        private Instant createTime;
        @SuppressWarnings("unused") // Managed by ORM
        private String userName;

        @Inject
        EntryImpl(DataModel dataModel) {
            this.dataModel = dataModel;
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

        void delete() {
            this.dataModel.remove(this);
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

    private abstract static class Active implements Predicate<EndDeviceMembershipImpl> {

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
        public boolean test(EndDeviceMembershipImpl membership) {
            return membership != null && !membership.getRanges().subRangeSet(range).isEmpty();
        }
    }


    private static final class ActiveAt extends Active {
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

    private static final class With implements Predicate<EndDeviceMembership> {

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