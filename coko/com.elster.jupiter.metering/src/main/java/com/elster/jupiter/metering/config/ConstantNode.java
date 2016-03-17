package com.elster.jupiter.metering.config;

import java.math.BigDecimal;

/**
 * Created by igh on 17/03/2016.
 */
public interface ConstantNode extends ExpressionNode {

    String TYPE_IDENTIFIER = "CST";

    BigDecimal getValue();
}
