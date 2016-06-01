package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.rest.util.properties.StringValidationRules;
import com.elster.jupiter.validation.rest.BasicPropertyTypes;

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

    public LocationInfo(MeteringService meteringService, Thesaurus thesaurus, String mRID) {
        Optional<Location> location = meteringService.findUsagePointLocation(mRID);
        location.ifPresent(loc -> createLocationInfo(meteringService, thesaurus, loc.getId()));
    }

    public void createLocationInfo(MeteringService meteringService, Thesaurus thesaurus, Long locationId) {
        this.locationId = locationId;

        List<String> lst = meteringService.getFormattedLocationMembers(locationId).stream()
                .flatMap(l -> l.stream())
                .map(loc -> loc == null ? "" : loc)
                .collect(Collectors.toList());
        unformattedLocationValue = lst.stream().collect(Collectors.joining(", "));

        List<List<String>> formattedLocationMembers = meteringService.getFormattedLocationMembers(locationId);
        formattedLocationMembers.stream().skip(1).forEach(list ->
                list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(0, "\\r\\n" + member)));
        formattedLocationValue = formattedLocationMembers.stream()
                .flatMap(List::stream).filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        locationValue = meteringService.getFormattedLocationMembers(locationId).stream()
                .flatMap(l -> l.stream())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        List<String> templateElementsNames = meteringService.getLocationTemplate().getTemplateElementsNames().stream()
                .filter(m -> !m.equalsIgnoreCase("locale")).collect(Collectors.toList());
        PropertyInfo[] infoProperties = new PropertyInfo[templateElementsNames.size()];
        List<String> locationList = meteringService.getFormattedLocationMembers(locationId).stream()
                .flatMap(l -> l.stream()).collect(Collectors.toList());

        for (int i = 0; i < templateElementsNames.size(); i++) {
            boolean isMandatory = false;
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
                    new PropertyValueInfo<>(locationList.size() > i ? locationList.get(i) : "", null, locationList.size() > i),
                    new PropertyTypeInfo(BasicPropertyTypes.TEXT, stringValidationRules, null, null),
                    isMandatory);
        }
        properties = infoProperties;
    }
}
