package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;
import com.elster.jupiter.orm.associations.Reference;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNode extends AbstractNode implements ExpressionNode {

    private ReadingTypeRequirement readingTypeRequirement;

    public ReadingTypeRequirementNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, ReadingTypeRequirement readingTypeRequirement) {
        super(parent, children);
        this.readingTypeRequirement = readingTypeRequirement;
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
