package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import java.util.Comparator;

public class ReadingTypeComparator implements Comparator<ReadingType> {

    @Override
    public int compare(ReadingType o1, ReadingType o2) {
        return o1.getMRID().compareToIgnoreCase(o2.getMRID());
    }
}