package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class FullySpecifiedReadingTypeImpl extends ReadingTypeRequirementImpl implements FullySpecifiedReadingType {
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();

    @Inject
    public FullySpecifiedReadingTypeImpl() {
        super();
    }

    FullySpecifiedReadingTypeImpl init(ReadingType readingType, String name) {
        this.readingType.set(readingType);
        setName(name);
        return this;
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.get();
    }

    @Override
    public boolean matches(ReadingType candidate) {
        return getReadingType().equals(candidate);
    }
}
