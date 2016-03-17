package com.elster.jupiter.metering.config;

/**
 * Created by igh on 17/03/2016.
 */
public interface ReadingTypeDeliverableNode extends ExpressionNode {


    String TYPE_IDENTIFIER = "DEL";

    ReadingTypeDeliverable getReadingTypeDeliverable();
}
