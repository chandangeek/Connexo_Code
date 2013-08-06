package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.ReadingType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
public class ReadingTypeInfos {
    public int total;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();

    ReadingTypeInfos() {
    }

    ReadingTypeInfos(ReadingType usagePoint) {
        add(usagePoint);
    }

    ReadingTypeInfos(Collection<? extends ReadingType> readingTypes) {
        addAll(readingTypes);
    }

    ReadingTypeInfo add(ReadingType readingType) {
        ReadingTypeInfo result = new ReadingTypeInfo(readingType);
        readingTypes.add(result);
        total++;
        return result;
    }

    void addAll(Collection<? extends ReadingType> usagePoints) {
        for (ReadingType each : usagePoints) {
            add(each);
        }
    }

}
