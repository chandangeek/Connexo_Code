package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeDeliverableNode extends AbstractNode {

    private ReadingTypeDeliverable readingTypeDeliverable;

    public ReadingTypeDeliverableNode(ReadingTypeDeliverable readingTypeDeliverable) {
        super();
        this.readingTypeDeliverable = readingTypeDeliverable;
    }

    public ReadingTypeDeliverableNode(ExpressionNode parentNode, ReadingTypeDeliverable readingTypeDeliverable) {
        super();
        this.setParent(parentNode);
        this.readingTypeDeliverable = readingTypeDeliverable;
    }

    public ReadingTypeDeliverable getReadingTypeDeliverable() {
        return readingTypeDeliverable;
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