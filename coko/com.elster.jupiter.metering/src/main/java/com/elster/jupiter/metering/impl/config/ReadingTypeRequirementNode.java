package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "REQ";

    private Reference<ReadingTypeRequirement> readingTypeRequirement = ValueReference.absent();

    public ReadingTypeRequirementNode(ReadingTypeRequirement readingTypeRequirement) {
        super();
        this.readingTypeRequirement.set(readingTypeRequirement);
    }

    public ReadingTypeRequirement getReadingTypeRequirement() {
        return readingTypeRequirement.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitRequirement(this);
    }

    @Override
    public ReadingTypeUnit getReadingTypeUnit() {
        //todo check wit Rudi how to handle this
        return null;
    }
}