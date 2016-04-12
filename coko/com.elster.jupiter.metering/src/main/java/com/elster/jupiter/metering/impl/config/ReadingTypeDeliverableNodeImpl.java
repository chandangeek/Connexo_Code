package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Dimension;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ReadingTypeDeliverableNodeImpl extends AbstractNode implements ReadingTypeDeliverableNode {

    static final String TYPE_IDENTIFIER = "DEL";

    public ReadingTypeDeliverableNodeImpl() {}

    private Reference<ReadingTypeDeliverable> readingTypeDeliverable = ValueReference.absent();

    public ReadingTypeDeliverableNodeImpl(ReadingTypeDeliverable readingTypeDeliverable) {
        super();
        this.readingTypeDeliverable.set(readingTypeDeliverable);
    }

    @Override
    public ReadingTypeDeliverable getReadingTypeDeliverable() {
        return readingTypeDeliverable.orNull();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitDeliverable(this);
    }

    @Override
    public Dimension getDimension() {
        return readingTypeDeliverable.get().getReadingType().getUnit().getUnit().getDimension();
    }

    public String toString() {
        return "D(" + readingTypeDeliverable.get().getId() + ")";
    }

    public List<ReadingTypeRequirementNode> getRequirements() {
        return ((AbstractNode) readingTypeDeliverable.get().getFormula().getExpressionNode()).getRequirements();
    }
}