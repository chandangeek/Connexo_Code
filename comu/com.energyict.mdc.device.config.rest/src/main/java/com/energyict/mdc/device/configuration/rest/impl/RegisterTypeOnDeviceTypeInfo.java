package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
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
    public Boolean isCumulative;
    public ReadingTypeInfo readingType;
    public ReadingTypeInfo calculatedReadingType;
    public DeviceTypeCustomPropertySetInfo customPropertySet;
    public List<ReadingTypeInfo> multipliedCalculatedReadingType;
    public long version;
    public VersionInfo<Long> parent;

    public RegisterTypeOnDeviceTypeInfo() {
    }

    public RegisterTypeOnDeviceTypeInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean inLoadProfileType, List<? extends MeasurementType> multipliedCalculatedRegisterTypes) {
        if (inLoadProfileType) {
            measurementType = ((ChannelType) measurementType).getTemplateRegister();
        }
        this.id = measurementType.getId();
        this.obisCode = measurementType.getObisCode();
        this.isLinkedByDeviceType = isLinkedByDeviceType;
        ReadingType readingType = measurementType.getReadingType();
        this.readingType = new ReadingTypeInfo(readingType);
        if (readingType.isCumulative()){
            //TODO check whether or not it is sufficient to just provide the calculated readingtype
            isCumulative = readingType.isCumulative();
            readingType.getCalculatedReadingType().ifPresent(
                    rt -> this.calculatedReadingType = new ReadingTypeInfo(rt)
            );
        }
        if (measurementType.getUnit() != null) {
            this.unitOfMeasure = measurementType.getUnit().toString();
        }
        multipliedCalculatedRegisterTypes.forEach(o -> multipliedCalculatedReadingType.add(new ReadingTypeInfo(o.getReadingType())));
        this.version = measurementType.getVersion();
    }

    public RegisterTypeOnDeviceTypeInfo(MeasurementType measurementType,
                                        boolean isLinkedByDeviceType,
                                        boolean isLinkedByActiveRegisterSpec,
                                        boolean isLinkedByInactiveRegisterSpec,
                                        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet,
                                        List<? extends MeasurementType> multipliedCalculatedRegisterTypes) {
        this(measurementType, isLinkedByDeviceType, false, multipliedCalculatedRegisterTypes);
        this.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        this.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
        if (registeredCustomPropertySet.isPresent()) {
            this.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
    }
}