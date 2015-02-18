package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class RegisterTypeInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public boolean isLinkedByDeviceType;
    public Boolean isLinkedByActiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public Boolean isLinkedByInactiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public String unitOfMeasure;
    public ReadingTypeInfo readingType;
    public ReadingTypeInfo calculatedReadingType;

    public RegisterTypeInfo() {
    }

    public RegisterTypeInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean inLoadProfileType) {
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
    }

    public RegisterTypeInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean isLinkedByActiveRegisterSpec, boolean isLinkedByInactiveRegisterSpec) {
        this(measurementType, isLinkedByDeviceType, false);
        this.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        this.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
    }

    public void writeTo(MeasurementType measurementType, ReadingType readingType) {
        measurementType.setObisCode(this.obisCode);
        measurementType.setReadingType(readingType);
    }

}