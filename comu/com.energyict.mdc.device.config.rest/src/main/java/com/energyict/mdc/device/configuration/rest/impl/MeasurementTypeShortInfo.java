package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MeasurementTypeShortInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public Boolean isCumulative;

    /* The ReadingType from the RegisterType */
    public ReadingTypeInfo readingType;

    /* This will be the actual readingType which is collected  */
    public ReadingTypeInfo collectedReadingType;

    /* A list of possible readingTypes where the 'multiplied' value can be stored */
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    public long version;
    public VersionInfo<Long> parent;

    @SuppressWarnings("unused")
    public MeasurementTypeShortInfo() {
    }

    public MeasurementTypeShortInfo(ChannelType channelType,
                                    ReadingType collectedReadingType,
                                    List<ReadingType> multipliedCalculatedRegisterTypes){
        this.id = channelType.getId();
        MeasurementType measurementType = channelType.getTemplateRegister();
        this.obisCode = measurementType.getObisCode();
        ReadingType readingType = measurementType.getReadingType();
        this.readingType = new ReadingTypeInfo(readingType);
        this.isCumulative = readingType.isCumulative();
        this.collectedReadingType = new ReadingTypeInfo(collectedReadingType);
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> possibleCalculatedReadingTypes.add(new ReadingTypeInfo(readingTypeConsumer)));
        this.version = measurementType.getVersion();
    }
}