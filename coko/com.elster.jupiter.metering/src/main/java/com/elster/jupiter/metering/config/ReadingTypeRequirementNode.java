package com.elster.jupiter.metering.config;

/**
 * Created by igh on 17/03/2016.
 */
public interface ReadingTypeRequirementNode extends ExpressionNode {


    String TYPE_IDENTIFIER = "REQ";

    ReadingTypeRequirement getReadingTypeRequirement();
}
