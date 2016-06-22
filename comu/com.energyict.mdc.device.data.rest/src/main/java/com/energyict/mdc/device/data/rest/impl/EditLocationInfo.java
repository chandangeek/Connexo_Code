package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.rest.util.properties.StringValidationRules;
import com.elster.jupiter.validation.rest.BasicPropertyTypes;
import com.energyict.mdc.device.data.Device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditLocationInfo {

    public String locationValue;
    public String unformattedLocationValue;
    public String formattedLocationValue;
    public Long locationId;
    public PropertyInfo[] properties;
    public Long usagePointLocationId;
    public boolean isInherited = false;

    public EditLocationInfo() {
    }

    public EditLocationInfo(MeteringService meteringService, LocationService locationService, Thesaurus thesaurus, Device device) {
        device.getLocation().ifPresent(deviceLocation -> {
            createLocationInfo(meteringService, locationService, thesaurus, deviceLocation.getId(), device);
        });
    }

    public void createLocationInfo(MeteringService meteringService,LocationService locationService, Thesaurus thesaurus, Long locationId, Device device) {
        createLocationInfo(meteringService, locationService, thesaurus, locationId);
        device.getUsagePoint().ifPresent(usagePoint -> {
            usagePoint.getLocation().ifPresent(usagePointLocation -> {
                if (usagePointLocation.getId() == locationId) {
                    isInherited = true;
                }
                usagePointLocationId = usagePointLocation.getId();
            });
        });
    }

    public void createLocationInfo(MeteringService meteringService, LocationService locationService, Thesaurus thesaurus, Long locationId) {
        this.locationId = locationId;

        List<String> unformattedList = new ArrayList<>();
        Optional<Location> location = locationService.findLocationById(locationId);
        if (location.isPresent()) {
            List<List<String>> locationMembers = location.get().format();
            locationMembers.stream()
                    .flatMap(Collection::stream)
                    .forEach(el -> {
                        if (el == null) {
                            unformattedList.add("");
                        } else {
                            unformattedList.add(el);
                        }
                    });
            unformattedLocationValue = unformattedList.stream().collect(Collectors.joining(", "));

            List<List<String>> formattedLocationMembers = locationMembers;
            formattedLocationMembers.stream().skip(1).forEach(list ->
                    list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(0, "\\r\\n" + member)));
            formattedLocationValue = formattedLocationMembers.stream()
                    .flatMap(List::stream).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));

            locationValue = locationMembers.stream()
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
            Boolean isMandatory = false;
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
                    new PropertyTypeInfo(BasicPropertyTypes.TEXT, stringValidationRules, null, null),
                    isMandatory);
        }
        properties = infoProperties;
    }


}
