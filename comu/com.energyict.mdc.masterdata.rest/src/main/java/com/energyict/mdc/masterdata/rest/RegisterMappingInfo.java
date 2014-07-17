package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.PhenomenonAdapter;
import com.energyict.mdc.masterdata.RegisterMapping;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class RegisterMappingInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public boolean isLinkedByDeviceType;
    public Boolean isLinkedByActiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public Boolean isLinkedByInactiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public int timeOfUse;
    @XmlJavaTypeAdapter(PhenomenonAdapter.class)
    public Phenomenon phenomenon;
    public ReadingTypeInfo readingType;

    public RegisterMappingInfo() {
    }

    public RegisterMappingInfo(RegisterMapping registerMapping, boolean isLinkedByDeviceType) {
        this.id = registerMapping.getId();
        this.name = registerMapping.getName();
        this.obisCode = registerMapping.getObisCode();
        this.isLinkedByDeviceType = isLinkedByDeviceType;
        this.timeOfUse = registerMapping.getTimeOfUse();
        this.phenomenon = registerMapping.getPhenomenon();
        this.readingType = new ReadingTypeInfo(registerMapping.getReadingType());
    }

    public RegisterMappingInfo(RegisterMapping registerMapping, boolean isLinkedByDeviceType, boolean isLinkedByActiveRegisterSpec, boolean isLinkedByInactiveRegisterSpec) {
        this(registerMapping, isLinkedByDeviceType);
        this.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        this.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
    }

    public void writeTo(RegisterMapping registerMapping, ReadingType readingType) {
        registerMapping.setName(this.name);
        registerMapping.setObisCode(this.obisCode);
        registerMapping.setTimeOfUse(this.timeOfUse);
        registerMapping.setPhenomenon(this.phenomenon);
        registerMapping.setReadingType(readingType);
    }

}