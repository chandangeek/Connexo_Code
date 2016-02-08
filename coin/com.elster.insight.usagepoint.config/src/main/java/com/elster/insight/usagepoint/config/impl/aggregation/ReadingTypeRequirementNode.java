package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNode extends AbstractNode implements ServerExpressionNode {

    static String TYPE_IDENTIFIER = "REQ";

    private ReadingTypeRequirement readingTypeRequirement;

    public ReadingTypeRequirementNode(ReadingTypeRequirement readingTypeRequirement) {
        super();
        this.readingTypeRequirement = readingTypeRequirement;
    }

    public ReadingTypeRequirementNode(ExpressionNode parentNode, ReadingTypeRequirement readingTypeRequirement) {
        super();
        this.setParent(parentNode);
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