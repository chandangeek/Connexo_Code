package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.ChannelType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class RegisterTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public boolean isLinkedByDeviceType;
    public Boolean isLinkedByActiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public Boolean isLinkedByInactiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public int timeOfUse;
    public PhenomenonInfo unitOfMeasure;
    public ReadingTypeInfo readingType;

    public RegisterTypeInfo() {
    }

    public RegisterTypeInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean inLoadProfileType) {
        if(inLoadProfileType){
            measurementType = ((ChannelType) measurementType).getTemplateRegister();
        }
        this.id = measurementType.getId();
        this.name = measurementType.getName();
        this.obisCode = measurementType.getObisCode();
        this.isLinkedByDeviceType = isLinkedByDeviceType;
        this.timeOfUse = measurementType.getTimeOfUse();
        this.unitOfMeasure = PhenomenonInfo.from(measurementType.getPhenomenon());
        this.readingType = new ReadingTypeInfo(measurementType.getReadingType());
    }

    public RegisterTypeInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean isLinkedByActiveRegisterSpec, boolean isLinkedByInactiveRegisterSpec) {
        this(measurementType, isLinkedByDeviceType, false);
        this.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        this.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
    }

    public void writeTo(MeasurementType measurementType, ReadingType readingType) {
        measurementType.setName(this.name);
        measurementType.setObisCode(this.obisCode);
        measurementType.setTimeOfUse(this.timeOfUse);
        measurementType.setUnit(this.unitOfMeasure.unit);
        measurementType.setReadingType(readingType);
    }

}