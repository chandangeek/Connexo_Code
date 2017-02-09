/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Dimension;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeRequirementNodeImpl extends AbstractNode implements ReadingTypeRequirementNode {

    static final String TYPE_IDENTIFIER = "REQ";

    private Reference<ReadingTypeRequirement> readingTypeRequirement = ValueReference.absent();

    // For ORM layer
    @SuppressWarnings("unused")
    public ReadingTypeRequirementNodeImpl() {}

    public ReadingTypeRequirementNodeImpl(ReadingTypeRequirement readingTypeRequirement) {
        super();
        this.readingTypeRequirement.set(readingTypeRequirement);
    }

    @Override
    public ReadingTypeRequirement getReadingTypeRequirement() {
        return readingTypeRequirement.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitRequirement(this);
    }

    public Dimension getDimension() {
        return readingTypeRequirement.get().getDimension();
    }

    public String toString() {
        return "R(" + readingTypeRequirement.get().getName() + ")";
    }

    @Override
    public void validate() {
        // No validation for constants
    }

}