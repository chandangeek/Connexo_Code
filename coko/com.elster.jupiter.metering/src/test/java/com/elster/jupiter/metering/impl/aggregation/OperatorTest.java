package com.elster.jupiter.metering.impl.aggregation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link Operator} component.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19 (11:23)
 */
public class OperatorTest {

    @Test
    public void plus() {
        assertThat(Operator.from(com.elster.jupiter.metering.impl.config.Operator.PLUS)).isEqualTo(Operator.PLUS);
    }

    @Test
    public void minus() {
        assertThat(Operator.from(com.elster.jupiter.metering.impl.config.Operator.MINUS)).isEqualTo(Operator.MINUS);
    }

    @Test
    public void multiply() {
        assertThat(Operator.from(com.elster.jupiter.metering.impl.config.Operator.MULTIPLY)).isEqualTo(Operator.MULTIPLY);
    }

    @Test
    public void divide() {
        assertThat(Operator.from(com.elster.jupiter.metering.impl.config.Operator.DIVIDE)).isEqualTo(Operator.DIVIDE);
    }

}