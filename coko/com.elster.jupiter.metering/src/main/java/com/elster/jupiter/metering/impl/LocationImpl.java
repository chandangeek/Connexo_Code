package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.*;

public class LocationImpl implements Location {

    private long id;
    private String name;
    private final DataModel dataModel;
    private List<LocationMember> members = new ArrayList<>();


    @Inject
    LocationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    LocationImpl init(String name) {
        this.name=name;
        return this;
    }

    static LocationImpl from(DataModel dataModel, String name) {
        return dataModel.getInstance(LocationImpl.class).init(name);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Optional<List<? extends LocationMember>> getMembers() {
        return Optional.of(Collections.unmodifiableList(members));
    }

    @Override
    public Optional<LocationMember> getMember(String locale) {
       return members.stream().filter(location -> location.getLocale().equalsIgnoreCase(locale))
                .findFirst();
    }

    void doSave() {
        if (hasId()) {
            dataModel.mapper(Location.class).update(this);
            return;
        }
        dataModel.mapper(Location.class).persist(this);
    }

    private boolean hasId() {
        return id != 0L;
    }

    @Override
    public void remove() {
        if (hasId()) {
            members.clear();
            dataModel.mapper(Location.class).remove(this);
        }
    }


    @Override
    public String getName() {
        return name;
    }

    LocationMemberImpl add(LocationMemberImpl member) {
        members.add(member);
        return member;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationImpl)) return false;
        LocationImpl kpi = (LocationImpl) o;
        return Objects.equals(id, kpi.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

}

