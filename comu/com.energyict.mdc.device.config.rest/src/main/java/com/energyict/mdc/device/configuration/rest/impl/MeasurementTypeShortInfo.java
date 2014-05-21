package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.PhenomenonInfo;
import com.energyict.mdc.masterdata.rest.ReadingTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
public class MeasurementTypeShortInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public ReadingTypeInfo readingType;
    public PhenomenonInfo phenomenon;

    public static List<MeasurementTypeShortInfo> from(Collection<RegisterMapping> registerMappings) {
        List<MeasurementTypeShortInfo> infos = new ArrayList<>(registerMappings.size());
        for (RegisterMapping mapping : registerMappings) {
            infos.add(MeasurementTypeShortInfo.from(mapping));
        }
        return infos;
    }

    public static MeasurementTypeShortInfo from(RegisterMapping registerMapping) {
        MeasurementTypeShortInfo info = new MeasurementTypeShortInfo();
        info.id = registerMapping.getId();
        info.name = registerMapping.getName();
        info.obisCode = registerMapping.getObisCode();
        info.readingType = new ReadingTypeInfo(registerMapping.getReadingType());
        info.phenomenon = PhenomenonInfo.from(registerMapping.getPhenomenon());
        return info;
    }
}