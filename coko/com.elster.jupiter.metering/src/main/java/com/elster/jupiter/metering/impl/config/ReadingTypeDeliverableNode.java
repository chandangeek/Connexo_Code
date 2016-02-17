package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

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