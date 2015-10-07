package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

class MeterReadingTypeConfigurationImpl {

    private Reference<MeterConfigurationImpl> meterConfiguration = ValueReference.absent();
    private int numberOfDigits;
    private int numberOfFractionDigits;

    private Reference<MultiplierTypeImpl> multiplierType = ValueReference.absent();

    private Reference<ReadingType> readingType = ValueReference.absent();


}
