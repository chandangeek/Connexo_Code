package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import java.util.ArrayList;
import java.util.List;

class MeterConfigurationImpl implements Effectivity {

    private Reference<Meter> meter = ValueReference.absent();
    private Interval interval;

    private List<MeterReadingTypeConfigurationImpl> readingTypeConfigs = new ArrayList<>();

    @Override
    public Interval getInterval() {
        return interval;
    }

}
