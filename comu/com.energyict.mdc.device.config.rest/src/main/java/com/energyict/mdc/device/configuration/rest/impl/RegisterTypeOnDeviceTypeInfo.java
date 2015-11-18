package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.MeasurementType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
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

    /* The ReadingType from the RegisterType */
    public ReadingTypeInfo readingType;

    /* In case of a Channel, we need to add the interval ... */
    public ReadingTypeInfo collectedReadingType;

    /* A list of possible readingTypes where the 'multiplied' value can be stored */
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();

    public DeviceTypeCustomPropertySetInfo customPropertySet;
    public long version;
    public VersionInfo<Long> parent;

    @SuppressWarnings("unused")
    public RegisterTypeOnDeviceTypeInfo() {
    }

    public RegisterTypeOnDeviceTypeInfo(MeasurementType measurementType,
                                        boolean isLinkedByDeviceType,
                                        boolean isLinkedByActiveRegisterSpec,
                                        boolean isLinkedByInactiveRegisterSpec,
                                        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet,
                                        List<ReadingType> multipliedCalculatedRegisterTypes,
                                        ReadingType collectedReadingType) {

        this.id = measurementType.getId();
        this.obisCode = measurementType.getObisCode();
        this.isLinkedByDeviceType = isLinkedByDeviceType;
        ReadingType readingType = measurementType.getReadingType();
        this.readingType = new ReadingTypeInfo(readingType);
        this.isCumulative = readingType.isCumulative();
        this.collectedReadingType = new ReadingTypeInfo(collectedReadingType);

        if (measurementType.getUnit() != null) {
            this.unitOfMeasure = measurementType.getUnit().toString();
        }
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> possibleCalculatedReadingTypes.add(new ReadingTypeInfo(readingTypeConsumer)));
        this.version = measurementType.getVersion();

        this.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        this.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
        if (registeredCustomPropertySet.isPresent()) {
            this.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
    }
}