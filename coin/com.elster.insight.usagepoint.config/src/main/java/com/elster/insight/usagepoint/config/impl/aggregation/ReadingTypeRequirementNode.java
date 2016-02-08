package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;
import com.elster.jupiter.orm.associations.Reference;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNode extends AbstractNode implements ServerExpressionNode {

    private ReadingTypeRequirement readingTypeRequirement;

    public ReadingTypeRequirementNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, ReadingTypeRequirement readingTypeRequirement) {
        super(parent, children);
        this.readingTypeRequirement = readingTypeRequirement;
    }

    public ReadingTypeRequirement getReadingTypeRequirement() {
        return readingTypeRequirement;
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