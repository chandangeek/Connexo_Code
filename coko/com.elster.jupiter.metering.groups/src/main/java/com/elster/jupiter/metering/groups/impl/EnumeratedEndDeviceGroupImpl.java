package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
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
                EndDeviceMembershipImpl newMembership = new EndDeviceMembershipImpl(entry.getEndDevice(), IntermittentInterval.NEVER);
                map.put(entry.getEndDevice(), newMembership);
                memberships.add(newMembership);
            }
            EndDeviceMembershipImpl membership = map.get(entry.getEndDevice());
            membership.addInterval(entry.getInterval());
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

        EntryImpl init(EnumeratedEndDeviceGroup endDeviceGroup, EndDevice endDevice, Interval interval) {
            this.endDeviceGroup = endDeviceGroup;
            this.groupId = endDeviceGroup.getId();
            this.endDevice = endDevice;
            this.endDeviceId = endDevice.getId();
            this.interval = interval;
            return this;
        }

        static EntryImpl from(DataModel dataModel, EnumeratedEndDeviceGroup endDeviceGroup, EndDevice endDevice, Interval interval) {
            return dataModel.getInstance(EntryImpl.class).init(endDeviceGroup, endDevice, interval);
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
    public Entry add(EndDevice endDevice, Interval interval) {
        EndDeviceMembershipImpl membership = forEndDevice(endDevice);
        if (membership == null) {
            membership = new EndDeviceMembershipImpl(endDevice, IntermittentInterval.NEVER);
            getMemberships().add(membership);
        }
        membership.addInterval(interval);
        EntryImpl entry = EntryImpl.from(dataModel, this, endDevice, membership.resultingInterval(interval));
        getEntries().add(entry);
        return entry;
    }

    @Override
    public void remove(Entry entry) {
        EndDeviceMembershipImpl membership = forEndDevice(entry.getEndDevice());
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
            List<Entry> existingEntries = entryFactory().find("endDeviceGroup", this);
            DiffList<Entry> entryDiff = ArrayDiffList.fromOriginal(existingEntries);
            entryDiff.clear();
            for (EndDeviceMembership membership : memberships) {
                for (Interval interval : membership.getIntervals().getIntervals()) {
                    entryDiff.add(EntryImpl.from(dataModel, this, membership.getEndDevice(), interval));
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
    public List<EndDevice> getMembers(final Date date) {
        return FluentIterable.from(getMemberships())
                .filter(Active.at(date))
                .transform(To.END_DEVICE)
                .toList();
    }

    @Override
    public List<EndDeviceMembership> getMembers(Interval interval) {
        final IntermittentInterval intervalScope = IntermittentInterval.from(interval);
        return FluentIterable.from(getMemberships())
                .filter(Active.during(interval))
                .transform(new Function<EndDeviceMembershipImpl, EndDeviceMembership>() {
                    @Override
                    public EndDeviceMembership apply(EndDeviceMembershipImpl input) {
                        return input.withIntervals(input.getIntervals().intersection(intervalScope));
                    }
                })
                .toList();
    }

    @Override
    public boolean isMember(final EndDevice endDevice, Date date) {
        return !FluentIterable.from(getMemberships())
                .filter(With.endDevice(endDevice))
                .filter(Active.at(date))
                .isEmpty();
    }

    @Override
    public void endMembership(EndDevice endDevice, Date date) {
        Optional<EndDeviceMembershipImpl> first = FluentIterable.from(getMemberships())
                .filter(With.endDevice(endDevice))
                .filter(Active.at(date)).first();
        if (first.isPresent()) {
            first.get().removeInterval(Interval.startAt(date));
        }
    }

    private EndDeviceMembershipImpl forEndDevice(EndDevice endDevice) {
        return FluentIterable.from(getMemberships())
                .filter(With.endDevice(endDevice))
                .first().orNull();
    }

    private static abstract class Active implements Predicate<EndDeviceMembershipImpl> {

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
        public boolean apply(EndDeviceMembershipImpl membership) {
            return membership != null && membership.getIntervals().overlaps(IntermittentInterval.from(interval));
        }
    }


    private static class ActiveAt extends Active {
        private final Date date;

        private ActiveAt(Date date) {
            this.date = date;
        }

        @Override
        public boolean apply(EndDeviceMembershipImpl membership) {
            return membership != null && membership.getIntervals().contains(date);
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
