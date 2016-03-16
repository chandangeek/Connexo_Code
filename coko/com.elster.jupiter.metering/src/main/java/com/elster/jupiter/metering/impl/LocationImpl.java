package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.orm.DataModel;
import javax.inject.Inject;
import java.util.*;

public class LocationImpl implements Location {

    private long id;
    private final DataModel dataModel;
    private List<LocationMember> members = new ArrayList<>();


    @Inject
    LocationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    LocationImpl init() {
        return this;
    }

    static LocationImpl from(DataModel dataModel, String name) {
        return dataModel.getInstance(LocationImpl.class).init();
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
    public LocationMember setMember(String countryCode,
                             String countryName,
                             String administrativeArea,
                             String locality,
                             String subLocality,
                             String streetType,
                             String streetName,
                             String streetNumber,
                             String establishmentType,
                             String establishmentName,
                             String establishmentNumber,
                             String addressDetail,
                             String zipCode,
                             boolean defaultLocation,
                             String locale) {
        LocationMemberImpl temp = LocationMemberImpl.from(dataModel, this.getId(), countryCode, countryName, administrativeArea, locality, subLocality,
                streetType, streetName, streetNumber, establishmentType, establishmentName, establishmentNumber, addressDetail, zipCode,
                defaultLocation, locale);
        temp.doSave();
        return temp;
    }

    @Override
    public void remove() {
        if (hasId()) {
            members.clear();
            dataModel.mapper(Location.class).remove(this);
        }
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

