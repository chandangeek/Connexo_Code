package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.ReadingType;

public interface IReadingType extends ReadingType {
    ReadingTypeCodeBuilder builder();
}
