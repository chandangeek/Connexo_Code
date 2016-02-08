package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.Reference;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeDeliverableNode extends AbstractNode implements ExpressionNode {

    private ReadingTypeDeliverable readingTypeDeliverable;

    public ReadingTypeDeliverableNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, ReadingTypeDeliverable readingTypeDeliverable) {
        super(parent, children);
        this.readingTypeDeliverable = readingTypeDeliverable;
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
