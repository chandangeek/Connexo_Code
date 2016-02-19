package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeDeliverableNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "DEL";

    //todo add foreign key and replace id by reference
    private Reference<ReadingTypeDeliverable> readingTypeDeliverable = ValueReference.absent();

    public ReadingTypeDeliverableNode(ReadingTypeDeliverable readingTypeDeliverable) {
        super();
        this.readingTypeDeliverable.set(readingTypeDeliverable);
    }

    public ReadingTypeDeliverable getReadingTypeDeliverable() {
        return readingTypeDeliverable.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitDeliverable(this);
    }

}