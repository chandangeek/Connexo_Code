package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
public class MeasurementTypeShortInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public ReadingTypeInfo readingType;
    public String phenomenon;

    public static List<MeasurementTypeShortInfo> from(Collection<? extends MeasurementType> measurementTypes) {
        List<MeasurementTypeShortInfo> infos = new ArrayList<>(measurementTypes.size());
        for (MeasurementType mapping : measurementTypes) {
            infos.add(MeasurementTypeShortInfo.from(mapping));
        }
        return infos;
    }

    public static MeasurementTypeShortInfo from(MeasurementType measurementType) {
        MeasurementTypeShortInfo info = new MeasurementTypeShortInfo();
        info.id = measurementType.getId();
        info.obisCode = measurementType.getObisCode();
        info.readingType = new ReadingTypeInfo(measurementType.getReadingType());
        info.phenomenon = measurementType.getUnit().toString();
        return info;
    }
}