/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocationImpl implements Location {

    private long id;
    private final DataModel dataModel;
    private final MeteringService meteringService;
    private List<LocationMember> members = new ArrayList<>();


    @Inject
    LocationImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
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
    public List<? extends LocationMember> getMembers() {
        return Collections.unmodifiableList(members);
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

    LocationMemberImpl add(LocationMemberImpl member) {
        members.add(member);
        return member;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocationImpl)) {
            return false;
        }
        LocationImpl kpi = (LocationImpl) o;
        return Objects.equals(id, kpi.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public List<List<String>> format() {
        List<List<String>> formattedLocation = new LinkedList<>();
        if (!members.isEmpty()) {
            LocationMember member = members.get(0);
            Map<String, String> memberValues = new LinkedHashMap<>();
            memberValues.put("countryCode", member.getCountryCode());
            memberValues.put("countryName", member.getCountryName());
            memberValues.put("administrativeArea", member.getAdministrativeArea());
            memberValues.put("locality", member.getLocality());
            memberValues.put("subLocality", member.getSubLocality());
            memberValues.put("streetType", member.getStreetType());
            memberValues.put("streetName", member.getStreetName());
            memberValues.put("streetNumber", member.getStreetNumber());
            memberValues.put("establishmentType", member.getEstablishmentType());
            memberValues.put("establishmentName", member.getEstablishmentName());
            memberValues.put("establishmentNumber", member.getEstablishmentNumber());
            memberValues.put("addressDetail", member.getAddressDetail());
            memberValues.put("zipCode", member.getZipCode());

            formattedLocation = meteringService.getLocationTemplate().getTemplateMembers()
                    .stream()
                    .sorted((m1, m2) -> Integer.compare(m1.getRanking(), m2.getRanking()))
                    .filter(m -> !m.getName().equalsIgnoreCase("locale"))
                    .collect(() -> {
                                List<List<String>> list = new ArrayList<>();
                                list.add(new ArrayList<>());
                                return list;
                            },
                            (list, s) -> {
                                if (meteringService.getLocationTemplate().getSplitLineElements().contains(s.getAbbreviation())) {
                                    list.add(new ArrayList<String>() {{
                                        add(memberValues.get(s.getName()));
                                    }});

                                } else {
                                    list.get(list.size() - 1).add(memberValues.get(s.getName()));
                                }
                            },
                            (list1, list2) -> {
                                list1.get(list1.size() - 1).addAll(list2.remove(0));
                                list1.addAll(list2);
                            });
        }

        return formattedLocation;

    }

    @Override
    public final String toString() {
        return this.format().stream()
                .flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.joining(", "));

    }


}