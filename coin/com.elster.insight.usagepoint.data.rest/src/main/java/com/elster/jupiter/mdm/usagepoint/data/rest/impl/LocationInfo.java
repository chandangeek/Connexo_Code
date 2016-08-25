package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.SimplePropertyType;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.rest.util.properties.StringValidationRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by david on 4/29/2016.
 */
public class LocationInfo {
    public String locationValue;
    public String unformattedLocationValue;
    public String formattedLocationValue;
    public Long locationId;
    public PropertyInfo[] properties;

    public LocationInfo() {
    }

    public LocationInfo(MeteringService meteringService, LocationService locationService, Thesaurus thesaurus, String mRID) {
        Optional<UsagePoint> usagePoint = meteringService.findUsagePoint(mRID);
        if (usagePoint.isPresent()) {
            Optional<Location> location = usagePoint.get().getLocation();
            location.ifPresent(loc -> createLocationInfo(meteringService, locationService, thesaurus, loc.getId()));
        }

    }

    public final void createLocationInfo(MeteringService meteringService, LocationService locationService, Thesaurus thesaurus, Long locationId) {
        this.locationId = locationId;
        List<String> unformattedList = new ArrayList<>();
        Optional<Location> location = locationService.findLocationById(locationId);
        if (location.isPresent()) {
            location.get().format().stream()
                    .flatMap(Collection::stream)
                    .map(element -> element == null ? "" : element)
                    .forEach(unformattedList::add);
            unformattedLocationValue = unformattedList.stream().collect(Collectors.joining(", "));

            List<List<String>> formattedLocationMembers = location.get().format();
            formattedLocationMembers.stream().skip(1).forEach(list ->
                    list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
            formattedLocationValue = formattedLocationMembers.stream()
                    .flatMap(List::stream).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));

            locationValue = location.get().format().stream()
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }

        List<String> templateElementsNames = meteringService.getLocationTemplate().getTemplateElementsNames().stream()
                .filter(m -> !m.equalsIgnoreCase("locale")).collect(Collectors.toList());
        PropertyInfo[] infoProperties = new PropertyInfo[templateElementsNames.size()];
        List<String> locationList = new ArrayList<>();
        if (location.isPresent()) {
            locationList = location.get().format().stream()
                    .flatMap(Collection::stream).collect(Collectors.toList());
        }
        for (int i = 0; i < templateElementsNames.size(); i++) {
            boolean isMandatory;
            String field = templateElementsNames.get(i);
            isMandatory = meteringService.getLocationTemplate().getTemplateMembers().stream()
                    .filter(member -> member.getName().equalsIgnoreCase(field))
                    .collect(Collectors.toList()).
                            get(0).isMandatory();

            StringValidationRules stringValidationRules = new StringValidationRules();
            stringValidationRules.setMinLength(0);
            stringValidationRules.setMaxLength(80);
            infoProperties[i] = new PropertyInfo(thesaurus.getString(templateElementsNames.get(i), templateElementsNames.get(i)),
                    templateElementsNames.get(i),
                    new PropertyValueInfo<>(locationList.size() > i ? locationList.get(i) : null, "", false),
                    new PropertyTypeInfo(SimplePropertyType.TEXT, stringValidationRules, null, null),
                    isMandatory);
        }
        properties = infoProperties;
    }
}
