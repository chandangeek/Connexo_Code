package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Dimension;

import java.util.Optional;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNodeImpl extends AbstractNode implements ReadingTypeRequirementNode {


    //todo add foreign key and replace id by reference
    private Reference<ReadingTypeRequirement> readingTypeRequirement = ValueReference.absent();

    public ReadingTypeRequirementNodeImpl(ReadingTypeRequirement readingTypeRequirement) {
        super();
        this.readingTypeRequirement.set(readingTypeRequirement);
    }

    @Override
    public ReadingTypeRequirement getReadingTypeRequirement() {
        return readingTypeRequirement.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitRequirement(this);
    }

    public Dimension getDimension() {
        return readingTypeRequirement.get().getDimension();
    }
}