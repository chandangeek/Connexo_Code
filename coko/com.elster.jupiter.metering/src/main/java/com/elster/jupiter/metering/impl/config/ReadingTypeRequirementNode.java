package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.aggregation.ServerExpressionNode;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNode extends AbstractNode implements ServerExpressionNode {

    static final String TYPE_IDENTIFIER = "REQ";

    //todo add foreign key and replace id by reference
    //private Reference<ReadingTypeRequirement> readingTypeRequirement = ValueReference.absent();
    private long readingTypeRequirement;


    public ReadingTypeRequirementNode(ReadingTypeRequirement readingTypeRequirement) {
        super();
        this.readingTypeRequirement = readingTypeRequirement.getId();
        //this.readingTypeRequirement.set(readingTypeRequirement);
    }

    public ReadingTypeRequirement getReadingTypeRequirement() {
        return null;
        //return readingTypeRequirement.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitRequirement(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitRequirement(this);
    }

}