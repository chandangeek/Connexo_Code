package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.Reference;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeDeliverableNode extends AbstractNode {

    static String TYPE_IDENTIFIER = "DEL";

    private Reference<ReadingTypeDeliverable> readingTypeDeliverable;

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

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitDeliverable(this);
    }

}