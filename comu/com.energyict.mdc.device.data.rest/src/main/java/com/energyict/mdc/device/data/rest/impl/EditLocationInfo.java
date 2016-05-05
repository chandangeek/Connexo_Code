package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.impl.module.*;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasValidProperties;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.validation.rest.BasicPropertyTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EditLocationInfo {

    public String locationValue;
    public String unformattedLocationValue;
    public String formattedLocationValue;
    public Long locationId;
    public PropertyInfo[] properties;

    public EditLocationInfo() {
    }

    public void createLocationInfo(MeteringService meteringService, Thesaurus thesaurus, Long locationId) {
        this.locationId = locationId;

        List<String> lst = new ArrayList<>();
        meteringService.getFormattedLocationMembers(locationId).stream()
                .flatMap(l -> l.stream())
                .forEach(el -> {
                    if (el == null){
                        lst.add("");
                    }
                    else {
                        lst.add(el);
                    }
                });
        unformattedLocationValue  = lst.stream().collect(Collectors.joining(", "));

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
            Boolean isMandatory = false;
            String field = templateElementsNames.get(i);
            isMandatory = meteringService.getLocationTemplate().getTemplateMembers().stream()
                    .filter(member -> member.getName().equalsIgnoreCase(field))
                    .collect(Collectors.toList()).
                            get(0).isMandatory();

            infoProperties[i] = new PropertyInfo(thesaurus.getString(templateElementsNames.get(i), templateElementsNames.get(i)),
                    templateElementsNames.get(i),
                    new PropertyValueInfo<>(locationList.size() > i ? locationList.get(i) : "", null),
                    new PropertyTypeInfo(BasicPropertyTypes.TEXT, null, null, null),
                    isMandatory);
        }
        properties = infoProperties;
    }

    public EditLocationInfo(MeteringService meteringService, Thesaurus thesaurus, String mRID) {
        Optional<Location> location = meteringService.findDeviceLocation(mRID);
        if (location.isPresent()) {
            createLocationInfo( meteringService, thesaurus, location.get().getId());

        }
    }

}
