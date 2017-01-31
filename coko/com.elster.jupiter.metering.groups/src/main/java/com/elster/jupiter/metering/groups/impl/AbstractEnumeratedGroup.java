/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.HasId;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

abstract class AbstractEnumeratedGroup<T extends HasId & IdentifiedObject> extends AbstractGroup<T> implements EnumeratedGroup<T> {

    private final QueryService queryService;
    private final ExecutionTimer groupMembersCountTimer;

    private List<AbstractEntry<T>> entries;

    private final List<MembershipImpl<T>> memberships = new ArrayList<>();

    AbstractEnumeratedGroup(DataModel dataModel, EventService eventService, QueryService queryService, ExecutionTimer groupMembersCountTimer) {
        super(eventService, dataModel);
        this.queryService = queryService;
        this.groupMembersCountTimer = groupMembersCountTimer;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public List<? extends AbstractEntry<T>> getEntries() {
        return fetchEntries();
    }

    private List<AbstractEntry<T>> fetchEntries() {
        if (entries == null) {
            this.entries = new ArrayList<>(loadEntriesAs(getEntryApiClass())); // Take a copy of the List
            buildMemberships();
        }
        return entries;
    }

    private <C extends AbstractEntry<T>> List<C> loadEntriesAs(Class<C> api) {
        return getDataModel().mapper(api).find("group", this);
    }

    private void buildMemberships() {
        Map<T, MembershipImpl<T>> map = new HashMap<>();
        for (AbstractEntry<T> entry : entries) {
            if (!map.containsKey(entry.getMember())) {
                MembershipImpl<T> newMembership = new MembershipImpl<>(entry.getMember(), ImmutableRangeSet.of());
                map.put(entry.getMember(), newMembership);
                memberships.add(newMembership);
            }
            MembershipImpl<T> membership = map.get(entry.getMember());
            membership.addRange(entry.getRange());
        }
    }

    private List<MembershipImpl<T>> getMemberships() {
        fetchEntries();
        return memberships;
    }

    @Override
    public Entry<T> add(T member, Range<Instant> range) {
        MembershipImpl<T> membership = forMember(member);
        if (membership == null) {
            membership = new MembershipImpl<>(member, ImmutableRangeSet.of());
            getMemberships().add(membership);
        }
        membership.addRange(range);
        AbstractEntry<T> entry = newEntryFrom(getEntryApiClass(), this, member, membership.resultingRange(range));
        fetchEntries().add(entry);
        return entry;
    }

    @Override
    public void remove(Entry<T> entry) {
        MembershipImpl<T> membership = forMember(entry.getMember());
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
        for (AbstractEntry<T> entry : fetchEntries()) {
            entry.setGroup(this);
        }
        saveEntriesAs(getEntryApiClass());
    }

    @Override
    public void update() {
        Save.UPDATE.save(getDataModel(), this);
        updateEntriesAs(getEntryApiClass());
    }

    private <C extends AbstractEntry<T>> void saveEntriesAs(Class<C> entryApi) {
        getDataModel().mapper(entryApi).persist(fetchEntries().stream()
                .map(entryApi::cast)
                .collect(Collectors.toList()));
    }

    private <C extends AbstractEntry<T>> void updateEntriesAs(Class<C> entryApi) {
        List<C> existingEntries = loadEntriesAs(entryApi);
        DiffList<C> entryDiff = ArrayDiffList.fromOriginal(existingEntries);
        entryDiff.clear();
        for (Membership<T> membership : memberships) {
            for (Range<Instant> range : membership.getRanges().asRanges()) {
                entryDiff.add(newEntryFrom(entryApi, this, membership.getMember(), range));
            }
        }
        DataMapper<C> dataMapper = getDataModel().mapper(entryApi);
        dataMapper.remove(entryDiff.getRemovals().stream().collect(Collectors.toList()));
        dataMapper.update(entryDiff.getRemaining().stream().collect(Collectors.toList()));
        dataMapper.persist(entryDiff.getAdditions().stream().collect(Collectors.toList()));
    }

    @Override
    public void delete() {
        List<? extends AbstractEntry<T>> entries;
        if (this.entries == null) {
            entries = this.loadEntriesAs(getEntryApiClass());
        } else {
            entries = this.entries;
        }
        entries.forEach(AbstractEntry::delete);
        if (this.entries != null) {
            this.entries.clear();
        }
        super.delete();
    }

    @Override
    public List<T> getMembers(final Instant instant) {
        return this.getMemberStream(instant)
                // Method reference doesn't work here! (Java bug JDK-8142476 / JDK-8141508)
                .sorted(Comparator.comparing(member -> member.getName(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Stream<T> getMemberStream(Instant instant) {
        return getMemberships().stream()
                .filter(Active.at(instant))
                .map(Membership::getMember);
    }

    @Override
    public long getMemberCount(Instant instant) {
        try {
            return this.groupMembersCountTimer.time(() -> this.getMemberStream(instant).count());
        } catch (Exception e) {
            // actual Caller implementation is not throwing any exception
            return 0;
        }
    }

    @Override
    public List<T> getMembers(Instant instant, int start, int limit) {
        Query<? extends AbstractEntry<T>> query = queryService.wrap(
                getDataModel().query(getEntryApiClass(), getApiClass(), getParameterApiClass()));
        query.setEager();
        Condition condition = where("group").isEqualTo(this).and(where("interval").isEffective(instant));
        return query.select(condition, start + 1, start + limit + 1, Order.ascending("member.name").toUpperCase()).stream()
                .map(Entry::getMember).collect(Collectors.toList());
    }

    @Override
    public List<Membership<T>> getMembers(Range<Instant> range) {
        return getMemberships().stream()
                .filter(Active.during(range))
                .map(input -> input.withRanges(input.getRanges().subRangeSet(range)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMember(final T object, Instant instant) {
        return getMemberships().stream()
                .filter(Active.at(instant))
                .anyMatch(With.member(object));
    }

    @Override
    public void endMembership(T member, Instant instant) {
        getMemberships().stream()
                .filter(With.member(member))
                .filter(Active.at(instant))
                .findFirst()
                .ifPresent(membership -> membership.removeRange(Range.atLeast(instant)));
    }

    @Override
    public Subquery toSubQuery(String... fields) {
        QueryExecutor<? extends AbstractEntry<T>> query = getDataModel().query(getEntryApiClass());
        Subquery subQueryForIdsInGroup = query.asSubquery(where("group").isEqualTo(this), "member");
        Condition condition = ListOperator.IN.contains(subQueryForIdsInGroup, "id");
        return getBasicQuerySupplier().get().asSubquery(condition, fields);
    }

    private MembershipImpl<T> forMember(T member) {
        return getMemberships().stream()
                .filter(With.member(member))
                .findFirst()
                .orElse(null);
    }

    private <C extends AbstractEntry<T>> C newEntryFrom(Class<C> entryApi, EnumeratedGroup<T> group,
                                                        T member, Range<Instant> range) {
        C result = getDataModel().getInstance(entryApi);
        result.init(group, member, range);
        return result;
    }

    abstract Class<? extends AbstractEntry<T>> getEntryApiClass();
    abstract Class<? extends Group<T>> getApiClass();

    abstract static class AbstractEntry<T extends HasId & IdentifiedObject> implements Entry<T> {
        private final DataModel dataModel;

        private Reference<EnumeratedGroup<T>> group = ValueReference.absent();
        private Reference<T> member = ValueReference.absent();
        private Interval interval;
        @SuppressWarnings("unused") // Managed by ORM
        private Instant createTime;
        @SuppressWarnings("unused") // Managed by ORM
        private String userName;

        AbstractEntry(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        AbstractEntry<T> init(EnumeratedGroup<T> group, T member, Range<Instant> range) {
            setGroup(group);
            setMember(member);
            this.interval = Interval.of(Effectivity.requireValid(range));
            return this;
        }

        public void setMember(T member) {
            this.member.set(member);
        }

        public void setGroup(EnumeratedGroup<T> group) {
            this.group.set(group);
        }

        @Override
        public Interval getInterval() {
            return interval;
        }

        @Override
        public T getMember() {
            return member.get();
        }

        @Override
        public EnumeratedGroup<T> getGroup() {
            return group.get();
        }

        void delete() {
            this.dataModel.remove(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof AbstractEntry<?>)) {
                return false;
            }
            AbstractEntry<?> entry = (AbstractEntry<?>) o;

            return group.get().getId() == entry.group.get().getId()
                    && member.get().equals(entry.member.get())
                    && Objects.equals(interval.getStart(), entry.interval.getStart());
        }

        @Override
        public int hashCode() {
            return Objects.hash(group.get().getId(), member.get().getId(), interval.getStart());
        }
    }

    private abstract static class Active implements Predicate<MembershipImpl<?>> {
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
        public boolean test(MembershipImpl<?> membership) {
            return membership != null && !membership.getRanges().subRangeSet(range).isEmpty();
        }
    }

    private static final class ActiveAt extends Active {
        private final Instant instant;

        private ActiveAt(Instant instant) {
            this.instant = instant;
        }

        @Override
        public boolean test(MembershipImpl<?> membership) {
            return membership != null && membership.getRanges().contains(instant);
        }
    }

    private static final class With<T> implements Predicate<Membership<T>> {
        private final T member;

        private With(T member) {
            this.member = member;
        }

        public static <T> With<T> member(T member) {
            return new With<>(member);
        }

        @Override
        public boolean test(Membership<T> membership) {
            return membership.getMember().equals(member);
        }
    }
}
