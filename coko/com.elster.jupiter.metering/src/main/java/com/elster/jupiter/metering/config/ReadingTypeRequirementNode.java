package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by igh on 17/03/2016.
 */
@ProviderType
public interface ReadingTypeRequirementNode extends ExpressionNode {

    ReadingTypeRequirement getReadingTypeRequirement();

}