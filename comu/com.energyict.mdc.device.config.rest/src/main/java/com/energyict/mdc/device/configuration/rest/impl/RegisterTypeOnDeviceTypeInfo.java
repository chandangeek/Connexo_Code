package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterTypeOnDeviceTypeInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public boolean isLinkedByDeviceType;
    public Boolean isLinkedByActiveRegisterConfig;
    public Boolean isLinkedByInactiveRegisterConfig;
    public String unitOfMeasure;
    public ReadingTypeInfo readingType;
    public ReadingTypeInfo calculatedReadingType;
    public DeviceTypeCustomPropertySetInfo customPropertySet;
    public long version;
    public VersionInfo<Long> parent;

    public RegisterTypeOnDeviceTypeInfo() {
    }

    public RegisterTypeOnDeviceTypeInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean inLoadProfileType) {
        if (inLoadProfileType) {
            measurementType = ((ChannelType) measurementType).getTemplateRegister();
        }
        this.id = measurementType.getId();
        this.obisCode = measurementType.getObisCode();
        this.isLinkedByDeviceType = isLinkedByDeviceType;
        ReadingType readingType = measurementType.getReadingType();
        this.readingType = new ReadingTypeInfo(readingType);
        if (readingType.isCumulative()){
            readingType.getCalculatedReadingType().ifPresent(
                    rt -> this.calculatedReadingType = new ReadingTypeInfo(rt)
            );
        }
        if (measurementType.getUnit() != null) {
            this.unitOfMeasure = measurementType.getUnit().toString();
        }
        this.version = measurementType.getVersion();
    }

    public RegisterTypeOnDeviceTypeInfo(MeasurementType measurementType,
                                        boolean isLinkedByDeviceType,
                                        boolean isLinkedByActiveRegisterSpec,
                                        boolean isLinkedByInactiveRegisterSpec,
                                        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet) {
        this(measurementType, isLinkedByDeviceType, false);
        this.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        this.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
        if (registeredCustomPropertySet.isPresent()) {
            this.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
    }
}