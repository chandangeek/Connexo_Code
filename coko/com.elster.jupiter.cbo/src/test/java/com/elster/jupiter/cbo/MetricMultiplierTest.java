package com.elster.jupiter.cbo;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MetricMultiplierTest {

    @Test
    public void testWith() {
        assertThat(MetricMultiplier.with(6)).isEqualTo(MetricMultiplier.MEGA);
    }


}
