package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;

/**
 * Created by igh on 17/03/2016.
 */
@ProviderType
public interface ConstantNode extends ExpressionNode {
    BigDecimal getValue();
}