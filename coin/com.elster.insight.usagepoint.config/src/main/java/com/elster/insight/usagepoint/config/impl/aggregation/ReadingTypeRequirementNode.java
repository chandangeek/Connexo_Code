package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNode implements ServerExpressionNode {

    private ReadingTypeRequirement readingTypeRequirement;

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