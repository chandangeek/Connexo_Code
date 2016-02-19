package com.elster.jupiter.metering.impl.aggregation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link Function} component.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19 (11:23)
 */
public class FunctionTest {

    @Test
    public void max() {
        assertThat(Function.from(com.elster.jupiter.metering.impl.config.Function.MAX)).isEqualTo(Function.MAX);
    }

    @Test
    public void min() {
        assertThat(Function.from(com.elster.jupiter.metering.impl.config.Function.MIN)).isEqualTo(Function.MIN);
    }

    @Test
    public void sum() {
        assertThat(Function.from(com.elster.jupiter.metering.impl.config.Function.SUM)).isEqualTo(Function.SUM);
    }

    @Test
    public void avg() {
        assertThat(Function.from(com.elster.jupiter.metering.impl.config.Function.AVG)).isEqualTo(Function.AVG);
    }

}