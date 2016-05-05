package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.validation.rest.BasicPropertyTypes;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.DeviceAttributesInfo.DeviceAttribute;
import com.energyict.mdc.device.lifecycle.config.DefaultState;


import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocationInfoFactory {
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    @Inject
    public LocationInfoFactory(MeteringService meteringService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    public EditLocationInfo from(Long locationId) {

        EditLocationInfo locationInfo = new EditLocationInfo();
        locationInfo.createLocationInfo(meteringService, thesaurus, locationId);
        return locationInfo;
    }
}
