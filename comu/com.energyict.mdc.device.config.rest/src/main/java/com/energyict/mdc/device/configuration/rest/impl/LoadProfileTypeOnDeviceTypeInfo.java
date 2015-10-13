package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadProfileTypeOnDeviceTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(LocalizedTimeDuration.Adapter.class)
    public TimeDuration timeDuration;
    public List<RegisterTypeInfo> registerTypes;
    public Boolean isLinkedToActiveDeviceConf;
    public DeviceTypeCustomPropertySetInfo customPropertySet;

    public LoadProfileTypeOnDeviceTypeInfo() {
    }

    public LoadProfileTypeOnDeviceTypeInfo(LoadProfileType loadProfileType, Optional<RegisteredCustomPropertySet> registeredCustomPropertySet) {
        this.id = loadProfileType.getId();
        this.name = loadProfileType.getName();
        this.obisCode = loadProfileType.getObisCode();
        this.timeDuration = loadProfileType.getInterval();
        this.registerTypes = new ArrayList<>(loadProfileType.getChannelTypes().size());
        for (MeasurementType measurementType : loadProfileType.getChannelTypes()) {
            this.registerTypes.add(new RegisterTypeInfo(measurementType, false, true));
        }
        if (registeredCustomPropertySet.isPresent()) {
            this.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
    }
}