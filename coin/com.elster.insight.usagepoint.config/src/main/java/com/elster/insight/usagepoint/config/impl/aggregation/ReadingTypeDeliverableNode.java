package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeDeliverableNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "DEL";

    //todo add foreign key and replace id by reference
    //private Reference<ReadingTypeDeliverable> readingTypeDeliverable = ValueReference.absent();
    private long readingTypeDeliverable;

    public ReadingTypeDeliverableNode(ReadingTypeDeliverable readingTypeDeliverable) {
        super();
        //this.readingTypeDeliverable.set(readingTypeDeliverable);
        this.readingTypeDeliverable = readingTypeDeliverable.getId();
    }

    public ReadingTypeDeliverable getReadingTypeDeliverable() {
        return null;
        //return readingTypeDeliverable.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitDeliverable(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitDeliverable(this);
    }

}