package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

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